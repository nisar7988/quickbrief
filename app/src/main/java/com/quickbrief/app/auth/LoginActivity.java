package com.quickbrief.app.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.quickbrief.app.MainActivity;
import com.quickbrief.app.R;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private CheckBox rememberMeCheckbox;
    private CircularProgressIndicator progressBar;
    private MaterialButton loginButton;
    private MaterialButton signupButton;
    private SharedPreferences sharedPreferences;
    private boolean isLoading = false;

    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER_ME = "remember_me";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        setupClickListeners();
        loadSavedCredentials();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        progressBar = findViewById(R.id.progressBar);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);
    }

    private void setupClickListeners() {
        MaterialButton googleSignInButton = findViewById(R.id.googleSignInButton);
        TextView forgotPasswordLink = findViewById(R.id.forgotPasswordLink);

        loginButton.setOnClickListener(v -> handleLogin());
        signupButton.setOnClickListener(v -> handleSignup());
        
        if (googleSignInButton != null) {
            googleSignInButton.setEnabled(false); // Disable the button
        }
        
        if (forgotPasswordLink != null) {
            forgotPasswordLink.setOnClickListener(v -> showForgotPasswordDialog());
        }
    }

    private void loadSavedCredentials() {
        if (sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)) {
            emailEditText.setText(sharedPreferences.getString(KEY_EMAIL, ""));
            passwordEditText.setText(sharedPreferences.getString(KEY_PASSWORD, ""));
            rememberMeCheckbox.setChecked(true);
        }
    }

    private void saveCredentials() {
        if (rememberMeCheckbox.isChecked()) {
            sharedPreferences.edit()
                    .putString(KEY_EMAIL, emailEditText.getText().toString())
                    .putString(KEY_PASSWORD, passwordEditText.getText().toString())
                    .putBoolean(KEY_REMEMBER_ME, true)
                    .apply();
        } else {
            sharedPreferences.edit().clear().apply();
        }
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        if (loading) {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            loginButton.setEnabled(false);
            signupButton.setEnabled(false);
        } else {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            loginButton.setEnabled(true);
            signupButton.setEnabled(true);
        }
    }

    private void handleLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (validateInput(email, password)) {
            setLoading(true);
            
            // Simulate login process
            loginButton.postDelayed(() -> {
                setLoading(false);
                saveCredentials();
                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                startMainActivity();
                finish();
            }, 1000);
        }
    }

    private void handleSignup() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (validateInput(email, password)) {
            setLoading(true);
            
            // Simulate signup process
            signupButton.postDelayed(() -> {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                saveCredentials();
                startMainActivity();
                finish();
            }, 1000);
        }
    }

    private void showForgotPasswordDialog() {
        String email = emailEditText.getText().toString().trim();
        
        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Send password reset email to: " + 
                    (email.isEmpty() ? "your email" : email))
                .setPositiveButton("Send", (dialog, which) -> {
                    if (email.isEmpty()) {
                        emailEditText.setError("Please enter your email");
                        return;
                    }
                    Toast.makeText(LoginActivity.this,
                        "Password reset email sent to " + email,
                        Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            return false;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            return false;
        }
        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            return false;
        }
        return true;
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("isGuest", false);
        startActivity(intent);
    }
} 