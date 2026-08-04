package androidx.media3.mpvplayer;

/** FongMi 定制 media3 私有模块的空实现（stub），方法均为 no-op。 */
public final class MpvSubtitleOptions {

    private MpvSubtitleOptions() {
    }

    public static final class Builder {

        public Builder() {
        }

        public Builder setPosition(double position) {
            return this;
        }

        public Builder setScale(double scale) {
            return this;
        }

        public Builder setSecondarySubtitle(int trackId, float position, boolean forced) {
            return this;
        }

        public Builder setCustomStyle(
                int textColor,
                int backgroundColor,
                int edgeType,
                int edgeColor,
                float edgeWidth,
                float shadow) {
            return this;
        }

        public Builder setSystemCaptionStyle() {
            return this;
        }

        public MpvSubtitleOptions build() {
            return new MpvSubtitleOptions();
        }
    }
}
