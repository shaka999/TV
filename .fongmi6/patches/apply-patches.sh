#!/usr/bin/env bash
# =============================================================================
# fongmi-6 构建层补丁脚本（幂等，可重复运行）
# 在 fongmi-6 分支的 TV 仓库根目录执行：bash .fongmi6/patches/apply-patches.sh
#
# 作用：在上游 fongmi 代码基础上应用 5 项构建层改动，使其兼容 Android 6.0.1 (API 23)。
# 不改源码逻辑层，只动构建配置，保证 git merge 上游时冲突≈0。
# =============================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

echo "==> [1/6] 备份原始构建文件"
mkdir -p .fongmi6/backup
for f in settings.gradle build.gradle gradle/libs.versions.toml app/build.gradle app/src/main/AndroidManifest.xml; do
    [ -f "$f" ] && cp "$f" ".fongmi6/backup/$(basename "$f").orig"
done

echo "==> [2/6] 移除 chaquopy（Python 爬虫运行时，卡 minSdk 的根因）"
# settings.gradle: 删 include ':chaquo'
sed -i "/^include ':chaquo'$/d" settings.gradle
# 根 build.gradle: 删 chaquopy 插件声明
sed -i "/alias libs.plugins.chaquo.python apply false/d" build.gradle
# app/build.gradle: 删 chaquo 模块依赖
sed -i "/implementation project(':chaquo')/d" app/build.gradle
# toml: 删 python 版本与插件别名
sed -i '/^python = "17.0.0"$/d' gradle/libs.versions.toml
sed -i '/^chaquo-python = { id = "com.chaquo.python"/d' gradle/libs.versions.toml

echo "==> [2.5/6] 移除 PyLoader（chaquopy 的 app 层副产物，引用 com.fongmi.chaquo）"
rm -f app/src/main/java/com/fongmi/android/tv/api/loader/PyLoader.java
BL=app/src/main/java/com/fongmi/android/tv/api/loader/BaseLoader.java
# BaseLoader.java: 删除 pyLoader 字段/构造/调用
sed -i '/private final PyLoader pyLoader;/d' "$BL"
sed -i '/pyLoader = new PyLoader();/d' "$BL"
sed -i '/pyLoader.clear();/d' "$BL"
# 删除 getSpider 里的 if (isPy(...)) return pyLoader.getSpider(...) 分支。
# ⚠️ 5.5.8 里这行是 if-else 链的开头，删掉后下一行 "else if (isJs(...))" 会变成孤立 else，
#    必须同时把下一行开头的 "else " 去掉，否则编译报 "'else' without 'if'"。
sed -i '/if (isPy(api)) return pyLoader.getSpider/d' "$BL"
sed -i 's/^\( *\)else if (isJs(api)) return jsLoader\.getSpider/\1if (isJs(api)) return jsLoader.getSpider/' "$BL"
sed -i '/else if (isPy(api)) pyLoader.setRecent/d' "$BL"
sed -i '/if ("py".equals(params.get("do"))) return pyLoader.proxy(params);/d' "$BL"

echo "==> [3/6] minSdk 24 -> 23（兼容 Android 6.0.1）"
sed -i 's/^minSdk = "24"$/minSdk = "23"/' gradle/libs.versions.toml

echo "==> [4/6] 注入 overrideLibrary（压制私有 aar / 模块的 minSdk 24 冲突）"
MANIFEST=app/src/main/AndroidManifest.xml
if ! grep -q 'tools:overrideLibrary' "$MANIFEST"; then
    # 包名列表沿用 4.0.7 分支验证过的集合；若 CI 报新冲突按报错补包名
    sed -i '/tools:ignore="ScopedStorage">/a\    <uses-sdk tools:overrideLibrary="com.github.catvod.crawler,com.fongmi.chaquo,com.fongmi.android.tv.quickjs,com.fongmi.hook,com.forcetech,com.tvbus.engine,com.p2p,com.xunlei.downloadlib,master.flame.danmaku,com.ghost.thunder" />' "$MANIFEST"
