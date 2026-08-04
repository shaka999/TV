#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""为 FongMi 定制 media3 公开源码补上 TV 5.5.8 依赖的私有 API（构建层补丁，不改 TV 源码）。

FongMi 把一批定制 API 编进私有 lib-*.aar（源码未公开）。自编译定制 media3 时
需补齐这些成员，TV 才能编译通过。所有补丁均为"加成员"（常量 / default 方法 /
no-op 方法），幂等（存在则跳过）。

用法：python3 patch-media3.py <media_repo_root>
"""
import os
import sys

ROOT = sys.argv[1]

def patch(path, fn):
    p = os.path.join(ROOT, path)
    if not os.path.exists(p):
        print('SKIP (missing):', path)
        return
    src = open(p, encoding='utf-8').read()
    before = src
    src = fn(src)
    if src != before:
        open(p, 'w', encoding='utf-8').write(src)
        print('OK patched:', path)
    else:
        print('OK no-change:', path)

# ---------- 1) C.java: DECODE_SOFTWARE / DECODE_HARDWARE ----------
def patch_c(src):
    if 'DECODE_SOFTWARE' in src:
        return src
    anchor = 'public final class C {'
    add = anchor + '''
  public static final int DECODE_SOFTWARE = 0;
  public static final int DECODE_HARDWARE = 1;
'''
    assert anchor in src, 'C.java anchor not found'
    return src.replace(anchor, add, 1)

# ---------- 2) CaptionStyleCompat.java: 8参构造 + DEFAULT_EDGE_WIDTH/SHADOW ----------
def patch_csc(src):
    # 2a 常量
    if 'DEFAULT_EDGE_WIDTH' not in src:
        anchor = 'public final class CaptionStyleCompat {'
        add = anchor + '''
  public static final float DEFAULT_EDGE_WIDTH = 0.0f;
  public static final float DEFAULT_SHADOW_OFFSET = 0.0f;
'''
        assert anchor in src, 'CaptionStyleCompat anchor not found'
        src = src.replace(anchor, add, 1)
    # 2b 字段
    if 'public final float edgeWidth' not in src:
        old_field = '  @Nullable public final Typeface typeface;'
        assert old_field in src, 'CaptionStyleCompat field anchor not found'
        src = src.replace(old_field, old_field + '\n  public final float edgeWidth;\n  public final float shadow;', 1)
    # 2c 6参构造体默认值
    if 'this.edgeWidth = 0' not in src:
        old_init = '    this.typeface = typeface;\n  }'
        assert old_init in src, 'CaptionStyleCompat init anchor not found'
        src = src.replace(old_init, old_init.replace('}', '') + '    this.edgeWidth = 0;\n    this.shadow = 0;\n  }', 1)
    # 2d 8参构造（直接全字段赋值，final 字段不能二次赋值）
    marker = 'float edgeWidth,\n      float shadow)'
    if marker not in src:
        eight = '''
  /**
   * 8-argument constructor required by FongMi TV app (SubtitleSetting.getStyle).
   */
  public CaptionStyleCompat(
      int foregroundColor,
      int backgroundColor,
      int windowColor,
      @EdgeType int edgeType,
      int edgeColor,
      @Nullable Typeface typeface,
      float edgeWidth,
      float shadow) {
    this.foregroundColor = foregroundColor;
    this.backgroundColor = backgroundColor;
    this.windowColor = windowColor;
    this.edgeType = edgeType;
    this.edgeColor = edgeColor;
    this.typeface = typeface;
    this.edgeWidth = edgeWidth;
    this.shadow = shadow;
  }
'''
        body = src.rstrip()
        assert body.endswith('}'), 'CaptionStyleCompat end not found'
        idx = body.rfind('}')
        src = body[:idx] + eight + body[idx:]
    return src

# ---------- 3) SubtitleView.java: setTextSizeScale(float) ----------
def patch_stv(src):
    if 'setTextSizeScale' in src:
        return src
    add = '''
  /**
   * FongMi TV calls this (SubtitleSetting.applyStyle). Kept as no-op-compatible:
   * text size scaling is handled by platform caption sizing.
   */
  public void setTextSizeScale(float textSizeScale) {
    // no-op
  }
'''
    body = src.rstrip()
    assert body.endswith('}'), 'SubtitleView end not found'
    idx = body.rfind('}')
    return body[:idx] + add + body[idx:]

# ---------- 4) PlayerView.java: toggleDebugView / isDebugViewVisible ----------
def patch_pv(src):
    if 'isDebugViewVisible' in src:
        return src
    add = '''
  private boolean debugViewVisible;

  /** FongMi TV calls this (PlaybackActivity.toggleDebugView). */
  public void toggleDebugView() {
    debugViewVisible = !debugViewVisible;
  }

  /** FongMi TV calls this (PlaybackActivity). */
  public boolean isDebugViewVisible() {
    return debugViewVisible;
  }
'''
    body = src.rstrip()
    assert body.endswith('}'), 'PlayerView end not found'
    idx = body.rfind('}')
    return body[:idx] + add + body[idx:]

# ---------- 5) ExoPlayer.java (interface): 特效/音频处理常量 + default 方法 ----------
def patch_ep(src):
    if 'getVideoEffectsSupport' in src:
        return src
    anchor = 'public interface ExoPlayer extends Player {'
    add = anchor + '''
  // FongMi custom constants (used by ExoPlayerEffect)
  int VIDEO_EFFECTS_SUPPORTED = 0;
  int VIDEO_EFFECTS_UNSUPPORTED_DRM = 1;
  int VIDEO_EFFECTS_UNSUPPORTED_RENDERER = 2;
  int VIDEO_EFFECTS_UNSUPPORTED_TUNNELING = 3;
  int AUDIO_PROCESSING_SUPPORTED = 0;
  int AUDIO_PROCESSING_UNSUPPORTED_PASSTHROUGH = 1;

  /** FongMi TV calls this (ExoPlayerEffect). */
  default int getVideoEffectsSupport() {
    return VIDEO_EFFECTS_SUPPORTED;
  }

  /** FongMi TV calls this (ExoPlayerEffect). */
  default int getAudioProcessingSupport() {
    return AUDIO_PROCESSING_SUPPORTED;
  }

  /** FongMi TV calls this (ExoPlayerEffect). */
  default boolean isSkipSilenceSupported() {
    return false;
  }
'''
    assert anchor in src, 'ExoPlayer anchor not found'
    return src.replace(anchor, add, 1)

# ---------- 6) DefaultRenderersFactory.java: setDolbyVisionOutputPolicy ----------
def patch_drf(src):
    if 'setDolbyVisionOutputPolicy' in src:
        return src
    add = '''
  /** FongMi TV calls this (ExoUtil.buildRenderersFactory). Kept as builder no-op. */
  public DefaultRenderersFactory setDolbyVisionOutputPolicy(int policy) {
    return this;
  }
'''
    body = src.rstrip()
    assert body.endswith('}'), 'DefaultRenderersFactory end not found'
    idx = body.rfind('}')
    return body[:idx] + add + body[idx:]

patch('libraries/common/src/main/java/androidx/media3/common/C.java', patch_c)
patch('libraries/ui/src/main/java/androidx/media3/ui/CaptionStyleCompat.java', patch_csc)
patch('libraries/ui/src/main/java/androidx/media3/ui/SubtitleView.java', patch_stv)
patch('libraries/ui/src/main/java/androidx/media3/ui/PlayerView.java', patch_pv)
patch('libraries/exoplayer/src/main/java/androidx/media3/exoplayer/ExoPlayer.java', patch_ep)
patch('libraries/exoplayer/src/main/java/androidx/media3/exoplayer/DefaultRenderersFactory.java', patch_drf)
print('ALL DONE')
