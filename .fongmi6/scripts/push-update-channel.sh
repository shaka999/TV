#!/usr/bin/env bash
# =============================================================================
# 把 build-tv 编好的 APK 推到 fongmi-4.0.7 分支的 apk/release/ 目录
# → 让已装的 4.0.7 TV 软件点"检查更新"时能直接检测到新版本并下载安装
#
# 用法（在 build-tv workflow 中调用）：bash .fongmi6/scripts/push-update-channel.sh
# 所需环境变量：
#   GH_TOKEN       - 默认用 ${{ secrets.GITHUB_TOKEN }}（可能因跨分支被拒）
#   UPSTREAM_PAT   - 推荐用 PAT（如 github.txt），配成 Secret，可跨分支 push
# =============================================================================
set -euo pipefail

# 解析当前 versionName（5.5.8）
VER=$(grep -oP 'versionName "\K[^"]+' app/build.gradle || echo "unknown")
APK_DIR="/tmp/apks"

# 远端 leanback.json 的 code 字段必须 > 4.0.7 已装包的 versionCode（40706）
# 4.0.7 的 Updater 逻辑：code > BuildConfig.VERSION_CODE 才提示升级
# 这里用 50058（5.5.8 的"5.5.8"伪装编号），APK 本身的 versionCode=558 不影响升级判断
JSON_CODE=50058

# 选择 token：UPSTREAM_PAT 优先（推荐），否则 GH_TOKEN，最后 fallback
TOKEN="${UPSTREAM_PAT:-${GH_TOKEN:-${GITHUB_TOKEN:-}}}}"
if [ -z "$TOKEN" ]; then
    echo "❌ 未配置 UPSTREAM_PAT / GH_TOKEN，无法 push"
    echo "   推荐在 GitHub Secrets 加 UPSTREAM_PAT = github.txt 的 PAT"
    exit 1
fi

# 临时目录克隆 4.0.7 分支
TMP=$(mktemp -d)
trap "rm -rf $TMP" EXIT
echo "==> 克隆 fongmi-4.0.7 分支"
git clone --depth 1 --branch fongmi-4.0.7 \
    "https://x-access-token:${TOKEN}@github.com/shaka999/TV.git" \
    "$TMP"

cd "$TMP"
mkdir -p apk/release

# 复制 APK（按 4.0.7 Updater.getApk 的命名约定：apk/release/leanback-{flavor_abi}.apk）
echo "==> 复制 APK 到 apk/release/"
for apk in "$APK_DIR"/*.apk; do
    name=$(basename "$apk")
    if [[ "$name" == *"armeabi"* ]]; then
        cp "$apk" "apk/release/leanback-armeabi_v7a.apk"
        echo "  ✓ apk/release/leanback-armeabi_v7a.apk"
    elif [[ "$name" == *"arm64"* ]]; then
        cp "$apk" "apk/release/leanback-arm64_v8a.apk"
        echo "  ✓ apk/release/leanback-arm64_v8a.apk"
    fi
done

# 写/更新 leanback.json（4.0.7 Updater 默认拉这个路径）
cat > apk/release/leanback.json <<EOF
{
  "name": "${VER}",
  "code": ${JSON_CODE},
  "desc": "FongMi/TV ${VER} fork 兼容 Android 6.0.1（基于 fongmi-6 构建层改动）；4.0.7 软件内升级通道"
}
EOF
echo "  ✓ apk/release/leanback.json (name=${VER}, code=${JSON_CODE})"

# dev/leanback.json 也保持同步（dev 模式更新检查）
cp apk/release/leanback.json apk/dev/leanback.json

# 提交 + push
git config user.name "github-actions[bot]"
git config user.email "actions@github.com"

if git diff --cached --quiet 2>/dev/null && git diff --quiet 2>/dev/null; then
    # 先 add 再看 staged diff
    git add apk/
    if git diff --cached --quiet; then
        echo "==> 无变更，跳过 push"
        exit 0
    fi
fi
git add apk/

git commit -m "release: ${VER} 6.0.1 兼容版 (code=${JSON_CODE})"
git push origin fongmi-4.0.7
echo "✅ 已推送到 fongmi-4.0.7 分支 apk/release/"