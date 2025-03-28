package com.quickbrief.app.api;

import com.quickbrief.app.model.NewsApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {
    @GET("v2/top-headlines")
    Call<NewsApiResponse> getTopHeadlines(
        @Query("country") String country,
        @Query("category") String category,
        @Query("page") int page,
        @Query("pageSize") int pageSize,
        @Query("apiKey") String apiKey
    );
    
    @GET("v2/top-headlines")
    Call<NewsApiResponse> getTopHeadlinesByLanguage(
        @Query("language") String language,
        @Query("category") String category,
        @Query("page") int page,
        @Query("pageSize") int pageSize,
        @Query("apiKey") String apiKey
    );

    @GET("v2/everything")
    Call<NewsApiResponse> getEverything(
        @Query("q") String query,
        @Query("sortBy") String sortBy,
        @Query("page") int page,
        @Query("pageSize") int pageSize,
        @Query("apiKey") String apiKey
    );
    
    @GET("v2/everything")
    Call<NewsApiResponse> getEverythingByLanguage(
        @Query("q") String query,
        @Query("language") String language,
        @Query("sortBy") String sortBy,
        @Query("page") int page,
        @Query("pageSize") int pageSize,
        @Query("apiKey") String apiKey
    );
} 