# 随机图片 API 聚合 App

一款支持多数据源的随机图片浏览应用，主打二次元内容，支持瀑布流浏览、收藏、搜索等功能。

## 功能特性

### 多 API 数据源
| API | 特点 | 搜索 | NSFW |
|-----|------|------|------|
| **Lolicon** | 二次元、R18、标签丰富 | ✅ | ✅ |
| **萌图** | 国内随机二次元图片 | ❌ | ❌ |
| **色图API** | 二次元色图、支持标签搜索 | ✅ | ✅ |
| **Kori图库** | 二次元图库、支持分类 | ❌ | ✅ |
| **随机美图** | 国内随机美图 | ❌ | ❌ |
| **二次元风景** | 国内二次元风景 | ❌ | ❌ |

### 核心功能

- **卡片浏览** - 左右滑动切换图片
- **瀑布流布局** - 非对称瀑布流，无限滚动
- **图片搜索** - 支持标签搜索
- **推荐标签** - 每个 API 提供推荐标签
- **NSFW 切换** - 一键切换成人内容
- **图片详情** - 全屏查看，支持缩放

### 收藏功能

- **收藏图片** - 点击爱心图标收藏
- **收藏分组** - 支持创建分组管理
- **收藏页面** - 网格展示所有收藏

### 图片操作

- **下载** - 保存到本地相册
- **分享** - 分享到微信/QQ
- **设为壁纸** - 设置为手机壁纸

### 设置功能

- **主题切换** - 浅色/深色/跟随系统
- **莫奈取色** - Android 12+ 动态颜色
- **使用统计** - 浏览/收藏/下载/搜索次数
- **缓存管理** - 清除缓存/历史/搜索记录

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose + Material3
- **架构**: MVVM + Clean Architecture
- **依赖注入**: Hilt
- **本地存储**: Room Database
- **图片加载**: Coil
- **网络请求**: Retrofit + Moshi

## 项目结构

```
app/src/main/java/com/randomimage/
├── data/
│   ├── local/          # Room 数据库
│   ├── remote/         # API 接口
│   └── repository/     # 数据仓库
├── domain/
│   └── model/          # 数据模型
├── ui/
│   ├── components/     # UI 组件
│   ├── screens/        # 页面
│   ├── theme/          # 主题
│   └── viewmodel/      # ViewModel
├── di/                 # 依赖注入
└── util/               # 工具类
```

## 安装使用

1. 从 [Releases](https://github.com/lingyuan0914/random-image-app/releases) 下载最新 APK
2. 安装到 Android 设备（需要 Android 8.0+）
3. 打开应用，选择数据源开始浏览

## 开发环境

- Android Studio Hedgehog+
- JDK 17
- Android SDK 34
- Gradle 8.5

## 构建

```bash
# 设置环境变量
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk

# 构建 Debug APK
./gradlew assembleDebug
```

## API 配置

部分 API 需要配置密钥：

1. **Unsplash API**: 在 `app/build.gradle.kts` 中替换 `YOUR_API_KEY_HERE`
2. **Lolicon API**: 免费无需配置
3. **其他 API**: 免费无需配置

## 权限说明

| 权限 | 用途 |
|------|------|
| `INTERNET` | 网络请求 |
| `WRITE_EXTERNAL_STORAGE` | 保存图片（Android 9 及以下） |
| `SET_WALLPAPER` | 设置壁纸 |

## 更新日志

### v1.0.0 (2026-06-14)
- 首次发布
- 支持 6 个 API 数据源
- 瀑布流布局
- 收藏功能
- 主题切换
- 莫奈取色

## 许可证

MIT License

## 联系方式

- GitHub: [@lingyuan0914](https://github.com/lingyuan0914)
