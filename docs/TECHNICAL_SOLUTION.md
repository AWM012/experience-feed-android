# Experience Feed 技术方案文档

## 1. 作业目标

本项目参考抖音 App 首页上方「经验」入口的内容形态，独立实现一个 Android 图文浏览应用，核心目标是完成课程「课后作业」中要求的双列图片流列表和图文详情页。

本次实现覆盖以下要求：

- 使用 OkHttp 发起网络图片请求。
- 在 Activity 中展示双列图片流列表。
- 点击列表图片进入图文详情页，展示大图。
- 展示图片描述信息，包括标题、日期、拍摄位置、图片大小、缓存路径和加载来源。
- 已经下载过的图片在下次进入时优先使用缓存，避免重复请求网络。
- 提供技术方案文档、演示录屏脚本和 GitHub 提交说明，方便作业点评。

## 2. 工程概览

项目采用原生 Android Java 实现，尽量减少框架依赖，重点展示网络请求、图片解码、缓存复用和页面跳转这几块基础能力。

```text
app/src/main/java/com/bytecamp/experiencefeed/
├── MainActivity.java        # 双列图片流首页
├── DetailActivity.java      # 图文详情页
├── ImageRepository.java     # Retrofit/OkHttp 配置、图片解码和多级缓存
├── ImageDownloadService.java # Retrofit 图片下载接口
├── FeedData.java            # 示例图文数据
├── ImagePost.java           # 图文数据模型
├── ImageLoadResult.java     # 图片加载结果
├── CacheSource.java         # 加载来源枚举
└── UiKit.java               # UI 尺寸、颜色、圆角和系统栏工具
```

依赖选择：

```gradle
implementation "com.squareup.okhttp3:okhttp:4.12.0"
implementation "com.squareup.retrofit2:retrofit:2.11.0"
```

## 3. 页面结构设计

### 首页

首页由 `MainActivity` 负责，整体分为三块：

- 顶部横向 Tab：模拟抖音首页顶部频道，突出「经验」入口。
- 标题区域：说明当前内容流主题和数量。
- 双列图片流：以左右两列瀑布流形式展示 12 条图文内容。

双列布局没有直接使用第三方图片列表组件，而是使用 `ScrollView + LinearLayout` 动态构建：

```text
ScrollView
└── LinearLayout vertical
    ├── HorizontalScrollView tabs
    ├── title block
    └── LinearLayout horizontal
        ├── left column
        ├── gap
        └── right column
```

图片卡片会根据预估高度分配到当前较短的一列，让左右两列高度更接近，形成轻量瀑布流效果。

### 详情页

详情页由 `DetailActivity` 负责，页面包括：

- 顶部返回栏：处理 Android 新系统沉浸状态栏下的安全区域，保证按钮不贴状态栏。
- 大图区域：展示当前图文的大图。
- 信息卡片：展示标题、描述、加载来源、日期、位置、图片大小和缓存路径。
- 重新下载按钮：用于演示清除缓存后重新请求网络图片。

首页点击卡片后，通过 `Intent` 把图文基础信息传给详情页。详情页仍然通过 `ImageRepository` 统一加载图片，因此可以复用同一套内存缓存和磁盘缓存逻辑。

## 4. 图片加载流程

图片加载统一收敛在 `ImageRepository` 中，Activity 不直接关心网络请求和文件读写细节。

加载流程：

```text
Activity 调用 load(post)
        ↓
检查内存缓存 LruCache
        ↓ 未命中
检查磁盘缓存 cacheDir/experience-images
        ↓ 未命中
Retrofit 声明下载请求，OkHttp 执行网络传输
        ↓
写入本地缓存文件
        ↓
BitmapFactory 解码
        ↓
主线程回调更新 UI
```

线程模型：

- 网络请求、磁盘读写和图片解码放在线程池中执行。
- UI 更新通过主线程 `Handler` 回到 Activity。
- Activity 销毁后不再更新 UI，避免异步回调影响已关闭页面。

## 5. 缓存方案

