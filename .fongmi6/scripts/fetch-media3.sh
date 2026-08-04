#!/usr/bin/env bash
# =============================================================================
# 从 GitHub Release 下载自编译的定制 media3 aar + MPV stub aar，放入 app/libs
# 在 TV 仓库根执行：bash .fongmi6/scripts/fetch-media3.sh
# 依赖：gh CLI 已登录（CI 预装；本地需 gh auth login）
# =============================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

REPO="${GITHUB_REPOSITORY:-shaka999/TV}"
LIBS_DIR=app/libs
mkdir -p "$LIBS_DIR"

# 找最新的 media3 相关 release tag
echo "==> 查找最新的 media3 release"
LATEST=$(gh release list --repo "$REPO" --limit 30 2>/dev/null \
    | grep -oE 'media3-[a-zA-Z0-9._-]+' | head -1 || true)

if [ -z "${LATEST:-}" ]; then
    echo "!! 未找到 media3 release。请先在 Actions 手动触发 'Build Custom Media3' workflow。"
    echo "   或将自编译的 lib-*.aar 手动放入 $LIBS_DIR/"
    exit 1
fi

echo "==> 下载 release: $LATEST"
gh release download "$LATEST" --repo "$REPO" \
    --pattern '*.aar' --dir "$LIBS_DIR" --clobber

echo "==> app/libs 内容："
ls -la "$LIBS_DIR"/*.aar 2>/dev/null || echo "!! 无 aar 文件"
echo ""
echo "✅ aar 已就位。下一步：./gradlew :app:assembleLeanbackArmeabi_v7aRelease :app:assembleLeanbackArm64_v8aRelease"
