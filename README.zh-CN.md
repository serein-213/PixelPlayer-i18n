<p align="right">
  <a href="README.md">English</a> | <a href="README.zh-CN.md">简体中文</a>
</p>

# PixelPlayer 🎵

<p align="center">
  <img src="assets/icon.png" alt="应用图标" width="128"/>
</p>

<p align="center">
  <strong>一款精美、功能丰富的 Android 音乐播放器</strong><br>
  基于 Jetpack Compose 与 Material Design 3 构建
</p>

<p align="center">
  <img src="assets/screenshot1.jpg" alt="截图 1" width="200" style="border-radius:26px;"/>
  <img src="assets/screenshot2.jpg" alt="截图 2" width="200" style="border-radius:26px;"/>
  <img src="assets/screenshot3.jpg" alt="截图 3" width="200" style="border-radius:26px;"/>
  <img src="assets/screenshot4.jpg" alt="截图 4" width="200" style="border-radius:26px;"/>
</p>

<p align="center">
    <a href="https://github.com/theovilardo/PixelPlayer/releases/latest">
        <img src="https://img.shields.io/github/v/release/theovilardo/PixelPlayer?include_prereleases&logo=github&style=for-the-badge&label=Latest%20Release" alt="最新版本">
    </a>
    <a href="https://github.com/theovilardo/PixelPlayer/releases">
        <img src="https://img.shields.io/github/downloads/theovilardo/PixelPlayer/total?logo=github&style=for-the-badge" alt="总下载量">
    </a>
    <img src="https://img.shields.io/badge/Android-10%2B-green?style=for-the-badge&logo=android" alt="Android 10+">
    <img src="https://img.shields.io/badge/Kotlin-100%25-purple?style=for-the-badge&logo=kotlin" alt="Kotlin">
</p>

---

## ‼️ 免责声明
- 本项目的任何 Fork 版本均不会获得官方支持，如果你使用的是 Fork 版本，请向 Fork 者寻求帮助。

---

## ✨ 功能特性

### 🎨 现代 UI/UX
- **Material You** — 动态配色，自动适配壁纸颜色
- **流畅动画** — 丝滑的页面过渡与微交互效果
- **界面自定义** — 可调节圆角半径与导航栏样式
- **深色/浅色主题** — 自动或手动切换主题
- **专辑封面配色** — 从专辑封面动态提取主题色

### 🎵 强大的播放功能
- **Media3 ExoPlayer** — 业界领先的音频引擎，支持 FFmpeg
- **后台播放** — 完整的媒体会话集成
- **播放队列管理** — 拖拽排序
- **随机/循环播放** — 支持所有播放模式
- **无缝播放** — 曲目间无缝衔接
- **自定义过渡** — 可配置曲目间的淡入淡出效果

### 📚 音乐库管理
- **多格式支持** — MP3、FLAC、AAC、OGG、WAV 等
- **多维度浏览** — 歌曲、专辑、艺术家、流派、文件夹
- **智能艺术家解析** — 可配置多艺术家曲目的分隔符
- **专辑艺术家分组** — 专辑组织更规范
- **文件夹过滤** — 自定义扫描目录

### 🔍 发现与整理
- **全文搜索** — 在整个音乐库中搜索
- **每日混音** — 基于收听习惯的 AI 个性化歌单
- **播放列表** — 创建与管理自定义歌单
- **统计数据** — 追踪收听历史与习惯

### 🎤 歌词
- **同步歌词** — 通过 LRCLIB API 获取 LRC 格式歌词
- **歌词编辑** — 修改或为曲目添加歌词
- **滚动显示** — 随音乐同步高亮显示

### 🖼️ 艺术家图片
- **Deezer 集成** — 自动从 Deezer API 获取艺术家图片
- **智能缓存** — 内存（LRU）+ 数据库缓存，支持离线访问
- **占位图标** — 图片不可用时显示精美占位图

### 📲 连接功能
- **Chromecast** — 投射到电视或智能音箱
- **Android Auto** — 完整支持车载播放（即将推出）
- **桌面小组件** — 使用 Glance 小组件在桌面控制播放

### ⚙️ 高级功能
- **标签编辑器** — 基于 TagLib 编辑元数据（支持 MP3、FLAC、M4A）
- **AI 歌单** — 使用 Gemini AI 生成播放列表
- **音频波形** — 基于 Amplituda 的可视化展示（即将推出）

