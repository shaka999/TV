# fongmi-6 构建方案（Android 6.0.1 兼容 + 贴合上游 + CI 构建）

## 一句话
以兼容 Android 6.0.1（API 23）为硬前提，整体跟进上游 FongMi/TV 5.5.8，**分叉全控制在构建层**（不改源码逻辑），构建全部走 GitHub Actions，实现"贴原版 + 可持续更新"。保留 `fongmi-4.0.7` 分支随时回退。

## 核心依据（已实测）
- 上游 `minSdk=24` 唯一根因是 **chaquopy 17**（官方要求 API 24）→ 移除后回 23
- 定制 media3 `release-1.10.1-fongmi` 分支 **minSdk=23**，本身兼容 6.0.1
- 上游代码所有高版本 API 都有 `SDK_INT` 守卫，API 23 上不崩
- 官方 APK 逆向：MPV 用的是开源 `is.xyz.mpv`（mpv-android），弹幕用定制 `androidx.media3.ui.danmaku`（私有 aar）

## 分支模型
```
upstream FongMi/TV · fongmi  ──sync──▶  shaka999/TV · fongmi（纯追踪，不改）
                                              │
                                              ▼ apply 构建层补丁（CI 内）
                                        shaka999/TV · fongmi-6（出包）
                                              │
shaka999/TV · fongmi-4.0.7 ◄── 回退安全网（保留不动）
```

## 构建层 5 项改动（apply-patches.sh，幂等）
1. **移除 chaquopy**：settings.gradle / 根 build.gradle / app/build.gradle / toml 四处删 chaquo 引用
2. **移除 PyLoader（chaquopy 副产物）**：删 `PyLoader.java` + BaseLoader.java 里 6 处 pyLoader 引用（`.py` 解析源自动降级为不可用，返回空 Spider，不崩）
3. **minSdk 24 → 23**：toml 一行
4. **overrideLibrary**：manifest 注入，压制私有 aar（forcetech/hook/thunder/tvbus/jianpian 等）的 minSdk 24 冲突
5. **自编译 media3 aar**：CI 编 `FongMi/media` 定制分支，产出 lib 放 app/libs
6. **MPV 空实现 stub**：提供 `androidx.media3.mpvplayer.*` 空类（`isAvailable()=false`），上游源码零改动编译通过，运行时自动回退 ExoPlayer

## 文件结构
```
fongmi6/
├── README.md                       # 本文件
├── patches/
│   ├── apply-patches.sh            # 构建层补丁脚本（核心）
│   └── mpv-stub/                   # MPV 空实现 stub 工程（独立 Android library）
│       ├── settings.gradle
│       ├── build.gradle            # compileOnly media3-common:1.10.0
│       └── src/main/java/androidx/media3/mpvplayer/
│           ├── MpvPlayer.java      # implements Player（118 方法 no-op）+ 特有方法 + Builder
│           ├── MpvPlayerConfig.java
│           ├── MpvAndroidOptions.java
│           ├── MpvSubtitleOptions.java
│           ├── audio/MpvAudioFilter.java     # 函数式接口 + EMPTY + Builder
│           ├── audio/AudioChannelMix.java
│           └── video/MpvVideoEqualizer.java  # DEFAULT + create() 静态工厂
├── workflows/
│   ├── build-media3.yml            # 编 media3 + stub → aar Release（手动/每周一）
│   ├── build-tv.yml                # 主构建：merge→patch→fetch aar→编 APK→Release
│   └── sync-upstream.yml           # 每日检测上游 → merge fongmi → 触发 build-tv
└── scripts/
    ├── fetch-media3.sh             # 从 Release 下载 aar 到 app/libs
    └── setup-signing.sh            # 生成 local.properties（签名）
```

