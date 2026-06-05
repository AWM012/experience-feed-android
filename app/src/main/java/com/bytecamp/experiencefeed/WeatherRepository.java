package com.bytecamp.experiencefeed;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

final class WeatherRepository {
    private static final String TAG = "ExperienceWeather";
    private static final String GEOCODING_URL =
            "https://geocoding-api.open-meteo.com/v1/search";
    private static final String FORECAST_URL =
            "https://api.open-meteo.com/v1/forecast";
    private static final String CURRENT_FIELDS =
            "temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m";

    interface Callback {
        void onSuccess(WeatherResult result);

        void onError(Throwable throwable);
    }

    static final class WeatherResult {
        final String city;
        final String area;
        final double temperature;
        final double apparentTemperature;
        final int humidity;
        final double windSpeed;
        final String condition;
        final String updateTime;

        WeatherResult(
                String city,
                String area,
                double temperature,
                double apparentTemperature,
                int humidity,
                double windSpeed,
                String condition,
                String updateTime
        ) {
            this.city = city;
            this.area = area;
            this.temperature = temperature;
            this.apparentTemperature = apparentTemperature;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.condition = condition;
            this.updateTime = updateTime;
        }
    }

    static final class GeocodingResponse {
        List<GeocodingResult> results;
    }

    static final class GeocodingResult {
        String name;
        String country;
        String admin1;
        double latitude;
        double longitude;
    }

    static final class ForecastResponse {
        CurrentWeather current;
    }

    static final class CurrentWeather {
        String time;

        @SerializedName("temperature_2m")
        double temperature;

        @SerializedName("apparent_temperature")
        double apparentTemperature;

        @SerializedName("relative_humidity_2m")
        int humidity;

        @SerializedName("weather_code")
        int weatherCode;

        @SerializedName("wind_speed_10m")
        double windSpeed;
    }

    private static volatile WeatherRepository instance;

    static WeatherRepository get(Context context) {
        if (instance == null) {
            synchronized (WeatherRepository.class) {
                if (instance == null) {
                    instance = new WeatherRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final WeatherService service;

    private WeatherRepository(Context context) {
        Cache cache = new Cache(
                new File(context.getCacheDir(), "weather-http-cache"),
                5L * 1024L * 1024L
        );
        OkHttpClient client = new OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(chain -> chain.proceed(
                        chain.request()
                                .newBuilder()
                                .header("User-Agent", "ExperienceFeed/1.0")
                                .header("Accept", "application/json")
                                .build()
                ))
                .addInterceptor(chain -> {
                    long startNanos = System.nanoTime();
                    okhttp3.Response response = chain.proceed(chain.request());
                    long durationMs = TimeUnit.NANOSECONDS.toMillis(
                            System.nanoTime() - startNanos
                    );
                    Log.d(TAG, response.code() + " " + durationMs + "ms "
                            + response.request().url());
                    return response;
                })
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        service = new Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherService.class);
    }

    void load(String city, Callback callback) {
        String normalizedCity = city == null ? "" : city.trim();
        if (normalizedCity.isEmpty()) {
            postError(callback, new IllegalArgumentException("地点不能为空"));
            return;
        }

        service.searchCity(GEOCODING_URL, normalizedCity, 1, "zh", "json")
                .enqueue(new retrofit2.Callback<GeocodingResponse>() {
                    @Override
                    public void onResponse(
                            Call<GeocodingResponse> call,
                            Response<GeocodingResponse> response
                    ) {
                        if (!response.isSuccessful()) {
                            postError(callback, httpError("城市查询", response.code()));
                            return;
                        }
                        GeocodingResponse body = response.body();
                        if (body == null || body.results == null || body.results.isEmpty()) {
                            postError(callback, new IOException("没有找到地点“" + normalizedCity + "”"));
                            return;
                        }
                        loadCurrent(body.results.get(0), callback);
                    }

                    @Override
                    public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                        postError(callback, throwable);
                    }
                });
    }

    private void loadCurrent(GeocodingResult location, Callback callback) {
        service.currentWeather(
                        FORECAST_URL,
                        location.latitude,
                        location.longitude,
                        CURRENT_FIELDS,
                        "auto"
                )
                .enqueue(new retrofit2.Callback<ForecastResponse>() {
                    @Override
                    public void onResponse(
                            Call<ForecastResponse> call,
                            Response<ForecastResponse> response
                    ) {
                        if (!response.isSuccessful()) {
                            postError(callback, httpError("天气查询", response.code()));
                            return;
                        }
                        ForecastResponse body = response.body();
                        if (body == null || body.current == null) {
                            postError(callback, new IOException("天气数据为空"));
                            return;
                        }
                        CurrentWeather current = body.current;
                        postSuccess(callback, new WeatherResult(
                                valueOr(location.name, "未知地点"),
                                areaLabel(location),
                                current.temperature,
                                current.apparentTemperature,
                                current.humidity,
                                current.windSpeed,
                                weatherCodeLabel(current.weatherCode),
                                timeLabel(current.time)
                        ));
                    }

                    @Override
                    public void onFailure(Call<ForecastResponse> call, Throwable throwable) {
                        postError(callback, throwable);
                    }
                });
    }

    private IOException httpError(String operation, int code) {
        return new IOException(operation + "失败，HTTP " + code);
    }

    private String areaLabel(GeocodingResult location) {
        String admin = valueOr(location.admin1, "");
        String country = valueOr(location.country, "");
        if (!admin.isEmpty() && !country.isEmpty()) {
            return admin + " · " + country;
        }
        if (!admin.isEmpty()) {
            return admin;
        }
        return country;
    }

    private String valueOr(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String timeLabel(String isoTime) {
        if (isoTime == null || isoTime.isEmpty()) {
            return "刚刚更新";
        }
        int separator = isoTime.indexOf('T');
        if (separator >= 0 && separator + 1 < isoTime.length()) {
            return isoTime.substring(separator + 1) + " 更新";
        }
        return isoTime + " 更新";
    }

    private String weatherCodeLabel(int code) {
        switch (code) {
            case 0:
                return "晴朗";
            case 1:
                return "大致晴朗";
            case 2:
                return "多云";
            case 3:
                return "阴";
            case 45:
            case 48:
                return "有雾";
            case 51:
            case 53:
            case 55:
                return "毛毛雨";
            case 56:
            case 57:
                return "冻毛毛雨";
            case 61:
            case 63:
            case 65:
                return "有雨";
            case 66:
            case 67:
                return "冻雨";
            case 71:
            case 73:
            case 75:
            case 77:
                return "有雪";
            case 80:
            case 81:
            case 82:
                return "阵雨";
            case 85:
            case 86:
                return "阵雪";
            case 95:
                return "雷雨";
            case 96:
            case 99:
                return "雷雨伴冰雹";
            default:
                return "未知天气";
        }
    }

    private void postSuccess(Callback callback, WeatherResult result) {
        mainHandler.post(() -> callback.onSuccess(result));
    }

    private void postError(Callback callback, Throwable throwable) {
        mainHandler.post(() -> callback.onError(throwable));
    }
}
