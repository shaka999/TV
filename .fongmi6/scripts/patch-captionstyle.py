#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""为 FongMi 定制 media3 的 CaptionStyleCompat 补上 TV 需要的 8 参构造（构建层补丁）。

背景：FongMi TV 5.5.8 的 SubtitleSetting 调用 8 参数构造
      (foregroundColor, backgroundColor, windowColor, edgeType, edgeColor, typeface, edgeWidth, shadow)，
      该构造只在 FongMi 私有 lib-*.aar 中（公开源码仅 6 参官方版）。
      本脚本在自编译 lib-ui 前给公开源码加上 edgeWidth/shadow 字段与 8 参构造，
      使 TV 编译通过且字幕样式（颜色/边缘类型）保持功能，edgeWidth/shadow 按 0 兜底。
用法：python3 patch-captionstyle.py <path/to/CaptionStyleCompat.java>
"""
import sys

p = sys.argv[1]
src = open(p, encoding='utf-8').read()

# 1) 加字段 edgeWidth / shadow（锚点：typeface 字段声明）
old_field = '  @Nullable public final Typeface typeface;'
new_field = old_field + '\n  public final float edgeWidth;\n  public final float shadow;'
if 'public final float edgeWidth' not in src:
    assert old_field in src, 'field anchor not found'
    src = src.replace(old_field, new_field, 1)

# 2) 6 参构造体补默认值
old_init = '    this.typeface = typeface;\n  }'
new_init = '    this.typeface = typeface;\n    this.edgeWidth = 0;\n    this.shadow = 0;\n  }'
if 'this.edgeWidth = 0' not in src:
    assert old_init in src, 'init anchor not found'
    src = src.replace(old_init, new_init, 1)

# 3) 追加 8 参构造（类结束符 } 之前）
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
    this(foregroundColor, backgroundColor, windowColor, edgeType, edgeColor, typeface);
    this.edgeWidth = edgeWidth;
    this.shadow = shadow;
  }
'''
marker = 'float edgeWidth,\n      float shadow)'
if marker not in src:
    body = src.rstrip()
    assert body.endswith('}'), 'class end not found'
    idx = body.rfind('}')
    src = body[:idx] + eight + body[idx:]

open(p, 'w', encoding='utf-8').write(src)
print('OK patched:', p)
