package com.quickbrief.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import java.util.Locale;

/**
 * Utility class to manage language settings in the app
 */
public class LanguageManager {
    private static final String TAG = "LanguageManager";
    private static final String PREFS_NAME = "LanguagePrefs";
    private static final String KEY_LANGUAGE = "language_code";
    private static final String KEY_LANGUAGE_NAME = "language_name";
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String DEFAULT_LANGUAGE_NAME = "English";

    private final Context context;
    private final SharedPreferences preferences;

    public LanguageManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Set the app language
     * @param languageCode ISO 639-1 language code (e.g., "en", "es", "fr")
     * @param languageName Display name of the language (e.g., "English", "Spanish", "French")
     */
    public void setLanguage(String languageCode, String languageName) {
        preferences.edit()
                .putString(KEY_LANGUAGE, languageCode)
                .putString(KEY_LANGUAGE_NAME, languageName)
                .apply();
        
        Log.d(TAG, "Language set to: " + languageCode + " (" + languageName + ")");
    }

    /**
     * Get the current language code
     * @return ISO 639-1 language code
     */
    public String getLanguageCode() {
        return preferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }

    /**
     * Get the current language name
     * @return Display name of the language
     */
    public String getLanguageName() {
        return preferences.getString(KEY_LANGUAGE_NAME, DEFAULT_LANGUAGE_NAME);
    }

    /**
     * Apply the saved language to the app configuration
     * Note: This should be called in attachBaseContext() of your Application class
     * or in onCreate() of your activities before setContentView()
     */
    public Context applyLanguage(Context context) {
        String languageCode = getLanguageCode();
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
        
        Log.d(TAG, "Applied language: " + languageCode);
        return context;
    }
} 