## 首次部署（在 fork 仓库 shaka999/TV 上）
1. **建分支**：`git checkout -b fongmi-6 origin/fongmi`（基于 fongmi 追踪分支）
2. **复制文件**：把 `fongmi6/patches` 和 `fongmi6/scripts` 复制到仓库 `.fongmi6/`，把 `fongmi6/workflows/*.yml` 复制到 `.github/workflows/`
3. **配置签名 Secrets（可选但推荐）**：仓库 Settings → Secrets：
   - `KEYSTORE_B64`：你的 release keystore 的 base64（`base64 -w0 release.jks`）
   - `KEYSTORE_PASS`：keystore 密码
   - `KEY_ALIAS`：key 别名（默认 tv）
   - 不配置则用临时自签 keystore（仅测试，不能覆盖安装升级）
4. **跑 build-media3.yml**（Actions → Build Custom Media3 → Run）→ 产出 aar Release
5. **跑 build-tv.yml**（Actions → Build TV → Run）→ 产出 APK Release
6. **6.0.1 真机回归**（见下方清单）

## 日常更新（全自动）
- `sync-upstream.yml` 每天 UTC 20:30 检测上游 fongmi 更新 → ff-merge 到 fongmi 分支 → 自动触发 build-tv.yml → 出新 APK
- 你只需在 6.0.1 真机上验证新包

## ⚠️ 已知风险与迭代点（首次构建可能需 1-2 轮微调）
1. **MPV stub 方法签名**：118 个 Player 方法从官方 `release`（1.10.1）分支提取；stub 工程用 `media3-common:1.10.0` 编译，若 1.10.0 与 1.10.1 方法集有细微差异，CI 会报"未实现方法 X" → 按提示在 MpvPlayer.java 补该方法即可
2. **overrideLibrary 包名**：列表沿用 `fongmi-4.0.7` 验证过的集合；5.5.8 私有 aar 包名若变，CI 报"library X minSdk Y" → 按报错把 X 包名加到 manifest 的 overrideLibrary 列表
3. **FFmpeg native**：`build-media3.yml` 默认只编 `decoder_ffmpeg` 的 Java 部分（未放 FFmpeg 源码 → native 自动跳过）。结果：app 能编译、硬解可用，但**软解（FFmpeg）不可用**。需软解时：clone `FongMi/FFmpeg` 到 `media/libraries/decoder_ffmpeg/src/main/jni/ffmpeg/` + 装 NDK/CMake + 取消 workflow 里 NDK 行注释
4. **media3-common 版本**：stub 工程用官方 maven `1.10.0`；若 google maven 无此版，改 build.gradle 为 `1.9.1` 或与定制版本对齐
5. **overrideLibrary 与移除 chaquo 共存**：override 列表含 `com.fongmi.chaquo`（无害，因模块已移除无对应库），无需删
6. **已复查覆盖的 MPV 引用面**：全仓 import `androidx.media3.mpvplayer.*` 共 6 个文件（MpvUtil / MpvPlayerEngine / MpvPlayerEffect / MpvVideoEffectController / MpvAudioEffectFilter / AudioEffectProcessor），stub 已覆盖其全部类与方法（含 AudioChannelMix 的 mixStereoLeft/mixStereoRight/mixMono）；ExoUtil 用到的定制 API（DecodeTrackSelector / DefaultPreloadManager / EventLogger）均在自编译 exoplayer 模块内

## 6.0.1 真机回归最小清单
- [ ] 冷启动不崩
- [ ] 直播源加载播放
- [ ] 点播（VOD）详情 + 选集播放
- [ ] 弹幕开关（渲染走定制 media3，应正常）
- [ ] 通知栏控制（播放/暂停/停止）
- [ ] 设置页各项无崩
- [ ]forcetech/thunder 私有 aar 相关入口（若用到）不崩

## 决策记录
- **chaquopy：移除**（不降级，JS/JAR 解析源已够用；真缺 Python 源再回填）
- **自编译定制 media3：接受**（一次性 CI 工程，产出 aar 长期复用）
- **MPV：搁置**（用 stub 空实现自动回退 ExoPlayer；自编译 mpv-android 留作后续）
- **保留 fongmi-4.0.7：是**（回退安全网，零成本切换）
