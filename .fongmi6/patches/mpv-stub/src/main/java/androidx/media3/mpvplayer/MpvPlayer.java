package androidx.media3.mpvplayer;

import android.content.Context;
import android.os.Looper;
import android.view.Surface;
import android.view.SurfaceHolder;
import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.Commands;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.PlayerMessage;
import androidx.media3.common.Timeline;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.VideoSize;
import androidx.media3.mpvplayer.audio.MpvAudioFilter;
import androidx.media3.mpvplayer.video.MpvVideoEqualizer;
import java.util.List;

/**
 * FongMi 定制 media3 私有模块 androidx.media3.mpvplayer.MpvPlayer 的空实现（stub）。
 * isAvailable() 恒 false，运行时自动回退 ExoPlayer；implements Player 以满足
 * MpvPlayerEngine.getPlayer() 返回类型约束。代码零改动，后续 merge 无冲突。
 */
public final class MpvPlayer implements Player {

    public static final int VIDEO_EFFECTS_SUPPORTED = 0;
    public static final int VIDEO_EFFECTS_UNSUPPORTED = 1;
    public static final int VIDEO_EFFECTS_UNSUPPORTED_DIRECT_DOLBY_VISION_OUTPUT = 2;

    public static final int AUDIO_EFFECTS_SUPPORTED = 0;
    public static final int AUDIO_EFFECTS_UNSUPPORTED = 1;
    public static final int AUDIO_EFFECTS_UNSUPPORTED_PASSTHROUGH = 2;

    public static boolean isAvailable() {
        return false;
    }

    private MpvPlayer() {
    }

    // ---- MpvPlayer 特有方法（非 Player 接口）----

    public int getVideoEffectsSupport() {
        return VIDEO_EFFECTS_UNSUPPORTED;
    }

    public boolean isVideoSharpnessSupported() {
        return false;
    }

    public int getAudioEffectsSupport() {
        return AUDIO_EFFECTS_UNSUPPORTED;
    }

    public int getAudioChannelCount() {
        return 0;
    }

    public boolean setAudioFilter(MpvAudioFilter filter) {
        return false;
    }

    public void setAudioOutputListener(@Nullable MpvAudioFilter listener) {
    }

    public void addSubtitle(MediaItem.SubtitleConfiguration config) {
    }

    public void setDecode(int decode) {
    }

    public void setSubtitleOptions(MpvSubtitleOptions options) {
    }

    public void setVideoEqualizer(MpvVideoEqualizer equalizer) {
    }

    // ---- Player 接口方法实现（no-op / 默认值）----

