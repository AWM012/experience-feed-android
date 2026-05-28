package com.bytecamp.experiencefeed;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

final class ImageRepository {
    interface Callback {
        void onSuccess(ImageLoadResult result);

        void onError(Throwable throwable);
    }

    private static volatile ImageRepository instance;

    static ImageRepository get(Context context) {
        if (instance == null) {
            synchronized (ImageRepository.class) {
                if (instance == null) {
                    instance = new ImageRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private final File imageDir;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final LruCache<String, Bitmap> memoryCache;
    private final OkHttpClient client;

    private ImageRepository(Context context) {
        imageDir = new File(context.getCacheDir(), "experience-images");
        if (!imageDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            imageDir.mkdirs();
        }

        int maxKb = (int) (Runtime.getRuntime().maxMemory() / 1024 / 8);
        memoryCache = new LruCache<String, Bitmap>(maxKb) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return Math.max(1, value.getByteCount() / 1024);
            }
        };

        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .followRedirects(true)
                .build();
    }

    void load(ImagePost post, Callback callback) {
        String key = cacheKey(post.imageUrl);
        File cacheFile = new File(imageDir, key + ".jpg");
        Bitmap cachedBitmap = memoryCache.get(key);
        if (cachedBitmap != null) {
            postSuccess(callback, new ImageLoadResult(
                    cachedBitmap,
                    cacheFile,
                    cacheFile.exists() ? cacheFile.length() : 0,
                    CacheSource.MEMORY
            ));
            return;
        }

        executor.execute(() -> {
            try {
                ImageLoadResult result;
                if (cacheFile.exists() && cacheFile.length() > 0) {
                    Bitmap bitmap = decodeFile(cacheFile);
                    memoryCache.put(key, bitmap);
                    result = new ImageLoadResult(bitmap, cacheFile, cacheFile.length(), CacheSource.DISK);
                } else {
                    result = downloadToCache(post.imageUrl, key, cacheFile);
                }
                postSuccess(callback, result);
            } catch (Throwable throwable) {
                postError(callback, throwable);
            }
        });
    }

    File cacheFileFor(ImagePost post) {
        return new File(imageDir, cacheKey(post.imageUrl) + ".jpg");
    }

    void removeCache(ImagePost post) {
        String key = cacheKey(post.imageUrl);
        memoryCache.remove(key);
        File file = cacheFileFor(post);
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    private ImageLoadResult downloadToCache(String imageUrl, String key, File cacheFile) throws IOException {
        Request request = new Request.Builder()
                .url(imageUrl)
                .header("User-Agent", "ExperienceFeed/1.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code());
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("empty response body");
            }
            byte[] bytes = body.bytes();
            writeAtomically(cacheFile, bytes);
            Bitmap bitmap = decodeFile(cacheFile);
            memoryCache.put(key, bitmap);
            return new ImageLoadResult(bitmap, cacheFile, cacheFile.length(), CacheSource.NETWORK);
        }
    }

    private Bitmap decodeFile(File cacheFile) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath(), options);
        if (bitmap == null) {
            throw new IOException("decode failed: " + cacheFile.getAbsolutePath());
        }
        return bitmap;
    }

    private void writeAtomically(File cacheFile, byte[] bytes) throws IOException {
        File tempFile = new File(cacheFile.getParentFile(), cacheFile.getName() + ".tmp");
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            outputStream.write(bytes);
            outputStream.flush();
        }
        if (cacheFile.exists() && !cacheFile.delete()) {
            throw new IOException("can not replace cache file");
        }
        if (!tempFile.renameTo(cacheFile)) {
            throw new IOException("can not move temp cache file");
        }
    }

    private void postSuccess(Callback callback, ImageLoadResult result) {
        mainHandler.post(() -> callback.onSuccess(result));
    }

    private void postError(Callback callback, Throwable throwable) {
        mainHandler.post(() -> callback.onError(throwable));
    }

    private String cacheKey(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            return String.valueOf(value.hashCode());
        }
    }
}
