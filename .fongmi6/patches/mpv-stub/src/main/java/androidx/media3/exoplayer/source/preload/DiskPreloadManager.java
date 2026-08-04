package androidx.media3.exoplayer.source.preload;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PriorityTaskManager;
import androidx.media3.datasource.Cache;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.RenderersFactory;

/**
 * FongMi 定制 media3 私有类 DiskPreloadManager 的替代实现（stub）。
 *
 * <p>完整 API 结构（Builder/Options/start/release）与 ExoDiskPreload 调用完全兼容。
 * 官方 media3 1.10 提供 {@code source.preload} 预加载 API（DefaultPreloadManager），
 * 但 FongMi 的 DiskPreloadManager 为私有实现，接口不同、源码未公开。
 *
 * <p>此处 start/release 为空操作：磁盘预加载优化降级（播放功能完全不受影响，
 * 仅少了"播前预下载到缓存"这一优化）。后续如需恢复该优化，可在此包装官方
 * DefaultPreloadManager（见 README 备注）。
 */
public final class DiskPreloadManager {

    private DiskPreloadManager() {
    }

    public static final class Builder {

        public Builder(
                @NonNull Cache cache,
                @NonNull DataSource.Factory upstreamFactory,
                @NonNull RenderersFactory renderersFactory) {
        }

        public Builder setPriorityTaskManager(@NonNull PriorityTaskManager priorityTaskManager) {
            return this;
        }

        public DiskPreloadManager build() {
            return new DiskPreloadManager();
        }
    }

    public static final class Options {

        private Options() {
        }

        public static final class Builder {

            public Builder setDurationMs(long durationMs) {
                return this;
            }

            public Builder setMaxThreads(int maxThreads) {
                return this;
            }

            public Options build() {
                return new Options();
            }
        }

        public static Builder builder() {
            return new Builder();
        }
    }

    public void start(@NonNull ExoPlayer player, @NonNull MediaItem mediaItem, @NonNull Options options) {
        // no-op：磁盘预加载优化降级
    }

    public void release() {
        // no-op
    }
}
