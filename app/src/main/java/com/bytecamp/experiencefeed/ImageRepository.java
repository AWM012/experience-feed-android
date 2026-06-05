package com.bytecamp.experiencefeed;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

final class ImageRepository {
    private static final String TAG = "ExperienceNetwork";
    private static final long HTTP_CACHE_SIZE = 20L * 1024L * 1024L;

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
    private final ImageDownloadService downloadService;

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

        File httpCacheDir = new File(context.getCacheDir(), "okhttp-http-cache");
        Cache httpCache = new Cache(httpCacheDir, HTTP_CACHE_SIZE);

        client = new OkHttpClient.Builder()
                .cache(httpCache)
                .addInterceptor(commonHeadersInterceptor())
                .addInterceptor(loggingInterceptor())
                .addNetworkInterceptor(responseCacheInterceptor())
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .followRedirects(true)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://picsum.photos/")
                .client(client)
                .build();
        downloadService = retrofit.create(ImageDownloadService.class);
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
                    result = downloadToCache(post.imageUrl, key, cacheFile, false);
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

    void reload(ImagePost post, Callback callback) {
        removeCache(post);
        String key = cacheKey(post.imageUrl);
        File cacheFile = new File(imageDir, key + ".jpg");
        executor.execute(() -> {
            try {
                postSuccess(callback, downloadToCache(post.imageUrl, key, cacheFile, true));
            } catch (Throwable throwable) {
                postError(callback, throwable);
            }
        });
    }

    private ImageLoadResult downloadToCache(
            String imageUrl,
            String key,
            File cacheFile,
            boolean forceNetwork
    ) throws IOException {
        String cacheControl = forceNetwork ? "no-cache" : null;
        retrofit2.Response<ResponseBody> response = downloadService
                .download(imageUrl, cacheControl)
                .execute();
        try {
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
            boolean fromHttpCache = response.raw().cacheResponse() != null;
            CacheSource source = fromHttpCache ? CacheSource.HTTP_CACHE : CacheSource.NETWORK;
            return new ImageLoadResult(bitmap, cacheFile, cacheFile.length(), source);
        } finally {
            ResponseBody body = response.body();
            if (body != null) {
                body.close();
            }
        }
    }

    private Interceptor commonHeadersInterceptor() {
        return chain -> chain.proceed(
                chain.request()
                        .newBuilder()
                        .header("User-Agent", "ExperienceFeed/1.0")
                        .header("Accept", "image/*")
                        .build()
        );
    }

    private Interceptor loggingInterceptor() {
        return chain -> {
            long startNanos = System.nanoTime();
            Log.d(TAG, "--> " + chain.request().method() + " " + chain.request().url());
            okhttp3.Response response = chain.proceed(chain.request());
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            String source = response.cacheResponse() != null ? "HTTP_CACHE" : "NETWORK";
            Log.d(TAG, "<-- " + response.code() + " " + durationMs + "ms "
                    + source + " " + response.request().url());
            return response;
        };
    }

    private Interceptor responseCacheInterceptor() {
        return chain -> chain.proceed(chain.request())
                .newBuilder()
                .header("Cache-Control", "public, max-age=86400")
                .build();
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