fi

echo "==> [5/6] （MPV 空实现 aar 与定制 media3 aar 由 fetch-media3.sh 放入 app/libs）"
echo "==> [5.5/6] HTML 兼容补丁：Html.fromHtml(X, int flags) -> Html.fromHtml(X)"
echo "    API 24+ 才有的 fromHtml(String, int flags)，6.0.1（API 23）只有废弃的单参版本，会 NoSuchMethodError"
echo "    涉及文件：Vod.java（点播片名解析）、Util.java（通用 HTML 处理）"
for f in app/src/main/java/com/fongmi/android/tv/bean/Vod.java \
         app/src/main/java/com/fongmi/android/tv/utils/Util.java; do
    [ -f "$f" ] || continue
    if grep -q 'Html\.fromHtml([^)]*,[^)]*)' "$f"; then
        sed -i -E 's/Html\.fromHtml\(([^,]+), [^)]+\)/Html.fromHtml(\1)/g' "$f"
        echo "    ✓ 修正: $f"
    fi
done

echo "==> [5.6/6] PiP 兼容补丁：isInPictureInPictureMode() -> false"
echo "    API 24+ 才有的 Activity.isInPictureInPictureMode()，6.0.1 没有该方法，调用即 NoSuchMethodError"
echo "    6.0.1 上系统不支持 PiP，所以替换为字面量 false 逻辑等价（PiP 永远不在该模式）"
echo "    同时处理链式调用 xxx.isInPictureInPictureMode()（如 PiP.java:76 的 activity.isInPictureInPictureMode()）"
echo "    涉及文件：PlaybackActivity (main)、PiP/LiveActivity/VideoActivity (mobile)"
PIP_FILES=(
    app/src/main/java/com/fongmi/android/tv/ui/activity/PlaybackActivity.java
    app/src/mobile/java/com/fongmi/android/tv/utils/PiP.java
    app/src/mobile/java/com/fongmi/android/tv/ui/activity/LiveActivity.java
    app/src/mobile/java/com/fongmi/android/tv/ui/activity/VideoActivity.java
)
for f in "${PIP_FILES[@]}"; do
    [ -f "$f" ] || continue
    # 链式调用：xxx.isInPictureInPictureMode() -> false
    sed -i -E 's/[A-Za-z_][A-Za-z0-9_]*\.isInPictureInPictureMode\(\)/false/g' "$f"
    # 裸调用：isInPictureInPictureMode() -> false
    sed -i -E 's/isInPictureInPictureMode\(\)/false/g' "$f"
    echo "    ✓ PiP 兼容: $f"
done

echo "==> [5.7/6] Service.stopForeground(int) 兼容（API 24+ → boolean）"
echo "    API 24 才有的 Service.stopForeground(int flags) + STOP_FOREGROUND_REMOVE 常量，"
echo "    API 23 只有已废弃的 stopForeground(boolean)，调用 int 版本即 NoSuchMethodError"
echo "    修复：调用点从 stopForeground(STOP_FOREGROUND_REMOVE) 改为 stopForeground(true)（语义等价：移除前台 + 取消通知）"
PBS=app/src/main/java/com/fongmi/android/tv/service/PlaybackService.java
if [ -f "$PBS" ] && grep -q 'stopForeground(STOP_FOREGROUND_REMOVE)' "$PBS"; then
    sed -i 's/stopForeground(STOP_FOREGROUND_REMOVE)/stopForeground(true)/g' "$PBS"
    echo "    ✓ Service 兼容: $PBS"
fi

