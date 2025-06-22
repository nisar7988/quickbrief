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
import com.quickbrief.app.auth.LoginActivity;
import com.quickbrief.app.auth.ProfileActivity;
import com.quickbrief.app.model.News;
import com.quickbrief.app.model.NewsApiResponse;
import com.quickbrief.app.util.LanguageManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

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
    
    // Category and language settings
    private String currentCategory = "top";
    
    // Language settings
    private LanguageManager languageManager;
    private String currentLanguage;
    
    // Pagination variables
    private String nextPage = null;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isGuest = getIntent().getBooleanExtra("isGuest", false);

        // Initialize language manager
        languageManager = new LanguageManager(this);
        currentLanguage = languageManager.getLanguageCode();
        
        // Check API key for debugging
        checkApiKey();

        // Initialize views
        initializeViews();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup SwipeRefreshLayout
        setupSwipeRefreshLayout();
        
        // Setup category chips
        setupCategoryChips();
        
        // Fetch initial news
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
                
                // Update current category and refresh news
                currentCategory = categoryCode;
                resetAndFetchNews();
                
                // Find the corresponding chip and select it
                for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) categoryChipGroup.getChildAt(i);
                    if (chip.getText().toString().equals(categoryName)) {
                        chip.setChecked(true);
                        break;
                    }
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

    private void resetAndFetchNews() {
        nextPage = null;
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
        Log.d(TAG, "fetchNews: nextPage=" + nextPage);
        
        // Log API key for debugging
        String apiKey = NewsApiClient.getApiKey();
        Log.d(TAG, "fetchNews: API Key length=" + (apiKey != null ? apiKey.length() : 0));
        
        // Log and show the current category for debugging
        Log.d(TAG, "fetchNews: Using category=" + currentCategory);
        Toast.makeText(this, "Category: " + currentCategory, Toast.LENGTH_SHORT).show();

        if (nextPage == null) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "fetchNews: Adding loading footer");
            newsAdapter.addLoadingFooter();
            
            // Scroll to the loading footer
            if (newsList.size() > 0) {
                newsRecyclerView.smoothScrollToPosition(newsAdapter.getItemCount() - 1);
            }
        }

        // Use the /1/news endpoint with cursor-based pagination
        Call<NewsApiResponse> call = NewsApiClient.getInstance()
            .getNewsApiService()
            .getNews("en", currentCategory, nextPage, NewsApiClient.getApiKey());
        Log.d(TAG, "fetchNews: Making API call with URL: " + call.request().url());
        
        call.enqueue(new Callback<NewsApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsApiResponse> call, @NonNull Response<NewsApiResponse> response) {
                Log.d(TAG, "onResponse: Response code=" + response.code() + ", isSuccessful=" + response.isSuccessful());
                
                if (nextPage != null) {
                    Log.d(TAG, "onResponse: Removing loading footer");
                    newsAdapter.removeLoadingFooter();
                } else {
                    progressBar.setVisibility(View.GONE);
                }
                
                swipeRefreshLayout.setRefreshing(false);
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    NewsApiResponse apiResponse = response.body();
                    Log.d(TAG, "onResponse: Status=" + apiResponse.getStatus() + ", TotalResults=" + apiResponse.getTotalResults());
                    List<News> newsItems = new ArrayList<>();
                    if (apiResponse.getResults() != null) {
                        Log.d(TAG, "onResponse: API returned " + apiResponse.getResults().size() + " articles");
                        for (NewsApiResponse.Article article : apiResponse.getResults()) {
                            if (article.getTitle() != null && article.getDescription() != null) {
                                newsItems.add(new News(
                                    article.getTitle(),
                                    article.getDescription(),
                                    article.getImageUrl(),
                                    article.getSource() != null ? article.getSource().getName() : "Unknown",
                                    article.getUrl()
                                ));
                            }
                        }
                    } else {
                        Log.d(TAG, "onResponse: API returned null results");
                    }
                    
                    // Update nextPage for cursor-based pagination
                    nextPage = apiResponse.getNextPage();
                    Log.d(TAG, "onResponse: nextPage for next request = " + nextPage);
                    
                    // Pagination logic
                    if (newsItems.isEmpty() || nextPage == null) {
                        isLastPage = true;
                        Log.d(TAG, "onResponse: Reached last page - no more news available");
                        if (nextPage != null) {
                            Toast.makeText(MainActivity.this, getString(R.string.end_of_news_feed), Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    if (thisIsFirstPage()) {
                        newsAdapter.updateNews(newsItems);
                    } else {
                        newsAdapter.addNews(newsItems);
                    }
                } else {
                    String errorMsg = getString(R.string.error_fetching_news);
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            errorMsg += ": " + errorBody;
                            Log.e(TAG, "onResponse: Error body: " + errorBody);
                            Toast.makeText(MainActivity.this, errorBody, Toast.LENGTH_LONG).show();
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
                Log.e(TAG, "onFailure: Network error", t);
                Log.e(TAG, "onFailure: Request URL: " + call.request().url());
                
                if (nextPage != null) {
                    Log.d(TAG, "onFailure: Removing loading footer");
                    newsAdapter.removeLoadingFooter();
                } else {
                    progressBar.setVisibility(View.GONE);
                }
                
                swipeRefreshLayout.setRefreshing(false);
                isLoading = false;
                Toast.makeText(MainActivity.this, getString(R.string.error_network, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean thisIsFirstPage() {
        return nextPage == null || newsList.isEmpty();
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

    private void initializeViews() {
        newsRecyclerView = findViewById(R.id.newsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        newsList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        newsRecyclerView.setLayoutManager(layoutManager);
        newsAdapter = new NewsAdapter(this, newsList);
        newsRecyclerView.setAdapter(newsAdapter);
        
        // Add scroll listener for pagination
        newsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                
                // Trigger loading when user is 3 items away from the bottom
                if (!isLoading && !isLastPage && dy > 0) { // Only trigger on scroll down
                    if (lastVisibleItemPosition >= totalItemCount - 3 && totalItemCount > 0) {
                        Log.d(TAG, "onScrolled: Triggering load more - lastVisible=" + lastVisibleItemPosition + 
                              ", total=" + totalItemCount + ", isLoading=" + isLoading + ", isLastPage=" + isLastPage);
                        loadMoreNews();
                    }
                }
            }
        });
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            resetAndFetchNews();
        });
    }

    private void setupCategoryChips() {
        // Create category chips with valid NewsData.io categories
        String[] categories = {"top", "business", "technology", "sports", "entertainment", "health", "science"};
        String[] categoryNames = {"Top", "Business", "Technology", "Sports", "Entertainment", "Health", "Science"};
        
        for (int i = 0; i < categories.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(categoryNames[i]);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            
            // Set the first chip (Top) as checked by default
            if (i == 0) {
                chip.setChecked(true);
            }
            
            final String category = categories[i];
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    currentCategory = category;
                    resetAndFetchNews();
                }
            });
            
            categoryChipGroup.addView(chip);
        }
    }

    private void loadMoreNews() {
        if (isLoading || isLastPage) {
            Log.d(TAG, "loadMoreNews: Skip loading more - isLoading=" + isLoading + ", isLastPage=" + isLastPage);
            return;
        }
        
        Log.d(TAG, "loadMoreNews: Starting to load more news with nextPage=" + nextPage);
        isLoading = true;
        
        // Add a small delay to prevent rapid multiple calls and show loading footer
        newsRecyclerView.postDelayed(() -> {
            if (!isLoading) {
                Log.d(TAG, "loadMoreNews: Loading was cancelled, skipping");
                return;
            }
            fetchNews();
        }, 200);
    }
} 