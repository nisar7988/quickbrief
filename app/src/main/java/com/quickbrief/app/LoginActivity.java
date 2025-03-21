package com.quickbrief.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout emailLayout, passwordLayout;
    private EditText emailInput, passwordInput;
    private Button loginButton, guestButton;
    private TextView signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        guestButton = findViewById(R.id.guestButton);
        signupLink = findViewById(R.id.signupLink);

        // Set click listeners
        loginButton.setOnClickListener(v -> attemptLogin());
        guestButton.setOnClickListener(v -> startMainActivity(true));
        signupLink.setOnClickListener(v -> startSignupActivity());
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Basic validation
        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            return;
        }
        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            return;
        }

        // TODO: Implement actual login logic
        // For now, just accept any input
        startMainActivity(false);
    }

    private void startMainActivity(boolean isGuest) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("isGuest", isGuest);
        startActivity(intent);
        if (!isGuest) {
            finish(); // Don't finish if guest mode (allow going back to login)
        }
    }

    private void startSignupActivity() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
} 