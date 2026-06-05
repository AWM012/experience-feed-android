# 网络层说明：Retrofit、OkHttp 拦截器与响应缓存

## 1. 三者分别负责什么

- Retrofit：把 Java 接口方法转换成 HTTP 请求。本项目使用 `@GET + @Url` 下载原始图片字节，不需要 JSON 转换器。
- OkHttp：真正负责建立网络连接、发送请求、接收响应。
- OkHttp 拦截器：请求和响应的统一处理入口，用于添加公共请求头、记录日志和设置缓存策略。

## 2. Retrofit 图片下载接口

`ImageDownloadService` 使用动态 URL：

```java
@GET
Call<ResponseBody> download(
        @Url String imageUrl,
        @Header("Cache-Control") String cacheControl
);
```

Retrofit 不只可以请求 JSON。返回类型使用 `ResponseBody` 时，可以直接获得图片、文件等原始响应内容。

## 3. OkHttp 拦截器

项目配置了三个拦截器：

- `commonHeadersInterceptor`：统一添加 `User-Agent` 和 `Accept: image/*`。
- `loggingInterceptor`：记录请求 URL、状态码、耗时，以及响应来自网络还是 HTTP 缓存。
- `responseCacheInterceptor`：为图片响应统一添加一天的缓存策略。

在 Android Studio Logcat 中搜索：

```text
ExperienceNetwork
```

可以看到类似日志：

```text
--> GET https://picsum.photos/...
<-- 200 320ms NETWORK https://picsum.photos/...
<-- 200 2ms HTTP_CACHE https://picsum.photos/...
```

## 4. 多级缓存顺序

```text
Bitmap 内存缓存
    ↓ 未命中
本地图片文件缓存
    ↓ 未命中
Retrofit 请求
    ↓
OkHttp HTTP 响应缓存
    ↓ 未命中
真实网络请求
```

详情页的「测试 OkHttp 响应缓存」会删除 Bitmap 内存缓存和本地图片文件缓存，但保留 OkHttp HTTP 缓存。再次加载时，可以观察到加载来源变成「OkHttp 响应缓存」。

详情页的「重新下载图片」会通过 `Cache-Control: no-cache` 绕过缓存，重新访问网络。