echo "==> [5.8/6] 移除 MPV 播放器选项（stub 不可用，选项误导用户）"
echo "    1) select_engine 数组删 MPV 项（设置页只显示 EXO）"
echo "    2) PlayerEngineDialog 删 mpv 点击/选中逻辑"
echo "    3) PlayerSetting clamp 上限锁死 ENGINE_EXO（防旧偏好值越界）"
STRINGS=app/src/main/res/values/strings.xml
PED=app/src/main/java/com/fongmi/android/tv/ui/dialog/PlayerEngineDialog.java
PS=app/src/main/java/com/fongmi/android/tv/setting/PlayerSetting.java
if [ -f "$STRINGS" ]; then
    sed -i '/<item>MPV<\/item>/d' "$STRINGS"
    echo "    ✓ 数组移除 MPV: $STRINGS"
fi
if [ -f "$PED" ]; then
    sed -i '/binding.mpv.setOnClickListener/d' "$PED"
    sed -i '/binding.mpv.setSelected/d' "$PED"
    sed -i 's/return getCurrentEngine(player) == PlayerSetting.ENGINE_MPV ? binding.mpv : binding.exo;/return binding.exo;/' "$PED"
    echo "    ✓ 对话框移除 MPV: $PED"
fi
if [ -f "$PS" ]; then
    sed -i 's/Math.clamp(Prefers.getInt("player_engine", ENGINE_EXO), ENGINE_EXO, ENGINE_MPV)/Math.clamp(Prefers.getInt("player_engine", ENGINE_EXO), ENGINE_EXO, ENGINE_EXO)/g' "$PS"
    sed -i 's/Math.clamp(engine, ENGINE_EXO, ENGINE_MPV)/Math.clamp(engine, ENGINE_EXO, ENGINE_EXO)/g' "$PS"
    echo "    ✓ 引擎锁死 EXO: $PS"
fi

echo "==> [6/6] 校验改动"
echo "--- settings.gradle includes:"
grep -n 'include' settings.gradle
echo "--- toml minSdk:"
grep -n 'minSdk' gradle/libs.versions.toml
echo "--- overrideLibrary:"
grep -n 'overrideLibrary' "$MANIFEST" || echo "!! 未注入 overrideLibrary"
echo "--- 残留 chaquo 引用（应为空）:"
grep -rn 'chaquo' settings.gradle build.gradle app/build.gradle gradle/libs.versions.toml app/src/main/java/com/fongmi/android/tv/api/loader/ 2>/dev/null || echo "OK 无残留"
echo "--- PyLoader 残留:"
ls app/src/main/java/com/fongmi/android/tv/api/loader/PyLoader.java 2>/dev/null && echo "!! PyLoader 未删除" || echo "OK PyLoader 已移除"
echo "--- BaseLoader 残留 pyLoader（应为空）:"
grep -n 'pyLoader' app/src/main/java/com/fongmi/android/tv/api/loader/BaseLoader.java 2>/dev/null || echo "OK 无残留"
echo "--- HTML fromHtml 残留 2 参（应为空）:"
grep -rn 'Html\.fromHtml([^)]*,[^)]*)' app/src/main/java/ 2>/dev/null || echo "OK 无残留"
echo "--- isInPictureInPictureMode() 残留调用（应为空）:"
grep -rnE '\.isInPictureInPictureMode\(\)| isInPictureInPictureMode\(\)' app/src/ 2>/dev/null || echo "OK 无残留"
echo "--- Service.stopForeground(STOP_FOREGROUND_REMOVE) 残留（应为空）:"
grep -rn 'stopForeground(STOP_FOREGROUND_REMOVE)' app/src/ 2>/dev/null || echo "OK 无残留"
echo "--- MPV 选项残留（应为空）:"
grep -rn 'ENGINE_MPV\|<item>MPV</item>\|binding.mpv' app/src/ 2>/dev/null | grep -v 'isMpv' | head -5 || echo "OK 无残留"

echo ""
echo "✅ 构建层补丁应用完成。下一步：bash .fongmi6/scripts/fetch-media3.sh 拉 aar，然后 ./gradlew assemble"
