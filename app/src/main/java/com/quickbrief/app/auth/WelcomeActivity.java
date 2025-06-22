package com.quickbrief.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.quickbrief.app.R;
import com.quickbrief.app.MainActivity;
import com.quickbrief.app.SignupActivity;

public class WelcomeActivity extends AppCompatActivity {
    private static final String TAG = "WelcomeActivity";

    private MaterialButton loginButton;
    private MaterialButton signupButton;
    private MaterialButton continueAsGuestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting WelcomeActivity");
        
        try {
            setContentView(R.layout.activity_welcome);
            Log.d(TAG, "onCreate: Layout set successfully");
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Error setting layout", e);
            Toast.makeText(this, "Error loading layout: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // Initialize views
            loginButton = findViewById(R.id.loginButton);
            signupButton = findViewById(R.id.signupButton);
            continueAsGuestButton = findViewById(R.id.continueAsGuestButton);
            Log.d(TAG, "onCreate: Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Error finding views", e);
            Toast.makeText(this, "Error finding views: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        
        // Setup click listeners
        loginButton.setOnClickListener(v -> {
            Log.d(TAG, "Login button clicked");
            try {
                Toast.makeText(this, "Login button clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                Log.d(TAG, "Starting LoginActivity");
                startActivity(intent);
                Log.d(TAG, "LoginActivity started successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error starting LoginActivity", e);
                Toast.makeText(this, "Error opening login: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });

        signupButton.setOnClickListener(v -> {
            Log.d(TAG, "Signup button clicked");
            try {
                Toast.makeText(this, "Signup button clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, SignupActivity.class);
                Log.d(TAG, "Starting SignupActivity");
                startActivity(intent);
                Log.d(TAG, "SignupActivity started successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error starting SignupActivity", e);
                Toast.makeText(this, "Error opening signup: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });

        continueAsGuestButton.setOnClickListener(v -> {
            Log.d(TAG, "Continue as guest clicked");
            try {
                Toast.makeText(this, "Continue as guest clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("isGuest", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                Log.d(TAG, "Starting MainActivity as guest");
                startActivity(intent);
                Log.d(TAG, "MainActivity started successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error starting MainActivity", e);
                Toast.makeText(this, "Error opening main: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
        
        Log.d(TAG, "onCreate: WelcomeActivity setup completed successfully");
    }

    @Override
    public void onBackPressed() {
        // Show exit confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes", (dialog, which) -> {
                finish();
                System.exit(0);
            })
            .setNegativeButton("No", null)
            .show();
    }
} 