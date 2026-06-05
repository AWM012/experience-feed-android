package com.bytecamp.experiencefeed;

enum CacheSource {
    MEMORY("当前内存缓存"),
    DISK("本地磁盘缓存"),
    HTTP_CACHE("OkHttp 响应缓存"),
    NETWORK("首次网络下载");

    final String label;

    CacheSource(String label) {
        this.label = label;
    }
}
