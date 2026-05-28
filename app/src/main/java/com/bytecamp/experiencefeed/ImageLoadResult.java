package com.bytecamp.experiencefeed;

import android.graphics.Bitmap;

import java.io.File;

final class ImageLoadResult {
    final Bitmap bitmap;
    final File cacheFile;
    final long sizeBytes;
    final CacheSource source;

    ImageLoadResult(Bitmap bitmap, File cacheFile, long sizeBytes, CacheSource source) {
        this.bitmap = bitmap;
        this.cacheFile = cacheFile;
        this.sizeBytes = sizeBytes;
        this.source = source;
    }
}
