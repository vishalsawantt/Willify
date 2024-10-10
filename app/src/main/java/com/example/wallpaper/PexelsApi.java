package com.example.wallpaper;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface PexelsApi {
    @GET("curated")
    Call<PhotoResponse> getCuratedPhotos(@Header("Authorization") String apiKey,
                                         @Query("page") int page,
                                         @Query("per_page") int perPage);

    @GET("search")
    Call<PhotoResponse> searchPhotos(@Header("Authorization") String apiKey,
                                     @Query("query") String query,
                                     @Query("page") int page,
                                     @Query("per_page") int perPage);

}

