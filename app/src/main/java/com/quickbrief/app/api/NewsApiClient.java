package com.quickbrief.app.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewsApiClient {
    private static final String BASE_URL = "https://newsapi.org/";
    private static final String API_KEY = "fb11adb3d0d849279d8423ae67d39f4f";
    private static NewsApiClient instance;
    private final NewsApiService newsApiService;

    private NewsApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        newsApiService = retrofit.create(NewsApiService.class);
    }

    public static synchronized NewsApiClient getInstance() {
        if (instance == null) {
            instance = new NewsApiClient();
        }
        return instance;
    }

    public NewsApiService getNewsApiService() {
        return newsApiService;
    }

    public static String getApiKey() {
        return API_KEY;
    }
} 