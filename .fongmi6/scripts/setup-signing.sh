#!/usr/bin/env bash
# =============================================================================
# 生成 local.properties（签名配置）—— CI 与本地通用
# 在 TV 仓库根执行：bash .fongmi6/scripts/setup-signing.sh
#
# 优先使用仓库 Secrets（KEYSTORE_B64/KEYSTORE_PASS/KEY_ALIAS）；
# 缺失时生成临时自签 keystore（仅测试用，正式发布请配置 Secrets）。
# =============================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

KEY_DIR=.fongmi6/keystore
mkdir -p "$KEY_DIR"

if [ -n "${KEYSTORE_B64:-}" ] && [ -n "${KEYSTORE_PASS:-}" ]; then
    echo "==> 使用 Secrets 签名配置"
    echo "$KEYSTORE_B64" | base64 -d > "$KEY_DIR/release.jks"
    cat > local.properties <<EOF
storeFile=../$KEY_DIR/release.jks
keyAlias=${KEY_ALIAS:-tv}
storePassword=${KEYSTORE_PASS}
sdk.dir=${ANDROID_SDK_ROOT:-/usr/local/lib/android/sdk}
EOF
    echo "✅ 签名已配置（来自 Secrets）"
else
    echo "!! 未配置签名 Secrets，生成临时自签 keystore（仅测试，正式发布请配置 Secrets）"
    PASS="fongmi6temp"
    keytool -genkeypair -v \
        -keystore "$KEY_DIR/release.jks" -storepass "$PASS" -keypass "$PASS" \
        -alias tv -keyalg RSA -keysize 2048 -validity 36500 \
        -dname "CN=FongMi6, OU=TV, O=TV, L=SH, S=SH, C=CN" 2>/dev/null
    cat > local.properties <<EOF
storeFile=../$KEY_DIR/release.jks
keyAlias=tv
storePassword=$PASS
sdk.dir=${ANDROID_SDK_ROOT:-/usr/local/lib/android/sdk}
EOF
    echo "✅ 临时签名已配置（keystore 在 $KEY_DIR/release.jks）"
fi
