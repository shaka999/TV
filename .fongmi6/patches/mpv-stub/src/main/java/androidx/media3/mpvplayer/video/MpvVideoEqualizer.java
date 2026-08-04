package androidx.media3.mpvplayer.video;

/** FongMi 定制 media3 私有模块的空实现（stub）。create() 工厂方法对应 MpvVideoEffectController 调用。 */
public final class MpvVideoEqualizer {

    public static final MpvVideoEqualizer DEFAULT = new MpvVideoEqualizer();

    private MpvVideoEqualizer() {
    }

    public static MpvVideoEqualizer create(
            float brightness,
            float contrast,
            float saturation,
            float gamma,
            float hue,
            float sharpness) {
        return DEFAULT;
    }
}
