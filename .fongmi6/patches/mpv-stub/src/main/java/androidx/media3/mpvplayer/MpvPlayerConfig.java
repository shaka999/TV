package androidx.media3.mpvplayer;

import android.content.Context;
import java.io.File;

/** FongMi 定制 media3 私有模块的空实现（stub），方法均为 no-op。 */
public final class MpvPlayerConfig {

    private MpvPlayerConfig() {
    }

    public static final class Builder {

        public Builder() {
        }

        public Builder setHlsHttpPersistent(boolean value) {
            return this;
        }

        public Builder addConfigDirectory(File dir) {
            return this;
        }

        public Builder addAndroidFontConfig(File configDir, File cacheDir) {
            return this;
        }

        public Builder addAndroidDefaults(MpvAndroidOptions options) {
            return this;
        }

        public Builder addTlsCaFileFromAsset(Context context, String assetName, File destFile) {
            return this;
        }

        public Builder addAndroidSubtitleOptions(Context context, MpvSubtitleOptions options) {
            return this;
        }

        public Builder addDiskCacheOptions(File dir, int seconds) {
            return this;
        }

        public MpvPlayerConfig build() {
            return new MpvPlayerConfig();
        }
    }
}
