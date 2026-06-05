package com.bytecamp.experiencefeed;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Url;

interface ImageDownloadService {
    @GET
    Call<ResponseBody> download(
            @Url String imageUrl,
            @Header("Cache-Control") String cacheControl
    );
}
