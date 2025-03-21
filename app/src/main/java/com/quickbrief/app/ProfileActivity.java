package com.quickbrief.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "QuickBriefPrefs";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_BIO = "user_bio";
    
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView bioTextView;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText bioEditText;
    private Button editButton;
    private Button saveButton;
    private View displayLayout;
    private View editLayout;
    
    private MenuItem saveMenuItem;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.my_profile);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Initialize views
        initViews();
        
        // Load user data
        loadUserData();
        
        // Set up button listeners
        setupListeners();
    }
    
    private void initViews() {
        // Display mode views
        displayLayout = findViewById(R.id.displayLayout);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        bioTextView = findViewById(R.id.bioTextView);
        editButton = findViewById(R.id.editButton);
        
        // Edit mode views
        editLayout = findViewById(R.id.editLayout);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        bioEditText = findViewById(R.id.bioEditText);
        saveButton = findViewById(R.id.saveButton);
    }
    
    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Get user data from SharedPreferences
        String name = prefs.getString(KEY_NAME, "");
        String email = prefs.getString(KEY_EMAIL, "");
        String bio = prefs.getString(KEY_BIO, "");
        
        // Set data to display views
        nameTextView.setText(name.isEmpty() ? "Not set" : name);
        emailTextView.setText(email.isEmpty() ? "Not set" : email);
        bioTextView.setText(bio.isEmpty() ? "Not set" : bio);
        
        // Set data to edit views
        nameEditText.setText(name);
        emailEditText.setText(email);
        bioEditText.setText(bio);
    }
    
    private void saveUserData() {
        // Get data from edit texts
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String bio = bioEditText.getText().toString().trim();
        
        // Save to SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_BIO, bio);
        editor.apply();
        
        // Update display views
        nameTextView.setText(name.isEmpty() ? "Not set" : name);
        emailTextView.setText(email.isEmpty() ? "Not set" : email);
        bioTextView.setText(bio.isEmpty() ? "Not set" : bio);
        
        // Show success message
        Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
    }
    
    private void setupListeners() {
        // Edit button click listener
        editButton.setOnClickListener(v -> switchToEditMode());
        
        // Save button click listener
        saveButton.setOnClickListener(v -> {
            saveUserData();
            switchToDisplayMode();
        });
    }
    
    private void switchToEditMode() {
        isEditMode = true;
        displayLayout.setVisibility(View.GONE);
        editLayout.setVisibility(View.VISIBLE);
        
        // Show save menu item
        if (saveMenuItem != null) {
            saveMenuItem.setVisible(true);
        }
    }
    
    private void switchToDisplayMode() {
        isEditMode = false;
        displayLayout.setVisibility(View.VISIBLE);
        editLayout.setVisibility(View.GONE);
        
        // Hide save menu item
        if (saveMenuItem != null) {
            saveMenuItem.setVisible(false);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        saveMenuItem = menu.findItem(R.id.action_save);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_save) {
            saveUserData();
            switchToDisplayMode();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
} 