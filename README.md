# Experience Feed Android

一个原生 Android 图文浏览应用，参考短视频内容社区中的「经验」信息流形态，实现双列图片流、图文详情、实时天气和多级图片缓存。

项目重点放在 Android 基础能力的完整串联：页面搭建、网络请求、图片解码、缓存复用、页面跳转和异常处理。

## Features

- 双列图片流：以左右两列展示 12 条图文内容，按卡片高度做轻量瀑布流分配。
- 图文详情页：点击卡片进入详情，展示大图、标题、描述、日期、位置、图片大小和缓存路径。
- 实时天气：经验页展示当前天气，默认上海，支持修改地点并保存上次选择。
- Retrofit 网络层：使用接口声明图片下载、城市搜索和实时天气请求。
- OkHttp 拦截器：统一添加请求头，并记录请求地址、状态码和耗时。
- 多级图片缓存：支持 OkHttp HTTP 缓存、Bitmap 内存缓存和本地磁盘缓存。
- 缓存来源展示：首页和详情页显示网络下载、HTTP 缓存、磁盘缓存或内存缓存。
- 手动重载：详情页支持清除当前图片缓存并重新下载，便于观察缓存链路。

## Screens

```text
Experience Feed
├── 首页
│   ├── 顶部频道 Tab
│   ├── 实时天气卡片
│   └── 双列图片流
└── 详情页
    ├── 大图预览
    ├── 图文信息
    ├── 缓存路径
    └── 重新下载
```

## Tech Stack

- Language: Java
- UI: Android View system, Activity, programmatic layout
- Network: Retrofit, OkHttp
- JSON: Gson Converter
- Cache: OkHttp Cache, LruCache, app private cache directory
- Data source:
  - Images: Picsum Photos
  - Weather: Open-Meteo

## Architecture

```text
Activity
  ├── MainActivity
  └── DetailActivity
        ↓
Repository
  ├── ImageRepository
  └── WeatherRepository
        ↓
Retrofit Service
  ├── ImageDownloadService
  └── WeatherService
        ↓
OkHttp
```

页面层只处理展示和交互，网络请求、图片解码、缓存判断和天气数据转换都收敛在 Repository 中。

## Image Loading Flow

```text
load image
  ↓
check memory cache
  ↓
check local image file
  ↓
request image by Retrofit + OkHttp
  ↓
write file cache
  ↓
decode bitmap
  ↓
update UI on main thread
```

图片缓存文件使用 URL 的 SHA-256 值命名，避免 URL 中的特殊字符影响本地文件路径。

## Weather Flow

天气接口需要经纬度，而用户输入的是城市名称，因此天气请求分为两步：

```text
city name
  ↓
geocoding api
  ↓
latitude / longitude
  ↓
weather api
  ↓
current weather
```

应用会保存最后一次选择的城市。首次启动默认查询上海。

## Project Structure

```text
app/src/main/java/com/bytecamp/experiencefeed/
├── MainActivity.java          # 首页、天气卡片、双列图片流
├── DetailActivity.java        # 图文详情页
├── ImageRepository.java       # 图片请求、解码和多级缓存
├── ImageDownloadService.java  # Retrofit 图片下载接口
├── WeatherRepository.java     # 城市搜索、天气查询和结果转换
├── WeatherService.java        # Retrofit 天气接口
├── FeedData.java              # 示例图文数据
├── ImagePost.java             # 图文数据模型
├── ImageLoadResult.java       # 图片加载结果
├── CacheSource.java           # 加载来源枚举
└── UiKit.java                 # UI 尺寸、颜色、圆角和系统栏工具
```

## Run

1. Clone this repository.
2. Open the project with Android Studio.
3. Wait for Gradle Sync to finish.
4. Create or select an Android emulator.
5. Run the `app` configuration.

Main dependencies:

```gradle
implementation "com.squareup.okhttp3:okhttp:4.12.0"
implementation "com.squareup.retrofit2:retrofit:2.11.0"
implementation "com.squareup.retrofit2:converter-gson:2.11.0"
```

## Build

```bash
./gradlew assembleDebug
```

The app requires internet access for image and weather requests.

## Notes

This project intentionally keeps the implementation close to Android fundamentals. It does not use Glide, Coil or RecyclerView, so the image request, bitmap decoding, cache selection and two-column layout logic remain explicit and easy to inspect.

Possible next steps:

- Replace the manual two-column layout with `RecyclerView + StaggeredGridLayoutManager`.
- Add pagination and pull-to-refresh.
- Add image placeholders and retry states.
- Add location permission support for automatic weather city detection.
