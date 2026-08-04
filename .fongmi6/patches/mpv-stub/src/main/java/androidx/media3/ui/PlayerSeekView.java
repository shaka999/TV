package androidx.media3.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.Player;
import androidx.media3.ui.DefaultTimeBar;
import androidx.media3.ui.TimeBar;

/**
 * FongMi 定制 media3 私有类 PlayerSeekView 的替代实现（stub）。
 *
 * <p>功能尽量对齐：基于官方 {@link TimeBar} 提供完整进度显示、拖动 seek、与
 * Player 状态绑定（setPlayer/getTimeBar/findViewById(exo_progress) 全部兼容）。
 * 仅缺失 FongMi 的"缩略图预览"增强（该功能依赖私有 aar 内的位图提取管线，
 * 源码未公开、官方 APK 中已混淆，无法复刻）。
 */
public class PlayerSeekView extends FrameLayout implements Player.Listener {

    private final TimeBar timeBar;
    @Nullable private Player player;
    private boolean scrubbing;

    public PlayerSeekView(@NonNull Context context) {
        this(context, null);
    }

    public PlayerSeekView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerSeekView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        timeBar = new DefaultTimeBar(context, attrs);
        // 与上游 PlaybackActivity findViewById(androidx.media3.ui.R.id.exo_progress) 对应
        timeBar.setId(androidx.media3.ui.R.id.exo_progress);
        addView(timeBar, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        timeBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(@NonNull TimeBar timeBar, long positionMs) {
                scrubbing = true;
            }

            @Override
            public void onScrubMove(@NonNull TimeBar timeBar, long positionMs) {
                // 拖动中：不实时回写 position（避免与手指竞争）
            }

            @Override
            public void onScrubStop(@NonNull TimeBar timeBar, long positionMs, boolean canceled) {
                scrubbing = false;
                if (!canceled && player != null && positionMs >= 0) {
                    player.seekTo(positionMs);
                }
                update();
            }
        });
    }

    public void setPlayer(@Nullable Player player) {
        if (this.player != null) {
            this.player.removeListener(this);
        }
        this.player = player;
        if (player != null) {
            player.addListener(this);
        }
        update();
    }

    @NonNull
    public TimeBar getTimeBar() {
        return timeBar;
    }

    @Override
    public void onEvents(@NonNull Player player, @NonNull Player.Events events) {
        update();
    }

    private void update() {
        if (player == null) {
            timeBar.setDuration(0);
            timeBar.setPosition(0);
            return;
        }
        long duration = player.getDuration();
        if (duration > 0) {
            timeBar.setDuration(duration);
        }
        if (!scrubbing) {
            timeBar.setPosition(player.getCurrentPosition());
        }
    }
}
