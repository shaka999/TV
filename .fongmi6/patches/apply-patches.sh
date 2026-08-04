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
# BaseLoader.java: 删除 pyLoader 字段/构造/调用（6 处）
sed -i '/private final PyLoader pyLoader;/d' app/src/main/java/com/fongmi/android/tv/api/loader/BaseLoader.java
sed -i '/pyLoader = new PyLoader();/d' app/src/main/java/com/fongmi/android/tv/api/loader/BaseLoader.java
sed -i '/pyLoader.clear();/d' app/src/main/java/com/fongmi/android/tv/api/loader/BaseLoader.java
sed -i '/if (isPy(api)) return pyLoader.getSpider/d' app/src/main/java/com/fongmi/android/tv/api/loader/BaseLoader.java
sed -i '/else if (isPy(api)) pyLoader.setRecent/d' app/src/main/java/com/fongmi/android/tv/api/loader/BaseLoader.java
sed -i '/if ("py".equals(params.get("do"))) return pyLoader.proxy(params);/d' app/src/main/java/com/fongmi/android/tv/api/loader/BaseLoader.java

echo "==> [3/6] minSdk 24 -> 23（兼容 Android 6.0.1）"
sed -i 's/^minSdk = "24"$/minSdk = "23"/' gradle/libs.versions.toml

echo "==> [4/6] 注入 overrideLibrary（压制私有 aar / 模块的 minSdk 24 冲突）"
MANIFEST=app/src/main/AndroidManifest.xml
if ! grep -q 'tools:overrideLibrary' "$MANIFEST"; then
    # 包名列表沿用 4.0.7 分支验证过的集合；若 CI 报新冲突按报错补包名
    sed -i '/tools:ignore="ScopedStorage">/a\    <uses-sdk tools:overrideLibrary="com.github.catvod.crawler,com.fongmi.chaquo,com.fongmi.android.tv.quickjs,com.fongmi.hook,com.forcetech,com.tvbus.engine,com.p2p,com.xunlei.downloadlib,master.flame.danmaku,com.ghost.thunder" />' "$MANIFEST"
fi

echo "==> [5/6] （MPV 空实现 aar 与定制 media3 aar 由 fetch-media3.sh 放入 app/libs）"

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

echo ""
echo "✅ 构建层补丁应用完成。下一步：bash .fongmi6/scripts/fetch-media3.sh 拉 aar，然后 ./gradlew assemble"