---

## 🛠️ 技术栈

| 类别 | 技术 |
|------|------|
| **开发语言** | [Kotlin](https://kotlinlang.org/) 100% |
| **UI 框架** | [Jetpack Compose](https://developer.android.com/jetpack/compose) |
| **设计系统** | [Material Design 3](https://m3.material.io/) |
| **音频引擎** | [Media3 ExoPlayer](https://developer.android.com/guide/topics/media/media3) + FFmpeg |
| **架构** | MVVM + StateFlow/SharedFlow |
| **依赖注入** | [Hilt](https://dagger.dev/hilt/) |
| **数据库** | [Room](https://developer.android.com/training/data-storage/room) |
| **网络请求** | [Retrofit](https://square.github.io/retrofit/) + OkHttp |
| **图片加载** | [Coil](https://coil-kt.github.io/coil/) |
| **异步** | Kotlin Coroutines & Flow |
| **后台任务** | WorkManager |
| **元数据** | [TagLib](https://github.com/nicholaus/taglib-android) |
| **小组件** | [Glance](https://developer.android.com/jetpack/compose/glance) |

---

## 📱 系统要求

- **Android 11**（API 30）或更高版本
- 推荐 **4GB RAM** 以获得最佳性能

---

## 🚀 快速开始

### 前置条件

- Android Studio Ladybug | 2024.2.1 或更新版本
- Android SDK 29+
- JDK 11+

### 安装步骤

1. **克隆仓库**
   ```sh
   git clone https://github.com/theovilardo/PixelPlayer.git
   ```

2. **在 Android Studio 中打开**
   - 打开 Android Studio
   - 选择"打开现有项目"
   - 导航到克隆的目录

3. **同步并构建**
   - 等待 Gradle 同步依赖
   - 构建项目（Build → Make Project）

4. **运行**
   - 连接设备或启动模拟器
   - 点击运行（▶️）

---

## ⬇️ 下载

<p align="center">
  <a href="https://github.com/theovilardo/PixelPlayer/releases/latest">
    <img src="https://raw.githubusercontent.com/Kunzisoft/Github-badge/main/get-it-on-github.png" alt="在 GitHub 上获取" height="60">
  </a>
</p>

<p align="center">
  <a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://app/%7B%22id%22%3A%22com.theveloper.pixelplay%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2Ftheovilardo%2FPixelPlayer%22%2C%22author%22%3A%22theovilardo%22%2C%22name%22%3A%22PixelPlayer%22%2C%22supportFixedAPKURL%22%3Afalse%7D">
    <img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="在 Obtainium 上获取" height="50">
  </a>
</p>

---

## 📂 项目结构

```
app/src/main/java/com/theveloper/pixelplay/
├── data/
│   ├── database/       # Room 实体、DAO、迁移
│   ├── model/          # 领域模型（Song、Album、Artist 等）
│   ├── network/        # API 服务（LRCLIB、Deezer）
│   ├── preferences/    # DataStore 偏好设置
│   ├── repository/     # 数据仓库
│   ├── service/        # MusicService、HTTP 服务器
│   └── worker/         # WorkManager 同步 Worker
├── di/                 # Hilt 依赖注入模块
├── presentation/
│   ├── components/     # 可复用 Compose 组件
│   ├── navigation/     # 导航图
│   ├── screens/        # 页面 Composable
│   └── viewmodel/      # ViewModel
├── ui/
│   ├── glancewidget/   # 桌面小组件
│   └── theme/          # 颜色、字体、主题
└── utils/              # 扩展函数与工具类
```

---

## 🤝 贡献

欢迎贡献代码！请随时提交 Pull Request。

1. Fork 本项目
2. 创建功能分支（`git checkout -b feature/AmazingFeature`）
3. 提交更改（`git commit -m 'Add some AmazingFeature'`）
4. 推送到分支（`git push origin feature/AmazingFeature`）
5. 发起 Pull Request

---

## 📄 许可证

本项目基于 MIT 许可证 — 详情请参阅 [LICENSE](LICENSE) 文件。

---

<p align="center">
  由 <a href="https://github.com/theovilardo">theovilardo</a> 用 ❤️ 制作
</p>
