package androidx.media3.mpvplayer;

import java.io.File;

/** FongMi 定制 media3 私有模块的空实现（stub），方法均为 no-op。 */
public final class MpvAndroidOptions {

    private MpvAndroidOptions() {
    }

    public static final class Builder {

        public Builder() {
        }

        public Builder setShaderCacheDirectory(File dir) {
            return this;
        }

        public Builder setAudioPassthroughEnabled(boolean enabled) {
            return this;
        }

        public Builder setDolbyVisionOutputPolicy(int policy) {
            return this;
        }

        public Builder setGpuNextEnabled(boolean enabled) {
            return this;
        }

        public Builder setVulkanEnabled(boolean enabled) {
            return this;
        }

        public MpvAndroidOptions build() {
            return new MpvAndroidOptions();
        }
    }
}