缓存分为三层：

- 内存缓存：`LruCache<String, Bitmap>`，用于当前进程内快速复用。
- 图片文件缓存：应用私有缓存目录 `cacheDir/experience-images`，用于应用重启后的复用。
- OkHttp 响应缓存：`cacheDir/okhttp-http-cache`，缓存 HTTP 原始响应，容量为 20 MB。

网络层使用 Retrofit 的 `@GET + @Url` 声明图片下载接口。Retrofit 负责把 Java 接口调用转换为 HTTP 请求，真正的网络连接、响应缓存和请求执行仍由 OkHttp 完成。

OkHttp 配置了三个拦截器：

- 请求头拦截器：统一添加 `User-Agent` 和 `Accept: image/*`。
- 日志拦截器：记录请求方法、URL、状态码和耗时。
- 响应缓存拦截器：为网络响应添加一天的 HTTP 缓存策略。

缓存 key 使用图片 URL 的 SHA-256 值：

```text
SHA-256(imageUrl) + ".jpg"
```

这样设计有三个好处：

- 同一个 URL 对应同一个缓存文件。
- 避免 URL 中的特殊字符直接进入文件名。
- 文件名长度稳定，适合在本地缓存目录中保存。

写入磁盘时先写临时文件，再重命名为正式文件：

```text
xxx.jpg.tmp -> xxx.jpg
```

这样可以避免下载中断时留下损坏的正式缓存文件。

## 6. UI 与交互细节

本项目的 UI 目标不是做复杂动效，而是让作业功能表达清楚：

- 首页和详情页显示「首次网络下载 / OkHttp 响应缓存 / 本地磁盘缓存 / 当前内存缓存」，方便录屏时演示不同层级的缓存命中。
- 详情页展示缓存文件路径，说明图片确实已经落到本地。
- 详情页提供「测试 OkHttp 响应缓存」按钮，清除图片文件缓存但保留 HTTP 缓存，用于演示 OkHttp 缓存命中。
- 顶部栏和底部内容处理状态栏、导航栏安全区域，避免在新版本 Android 模拟器上出现贴边和遮挡。
- 卡片使用圆角白底，列表背景使用浅色，让图片内容成为视觉重点。

## 7. 异常处理

当前已处理的异常场景：

- HTTP 状态码非 2xx 时进入失败回调。
- 响应体为空时进入失败回调。
- 图片文件解码失败时进入失败回调。
- Activity 已销毁时忽略异步加载结果。
- 重新下载时会先移除当前图片缓存，再重新走加载链路。

当前仍可优化的方向：

- 增加失败占位图。
- 增加弱网重试策略。
- 按图片实际尺寸采样解码，降低大图内存压力。
- 为磁盘缓存增加最大容量和过期清理策略。

## 8. 技术取舍

本项目刻意选择「少依赖、逻辑透明」的实现方式：

- 不使用 Glide 或 Coil，是为了展示自己对 Retrofit、OkHttp、图片解码和缓存复用的理解。
- 不使用 RecyclerView，是为了让双列分配算法更直观，适合作业讲解。
- 使用原生 Activity 和 Java，是为了贴近课程基础要求，降低额外框架干扰。

如果升级到更接近生产环境的版本，可以考虑：

- 使用 `RecyclerView + StaggeredGridLayoutManager` 提升长列表性能。
- 使用 ViewModel 保存页面状态，降低旋转屏幕或重建 Activity 带来的影响。
- 使用成熟图片库接管采样解码、缓存清理、请求取消和生命周期绑定。
- 增加分页加载和下拉刷新，让图片流更接近真实内容产品。

## 9. 验证方式

本地已完成 debug 构建验证：

```bash
./gradlew assembleDebug
```

建议录屏前重点验证：

- 首页能展示双列图片流。
- 首次进入显示网络下载。
- 再次进入或进入详情页时可以命中内存缓存/磁盘缓存。
- 点击卡片进入详情页。
- 详情页返回按钮可正常回到首页。
- 重新下载按钮可以触发重新请求。
