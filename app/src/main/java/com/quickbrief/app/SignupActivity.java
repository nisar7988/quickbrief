package com.quickbrief.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;

public class SignupActivity extends AppCompatActivity {
    private TextInputLayout nameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private EditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button signupButton;
    private TextView loginLink;
    private CircularProgressIndicator progressBar;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        
        signupButton = findViewById(R.id.signupButton);
        loginLink = findViewById(R.id.loginLink);
        progressBar = findViewById(R.id.progressBar);

        // Set click listeners
        signupButton.setOnClickListener(v -> attemptSignup());
        loginLink.setOnClickListener(v -> finish());
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        if (loading) {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            signupButton.setEnabled(false);
        } else {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            signupButton.setEnabled(true);
        }
    }

    private void attemptSignup() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Basic validation
        if (name.isEmpty()) {
            nameLayout.setError("Name is required");
            return;
        }
        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            return;
        }
        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            return;
        }
        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.setError("Please confirm your password");
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            return;
        }

        setLoading(true);

        // Simulate signup process
        signupButton.postDelayed(() -> {
            setLoading(false);
            Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
            
            // Redirect to MainActivity
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            intent.putExtra("isGuest", false);
            startActivity(intent);
            finish();
        }, 1000);
    }
}