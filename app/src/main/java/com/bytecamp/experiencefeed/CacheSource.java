package com.bytecamp.experiencefeed;

enum CacheSource {
    MEMORY("内存缓存"),
    DISK("磁盘缓存"),
    NETWORK("网络下载");

    final String label;

    CacheSource(String label) {
        this.label = label;
    }
}
