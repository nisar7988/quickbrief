package com.quickbrief.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.quickbrief.app.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private AuthHelper authHelper;
    private TextView userEmailTextView;
    private TextView userIdTextView;
    private TextView verificationStatusTextView;
    private TextView createdAtTextView;
    private CircularProgressIndicator progressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Setup toolbar
        setupToolbar();
        
        // Initialize views first
        initializeViews();
        
        // Initialize AuthHelper
        authHelper = new AuthHelper();
        
        // Setup logout button
        setupLogoutButton();
        
        // Load user data after views are initialized
        loadUserData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.my_profile);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void setLoading(boolean loading) {
        if (progressIndicator != null) {
            if (loading) {
                progressIndicator.setVisibility(View.VISIBLE);
            } else {
                progressIndicator.setVisibility(View.GONE);
            }
        }
    }

    private void loadUserData() {
        setLoading(true);
        
        try {
            FirebaseUser currentUser = authHelper.getCurrentUser();
            if (currentUser == null) {
                setLoading(false);
                Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
                startLoginActivity();
                return;
            }

            // Set basic user info
            if (userEmailTextView != null) {
                userEmailTextView.setText("Email: " + (currentUser.getEmail() != null ? currentUser.getEmail() : "N/A"));
            }
            if (userIdTextView != null) {
                userIdTextView.setText("User ID: " + currentUser.getUid());
            }
            
            // Get additional user data from Realtime Database
            authHelper.getUsersRef().child(currentUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            setLoading(false);
                            if (dataSnapshot.exists()) {
                                UserData userData = dataSnapshot.getValue(UserData.class);
                                if (userData != null) {
                                    updateUI(userData);
                                } else {
                                    // Handle case where userData is null
                                    if (verificationStatusTextView != null) {
                                        verificationStatusTextView.setText("Verification: Unknown");
                                    }
                                    if (createdAtTextView != null) {
                                        createdAtTextView.setText("Created: Unknown");
                                    }
                                }
                            } else {
                                // Handle case where user data doesn't exist in database
                                if (verificationStatusTextView != null) {
                                    verificationStatusTextView.setText("Verification: Unknown");
                                }
                                if (createdAtTextView != null) {
                                    createdAtTextView.setText("Created: Unknown");
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            setLoading(false);
                            Toast.makeText(ProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                            // Set default values
                            if (verificationStatusTextView != null) {
                                verificationStatusTextView.setText("Verification: Unknown");
                            }
                            if (createdAtTextView != null) {
                                createdAtTextView.setText("Created: Unknown");
                            }
                        }
                    });
        } catch (Exception e) {
            setLoading(false);
            Toast.makeText(this, "Error loading user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            startLoginActivity();
        }
    }

    private void updateUI(UserData userData) {
        try {
            // Format creation date
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            String formattedDate = sdf.format(new Date(userData.getCreatedAt()));
            
            // Update UI with null checks
            if (verificationStatusTextView != null) {
                verificationStatusTextView.setText("Verification: " + (userData.isEmailVerified() ? "Verified" : "Not Verified"));
            }
            if (createdAtTextView != null) {
                createdAtTextView.setText("Created: " + formattedDate);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error updating UI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupLogoutButton() {
        MaterialButton logoutButton = findViewById(R.id.logoutButton);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                try {
                    authHelper.logout();
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    startLoginActivity();
                } catch (Exception e) {
                    Toast.makeText(this, "Error logging out: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void startLoginActivity() {
        try {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error starting login activity: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        try {
            userEmailTextView = findViewById(R.id.userEmailTextView);
            userIdTextView = findViewById(R.id.userIdTextView);
            verificationStatusTextView = findViewById(R.id.verificationStatusTextView);
            createdAtTextView = findViewById(R.id.createdAtTextView);
            progressIndicator = findViewById(R.id.progressIndicator);
            
            // Set default text for views
            if (userEmailTextView != null) {
                userEmailTextView.setText("Email: Loading...");
            }
            if (userIdTextView != null) {
                userIdTextView.setText("User ID: Loading...");
            }
            if (verificationStatusTextView != null) {
                verificationStatusTextView.setText("Verification: Loading...");
            }
            if (createdAtTextView != null) {
                createdAtTextView.setText("Created: Loading...");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 