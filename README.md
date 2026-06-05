# Experience Feed

参考抖音首页上方「经验」入口实现的 Android 课后作业：一个双列图片流列表和图文详情页。

项目使用 Retrofit 声明图片下载接口，底层通过 OkHttp 请求网络图片，并实现 OkHttp HTTP 响应缓存、Bitmap 内存缓存和图片文件缓存。图片已经下载过时，二次进入会优先读取缓存，避免重复请求网络。

## 功能清单

- 双列图片流：首页以瀑布流形式展示 12 条图文内容。
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
```

## 演示建议

录屏时建议按下面顺序展示：

1. 打开 App，展示首页双列图片流。
2. 滚动列表，说明图片卡片包含标题、描述、日期、大小、位置和加载来源。
3. 点击任意卡片进入详情页。
4. 展示大图、图片大小、缓存路径和加载来源。
5. 返回首页。
6. 重新进入 App 或详情页，展示缓存命中。

演示脚本见 [docs/DEMO_SCRIPT.md](docs/DEMO_SCRIPT.md)。

## 作业文档

- 技术方案：[docs/TECHNICAL_SOLUTION.md](docs/TECHNICAL_SOLUTION.md)
- 方案设计与思考过程：[docs/DESIGN_PRESENTATION.md](docs/DESIGN_PRESENTATION.md)
- 网络层说明：[docs/NETWORK_LAYER_GUIDE.md](docs/NETWORK_LAYER_GUIDE.md)
- 学习总结：[docs/LEARNING_SUMMARY.md](docs/LEARNING_SUMMARY.md)
- 提交检查清单：[docs/SUBMISSION_CHECKLIST.md](docs/SUBMISSION_CHECKLIST.md)