  public Looper getApplicationLooper() { return null; }
  public void addListener(Listener listener) {}
  public void removeListener(Listener listener) {}
  public void setMediaItems(List<MediaItem> mediaItems) {}
  public void setMediaItems(List<MediaItem> mediaItems, boolean resetPosition) {}
  public void setMediaItems(List<MediaItem> mediaItems, int startIndex, long startPositionMs) {}
  public void setMediaItem(MediaItem mediaItem) {}
  public void setMediaItem(MediaItem mediaItem, long startPositionMs) {}
  public void setMediaItem(MediaItem mediaItem, boolean resetPosition) {}
  public void addMediaItem(MediaItem mediaItem) {}
  public void addMediaItem(int index, MediaItem mediaItem) {}
  public void addMediaItems(List<MediaItem> mediaItems) {}
  public void addMediaItems(int index, List<MediaItem> mediaItems) {}
  public void moveMediaItem(int currentIndex, int newIndex) {}
  public void moveMediaItems(int fromIndex, int toIndex, int newIndex) {}
  public void replaceMediaItem(int index, MediaItem mediaItem) {}
  public void replaceMediaItems(int fromIndex, int toIndex, List<MediaItem> mediaItems) {}
  public void removeMediaItem(int index) {}
  public void removeMediaItems(int fromIndex, int toIndex) {}
  public void clearMediaItems() {}
  public boolean isCommandAvailable(int command) { return false; }
  public boolean canAdvertiseSession() { return false; }
  public Commands getAvailableCommands() { return null; }
  public void prepare() {}
  public int getPlaybackState() { return 0; }
  public int getPlaybackSuppressionReason() { return 0; }
  public boolean isPlaying() { return false; }
  public PlaybackException getPlayerError() { return null; }
  public void play() {}
  public void pause() {}
  public void setPlayWhenReady(boolean playWhenReady) {}
  public boolean getPlayWhenReady() { return false; }
  public void setRepeatMode(int repeatMode) {}
  public int getRepeatMode() { return 0; }
  public void setShuffleModeEnabled(boolean shuffleModeEnabled) {}
  public boolean getShuffleModeEnabled() { return false; }
  public boolean isLoading() { return false; }
  public void seekToDefaultPosition() {}
  public void seekToDefaultPosition(int mediaItemIndex) {}
  public void seekTo(long positionMs) {}
  public void seekTo(int mediaItemIndex, long positionMs) {}
  public long getSeekBackIncrement() { return 0; }
  public void seekBack() {}
  public long getSeekForwardIncrement() { return 0; }
  public void seekForward() {}
  public boolean hasPreviousMediaItem() { return false; }
  public void seekToPreviousMediaItem() {}
  public long getMaxSeekToPreviousPosition() { return 0; }
  public void seekToPrevious() {}
  public boolean hasNextMediaItem() { return false; }
  public void seekToNextMediaItem() {}
  public void seekToNext() {}
  public void setPlaybackParameters(PlaybackParameters playbackParameters) {}
  public PlaybackParameters getPlaybackParameters() { return null; }
  public void stop() {}
  public void release() {}
  public Tracks getCurrentTracks() { return null; }
  public TrackSelectionParameters getTrackSelectionParameters() { return null; }
  public void setTrackSelectionParameters(TrackSelectionParameters parameters) {}
  public MediaMetadata getMediaMetadata() { return null; }
  public MediaMetadata getPlaylistMetadata() { return null; }
  public void setPlaylistMetadata(MediaMetadata mediaMetadata) {}
  public Object getCurrentManifest() { return null; }
  public Timeline getCurrentTimeline() { return null; }
  public int getCurrentPeriodIndex() { return 0; }
  public int getCurrentWindowIndex() { return 0; }
  public int getCurrentMediaItemIndex() { return 0; }
  public int getNextWindowIndex() { return 0; }
  public int getNextMediaItemIndex() { return 0; }
  public int getPreviousWindowIndex() { return 0; }
  public int getPreviousMediaItemIndex() { return 0; }
  public MediaItem getCurrentMediaItem() { return null; }
  public int getMediaItemCount() { return 0; }
  public MediaItem getMediaItemAt(int index) { return null; }
  public long getDuration() { return 0; }
  public long getCurrentPosition() { return 0; }
  public long getBufferedPosition() { return 0; }
  public int getBufferedPercentage() { return 0; }
  public long getTotalBufferedDuration() { return 0; }
  public boolean isCurrentWindowDynamic() { return false; }
  public boolean isCurrentMediaItemDynamic() { return false; }
  public boolean isCurrentWindowLive() { return false; }
  public boolean isCurrentMediaItemLive() { return false; }
  public long getCurrentLiveOffset() { return 0; }
  public boolean isCurrentWindowSeekable() { return false; }
  public boolean isCurrentMediaItemSeekable() { return false; }
  public boolean isPlayingAd() { return false; }
  public int getCurrentAdGroupIndex() { return 0; }
  public int getCurrentAdIndexInAdGroup() { return 0; }
  public long getContentDuration() { return 0; }
  public long getContentPosition() { return 0; }
  public long getContentBufferedPosition() { return 0; }
  public AudioAttributes getAudioAttributes() { return null; }
  public float getVolume() { return 0; }
  public void mute() {}
  public void unmute() {}
  public void clearVideoSurface() {}
  public void clearVideoSurface(Surface surface) {}
  public void setVideoSurface(Surface surface) {}
  public void setVideoSurfaceHolder(SurfaceHolder surfaceHolder) {}
  public void clearVideoSurfaceHolder(SurfaceHolder surfaceHolder) {}
  public void setVideoSurfaceView(SurfaceView surfaceView) {}
  public void clearVideoSurfaceView(SurfaceView surfaceView) {}
  public void setVideoTextureView(TextureView textureView) {}
  public void clearVideoTextureView(TextureView textureView) {}
  public VideoSize getVideoSize() { return null; }
  public Size getSurfaceSize() { return null; }
  public CueGroup getCurrentCues() { return null; }
  public DeviceInfo getDeviceInfo() { return null; }
  public int getDeviceVolume() { return 0; }
  public boolean isDeviceMuted() { return false; }
  public void increaseDeviceVolume() {}
  public void increaseDeviceVolume(int flags) {}
  public void decreaseDeviceVolume() {}
  public void decreaseDeviceVolume(int flags) {}
  public void setDeviceMuted(boolean muted) {}
  public void setDeviceMuted(boolean muted, int flags) {}
  public void setAudioAttributes(AudioAttributes audioAttributes, boolean handleAudioFocus) {}

    // ---- Builder ----

    public static final class Builder {

        public Builder(Context context) {
        }

        public Builder setDecode(int decode) {
            return this;
        }

        public Builder setConfig(MpvPlayerConfig config) {
            return this;
        }

        public MpvPlayer build() {
            return new MpvPlayer();
        }
    }
}
