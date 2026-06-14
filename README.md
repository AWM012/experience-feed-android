# Experience Feed

参考抖音首页上方「经验」入口实现的 Android 课后作业：一个双列图片流列表和图文详情页。

项目使用 Retrofit 声明图片下载和实时天气接口，底层通过 OkHttp 执行网络请求。图片部分实现了 OkHttp HTTP 响应缓存、Bitmap 内存缓存和图片文件缓存；天气部分使用 Gson 自动解析 JSON，并支持修改和保存天气地点。

## 功能清单

- 双列图片流：首页以瀑布流形式展示 12 条图文内容。
- 实时天气：首页展示当前天气，默认地点为上海，并支持修改和保存地点。
- 城市搜索：先根据城市名称查询经纬度，再使用经纬度获取实时温度、天气、湿度和风速。
- 网络加载：使用 Retrofit + OkHttp 请求 HTTPS 网络图片。
- 请求拦截器：统一添加请求头，并记录请求地址、状态码和耗时。
- 多级缓存：支持 OkHttp 响应缓存、Bitmap 内存缓存和本地图片文件缓存。
- 图文详情：点击卡片进入详情页，展示大图、标题、描述和图片信息。
- 图片描述：展示图片日期、拍摄位置、图片大小、缓存路径。
- 缓存复用：图片文件按 URL 的 SHA-256 命名，写入应用私有缓存目录。
- 加载来源可视化：首页和详情页显示「首次网络下载 / OkHttp 响应缓存 / 本地磁盘缓存 / 当前内存缓存」。
- 手动重载：详情页支持清除当前图片缓存并重新下载。

## 工程结构

```text
.
├── app/
│   └── src/main/java/com/bytecamp/experiencefeed/
│       ├── MainActivity.java          # 双列图片流首页
│       ├── DetailActivity.java        # 图文详情页
│       ├── ImageRepository.java       # Retrofit/OkHttp 配置、拦截器和多级缓存
│       ├── ImageDownloadService.java  # Retrofit 图片下载接口
│       ├── WeatherRepository.java     # 城市搜索、天气查询和天气数据转换
│       ├── WeatherService.java        # Retrofit 天气 JSON 接口
│       ├── FeedData.java              # 示例图片数据
│       ├── ImagePost.java             # 图文数据模型
│       ├── ImageLoadResult.java       # 图片加载结果
│       ├── CacheSource.java           # 加载来源枚举
│       └── UiKit.java                 # 通用 UI 尺寸、颜色、系统栏工具
├── docs/
│   ├── ASSIGNMENT_REQUIREMENTS.md     # 课后作业要求摘录
│   ├── TECHNICAL_SOLUTION.md          # 技术方案文档
│   ├── DESIGN_PRESENTATION.md         # 点评环节方案设计与思考过程
│   ├── NETWORK_LAYER_GUIDE.md         # Retrofit、拦截器和 HTTP 缓存说明
│   ├── WEATHER_FEATURE_GUIDE.md        # 实时天气功能与请求流程说明
│   ├── LEARNING_SUMMARY.md            # 网络请求问题与优化总结
│   ├── DEMO_SCRIPT.md                 # 3 分钟演示录屏脚本
│   └── GITHUB_SUBMISSION.md           # GitHub 提交流程
└── demo/                              # 放置最终演示录屏
```

## 运行方式

1. 使用 Android Studio 打开本仓库。
2. 等待 Gradle Sync 完成。
3. 创建或选择 Android 模拟器。
4. 运行 `app`。

项目主要依赖：

```gradle
implementation "com.squareup.okhttp3:okhttp:4.12.0"
implementation "com.squareup.retrofit2:retrofit:2.11.0"
implementation "com.squareup.retrofit2:converter-gson:2.11.0"
```

## 文档

- 技术方案：[docs/TECHNICAL_SOLUTION.md](docs/TECHNICAL_SOLUTION.md)
- 方案设计与思考过程：[docs/DESIGN_PRESENTATION.md](docs/DESIGN_PRESENTATION.md)
- 网络层说明：[docs/NETWORK_LAYER_GUIDE.md](docs/NETWORK_LAYER_GUIDE.md)
- 实时天气说明：[docs/WEATHER_FEATURE_GUIDE.md](docs/WEATHER_FEATURE_GUIDE.md)
- 学习总结：[docs/LEARNING_SUMMARY.md](docs/LEARNING_SUMMARY.md)
