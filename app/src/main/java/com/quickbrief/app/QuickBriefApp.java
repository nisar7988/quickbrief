package com.quickbrief.app;

import android.app.Application;
import android.content.Context;

import com.quickbrief.app.util.LanguageManager;

public class QuickBriefApp extends Application {
    private LanguageManager languageManager;

    @Override
    public void onCreate() {
        super.onCreate();
        languageManager = new LanguageManager(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        languageManager = new LanguageManager(base);
        super.attachBaseContext(languageManager.applyLanguage(base));
    }
} 