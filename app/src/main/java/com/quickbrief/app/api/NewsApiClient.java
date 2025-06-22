package com.quickbrief.app.api;

import android.util.Log;
import com.quickbrief.app.BuildConfig;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewsApiClient {
    private static final String TAG = "NewsApiClient";
    private static final String BASE_URL = "https://newsdata.io/api/";
    private static final String API_KEY = BuildConfig.NEWS_API_KEY;
    private static NewsApiClient instance;
    private final NewsApiService newsApiService;

    private NewsApiClient() {
        Log.d(TAG, "NewsApiClient: Initializing with BASE_URL=" + BASE_URL);
        Log.d(TAG, "NewsApiClient: API_KEY=" + (API_KEY != null ? API_KEY.substring(0, Math.min(4, API_KEY.length())) + "..." : "null"));
        
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
        Log.d(TAG, "getApiKey: Returning API key: " + (API_KEY != null ? API_KEY.substring(0, Math.min(4, API_KEY.length())) + "..." : "null"));
        return API_KEY;
    }
} 