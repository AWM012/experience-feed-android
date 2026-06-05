package com.bytecamp.experiencefeed;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

interface WeatherService {
    @GET
    Call<WeatherRepository.GeocodingResponse> searchCity(
            @Url String url,
            @Query("name") String city,
            @Query("count") int count,
            @Query("language") String language,
            @Query("format") String format
    );

    @GET
    Call<WeatherRepository.ForecastResponse> currentWeather(
            @Url String url,
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("current") String currentFields,
            @Query("timezone") String timezone
    );
}
