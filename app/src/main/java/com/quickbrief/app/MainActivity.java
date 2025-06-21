package com.quickbrief.app;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.quickbrief.app.adapter.NewsAdapter;
import com.quickbrief.app.api.NewsApiClient;
import com.quickbrief.app.model.News;
import com.quickbrief.app.model.NewsApiResponse;
import com.quickbrief.app.util.LanguageManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CircularProgressIndicator progressBar;
    private ChipGroup categoryChipGroup;
    private List<News> newsList;
    private boolean isGuest;
    
    // Category mapping
    private Map<Integer, String> categoryMap = new HashMap<>();
    private String currentCategory = "general";
    
    // Language settings
    private LanguageManager languageManager;
    private String currentLanguage;
    
    // Pagination variables
    private int currentPage = 1;
    private final int PAGE_SIZE = 10; // Increased from 3 to a more reasonable value
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize language manager
        languageManager = new LanguageManager(this);
        currentLanguage = languageManager.getLanguageCode();
        
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Starting app initialization");
        
        // Check API key
        checkApiKey();
        
        // Get intent extras
        isGuest = getIntent().getBooleanExtra("isGuest", false);

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        newsRecyclerView = findViewById(R.id.newsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        categoryChipGroup = findViewById(R.id.categoryChipGroup);

        // Initialize category map
        initCategoryMap();
        Log.d(TAG, "onCreate: Category map initialized");

        // Add a button to manually load more news (for testing)
        findViewById(R.id.btnLoadMore).setOnClickListener(v -> {
            Log.d(TAG, "Load More button clicked");
            // Force reset loading state
            isLoading = false;
            loadMoreNews();
        });
        Log.d(TAG, "onCreate: Load more button setup complete");

        // Setup RecyclerView
        newsList = new ArrayList<>();
        newsAdapter = new NewsAdapter(this, newsList);
        layoutManager = new LinearLayoutManager(this);
        newsRecyclerView.setLayoutManager(layoutManager);
        newsRecyclerView.setAdapter(newsAdapter);
        Log.d(TAG, "onCreate: RecyclerView setup complete");

        // Setup category selection listener
        categoryChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != View.NO_ID) {
                currentCategory = categoryMap.get(checkedId);
                Log.d(TAG, "Category selected: " + currentCategory);
                // Reset pagination and fetch news for the selected category
                resetAndFetchNews();
            }
        });

        // Setup scroll listener for pagination
        newsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                // Only trigger loading more when scrolling down
                if (dy <= 0) return;
                
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

                Log.d(TAG, "onScrolled: visibleItemCount=" + visibleItemCount + 
                      ", totalItemCount=" + totalItemCount + 
                      ", firstVisibleItemPosition=" + firstVisibleItemPosition + 
                      ", lastVisibleItemPosition=" + lastVisibleItemPosition +
                      ", isLoading=" + isLoading + 
                      ", isLastPage=" + isLastPage);

                // Check if we need to load more data
                // Load more when user is near the end (2 items before the end)
                if (!isLoading && !isLastPage) {
                    if (lastVisibleItemPosition >= totalItemCount - 3) {
                        Log.d(TAG, "onScrolled: Near the end, loading more news");
                        loadMoreNews();
                    }
                }
            }
        });

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(R.color.accent_blue);
        swipeRefreshLayout.setOnRefreshListener(this::resetAndFetchNews);

        // Initial news fetch
        fetchNews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_menu) {
            showMainMenu();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showMainMenu() {
        // Inflate the custom menu layout
        View menuView = getLayoutInflater().inflate(R.layout.custom_menu_layout, null);
        
        // Create a dialog with our custom style
        Dialog menuDialog = new Dialog(this, R.style.CustomMenuDialog);
        menuDialog.setContentView(menuView);
        
        // Set up click listeners for menu items
        menuView.findViewById(R.id.menu_profile).setOnClickListener(v -> {
            menuDialog.dismiss();
            // Navigate to profile screen
            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });
        
        menuView.findViewById(R.id.menu_language).setOnClickListener(v -> {
            menuDialog.dismiss();
            showLanguageSelectionDialog();
        });
        
        menuView.findViewById(R.id.menu_categories).setOnClickListener(v -> {
            menuDialog.dismiss();
            showCategoriesDialog();
        });
        
        menuView.findViewById(R.id.menu_logout).setOnClickListener(v -> {
            menuDialog.dismiss();
            logout();
        });
        
        // Show the dialog
        menuDialog.show();
        
        // Set the dialog to take up the full width and position it at the top
        Window window = menuDialog.getWindow();
        if (window != null) {
            // Find the toolbar and menu icon
            Toolbar toolbar = findViewById(R.id.toolbar);
            View menuIcon = toolbar.findViewById(R.id.action_menu);
            
            // Set layout parameters
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.TOP);
            
            // Position the menu right below the toolbar
            int statusBarHeight = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
            
            int toolbarHeight = toolbar.getHeight();
            
            // Set the y position to be right at the bottom of the toolbar
            window.getAttributes().y = statusBarHeight + toolbarHeight;
            
            // Remove the top shadow since we're positioning it exactly at the toolbar bottom
            View topShadow = menuView.findViewById(R.id.top_shadow);
            if (topShadow != null) {
                topShadow.setVisibility(View.GONE);
            }
        }
    }

    private void showLanguageSelectionDialog() {
        // Get language arrays from resources
        String[] languageNames = getResources().getStringArray(R.array.language_names);
        String[] languageCodes = getResources().getStringArray(R.array.language_codes);
        
        // Find the current language index
        int currentIndex = 0;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLanguage)) {
                currentIndex = i;
                break;
            }
        }
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle(R.string.select_language)
            .setSingleChoiceItems(languageNames, currentIndex, (dialog, which) -> {
                // Update language preference
                String selectedLanguageCode = languageCodes[which];
                String selectedLanguageName = languageNames[which];
                
                // Only update if language changed
                if (!selectedLanguageCode.equals(currentLanguage)) {
                    // Save the selected language
                    languageManager.setLanguage(selectedLanguageCode, selectedLanguageName);
                    currentLanguage = selectedLanguageCode;
                    
                    // Show toast message
                    Toast.makeText(this, 
                        getString(R.string.language_changed, selectedLanguageName), 
                        Toast.LENGTH_SHORT).show();
                    
                    // Refresh news with the new language
                    resetAndFetchNews();
                    
                    // Inform user about restart for UI language change
                    Toast.makeText(this, 
                        R.string.language_restart_required, 
                        Toast.LENGTH_LONG).show();
                }
                
                dialog.dismiss();
            });
        
        builder.show();
    }

    private void showCategoriesDialog() {
        // Get category arrays from resources
        String[] categoryNames = getResources().getStringArray(R.array.category_names);
        String[] categoryCodes = getResources().getStringArray(R.array.category_codes);
        
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle(R.string.select_category)
            .setItems(categoryNames, (dialog, which) -> {
                String categoryCode = categoryCodes[which];
                String categoryName = categoryNames[which];
                
                Toast.makeText(this, "Selected: " + categoryName, Toast.LENGTH_SHORT).show();
                
                // Find the corresponding chip and select it
                int chipId = -1;
                for (Map.Entry<Integer, String> entry : categoryMap.entrySet()) {
                    if (entry.getValue().equals(categoryCode)) {
                        chipId = entry.getKey();
                        break;
                    }
                }
                
                if (chipId != -1) {
                    categoryChipGroup.check(chipId);
                }
            });
        
        builder.show();
    }

    private void logout() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.yes, (dialog, which) -> {
                Toast.makeText(this, R.string.logging_out, Toast.LENGTH_SHORT).show();
                // Navigate to login screen
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton(R.string.no, null);
        
        builder.show();
    }

    private void initCategoryMap() {
        categoryMap.put(R.id.chipGeneral, "general");
        categoryMap.put(R.id.chipSports, "sports");
        categoryMap.put(R.id.chipEntertainment, "entertainment");
        categoryMap.put(R.id.chipBusiness, "business");
        categoryMap.put(R.id.chipTechnology, "technology");
        categoryMap.put(R.id.chipCrime, "crime");
    }

    private void resetAndFetchNews() {
        currentPage = 1;
        isLastPage = false;
        isLoading = false;
        newsList.clear();
        newsAdapter.notifyDataSetChanged();
        fetchNews();
    }

    private void fetchNews() {
        if (isLoading) {
            Log.d(TAG, "fetchNews: Already loading, skipping request");
            return;
        }
        
        isLoading = true;
        Log.d(TAG, "fetchNews: currentPage=" + currentPage + 
              ", category=" + currentCategory + 
              ", language=" + currentLanguage);
        
        if (currentPage == 1) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "fetchNews: Adding loading footer");
            newsAdapter.addLoadingFooter();
            
            // Scroll to the loading footer
            if (newsList.size() > 0) {
                newsRecyclerView.smoothScrollToPosition(newsAdapter.getItemCount() - 1);
            }
        }

        // Get news based on language
        Call<NewsApiResponse> call;
        
        // For some languages, top headlines might not work well, so use 'everything' endpoint
        // with a general query for those languages
        if (currentLanguage.equals("ar") || currentLanguage.equals("zh") || 
            currentLanguage.equals("ja") || currentLanguage.equals("ru")) {
            // Use 'everything' endpoint with a general query for these languages
            call = NewsApiClient.getInstance()
                .getNewsApiService()
                .getEverythingByLanguage("news", currentLanguage, "publishedAt", 
                                        currentPage, PAGE_SIZE, NewsApiClient.getApiKey());
            Log.d(TAG, "fetchNews: Using 'everything' endpoint for language: " + currentLanguage);
        } else {
            // Use 'top-headlines' endpoint for other languages
            call = NewsApiClient.getInstance()
                .getNewsApiService()
                .getTopHeadlinesByLanguage(currentLanguage, currentCategory, 
                                          currentPage, PAGE_SIZE, NewsApiClient.getApiKey());
            Log.d(TAG, "fetchNews: Using 'top-headlines' endpoint for language: " + currentLanguage);
        }
        
        call.enqueue(new Callback<NewsApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsApiResponse> call, @NonNull Response<NewsApiResponse> response) {
                if (currentPage != 1) {
                    Log.d(TAG, "onResponse: Removing loading footer");
                    newsAdapter.removeLoadingFooter();
                } else {
                    progressBar.setVisibility(View.GONE);
                }
                
                swipeRefreshLayout.setRefreshing(false);
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    NewsApiResponse apiResponse = response.body();
                    Log.d(TAG, "onResponse: Status=" + apiResponse.getStatus() + 
                          ", TotalResults=" + apiResponse.getTotalResults() +
                          ", CurrentPage=" + currentPage);
                    List<News> newsItems = new ArrayList<>();
                    if (apiResponse.getResults() != null) { // Corrected from getArticles() to getResults() previously, ensuring it's results now
                        // Change getArticles() to getResults() to match NewsData.io response
                        for (NewsApiResponse.Article article : apiResponse.getResults()) {
                            if (article.getTitle() != null && article.getDescription() != null) {
                                newsItems.add(new News(
                                    article.getTitle(),
                                    article.getDescription(),
 article.getImage_url(), // Changed from getUrlToImage() to getImage_url()
                                    article.getSource() != null ? article.getSource().getName() : "Unknown",
                                    article.getUrl()
                                ));
                            }
                        }
                    }
                    
                    Log.d(TAG, "onResponse: Received " + newsItems.size() + " news items for page " + currentPage);
                    
                    // Check if this is the last page
                    if (newsItems.isEmpty() || newsItems.size() < PAGE_SIZE) {
                        isLastPage = true;
                        Log.d(TAG, "onResponse: This is the last page (page " + currentPage + ")");
                        
                        // Show a toast if we've reached the end
                        if (currentPage > 1) {
                            Toast.makeText(MainActivity.this, 
                                R.string.end_of_news_feed, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        isLastPage = false;
                        Log.d(TAG, "onResponse: More pages available after page " + currentPage);
                    }
                    
                    if (currentPage == 1) {
                        if (newsItems.isEmpty()) {
                            Toast.makeText(MainActivity.this, 
                                getString(R.string.no_news_available, currentCategory), 
                                Toast.LENGTH_SHORT).show();
                            
                            // If no news found with top-headlines, try 'everything' endpoint
                            if (!call.request().url().toString().contains("everything")) {
                                Log.d(TAG, "onResponse: No news found with top-headlines, trying 'everything' endpoint");
                                // Try with 'everything' endpoint
                                NewsApiClient.getInstance()
                                    .getNewsApiService()
                                    .getEverythingByLanguage("news", currentLanguage, "publishedAt", 
                                                            currentPage, PAGE_SIZE, NewsApiClient.getApiKey())
                                    .enqueue(this);
                                return;
                            }
                        } else {
                            newsAdapter.updateNews(newsItems);
                        }
                    } else {
                        newsAdapter.addNews(newsItems);
                    }
                } else {
                    String errorMsg = getString(R.string.error_fetching_news);
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            errorMsg += ": " + errorBody;
                            
                            // Check for common NewsAPI error codes
                            if (response.code() == 429) {
                                errorMsg = "Rate limit exceeded. Please try again later.";
                                // Mark as last page to prevent further requests
                                isLastPage = true;
                            } else if (response.code() == 401) {
                                errorMsg = "API key error. Please check your API key.";
                                isLastPage = true;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "onResponse: " + errorMsg + ", code: " + response.code());
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsApiResponse> call, @NonNull Throwable t) {
                if (currentPage != 1) {
                    Log.d(TAG, "onFailure: Removing loading footer");
                    newsAdapter.removeLoadingFooter();
                } else {
                    progressBar.setVisibility(View.GONE);
                }
                
                swipeRefreshLayout.setRefreshing(false);
                isLoading = false;
                Log.e(TAG, "onFailure: Network error", t);
                Toast.makeText(MainActivity.this, 
                    getString(R.string.error_network, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadMoreNews() {
        if (isLoading || isLastPage) {
            Log.d(TAG, "loadMoreNews: Skip loading more - isLoading=" + isLoading + ", isLastPage=" + isLastPage);
            return;
        }
        
        Log.d(TAG, "loadMoreNews: Starting to load more news");
        isLoading = true;
        currentPage++;
        Log.d(TAG, "loadMoreNews: Loading page " + currentPage);
        
        // Add a small delay to prevent rapid multiple calls
        newsRecyclerView.postDelayed(() -> {
            fetchNews();
        }, 300);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reset loading state when activity resumes
        isLoading = false;
        Log.d(TAG, "onResume: Reset isLoading to false");
    }

    /**
     * Check if the API key is valid
     */
    private void checkApiKey() {
        String apiKey = NewsApiClient.getApiKey();
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_API_KEY_HERE")) {
            Log.e(TAG, "Invalid API key: " + apiKey);
            Toast.makeText(this, "Invalid API key. Please set a valid NewsAPI key.", Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "API key is set: " + apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4));
        }
    }
} 