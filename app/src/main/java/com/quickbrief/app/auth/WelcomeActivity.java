package com.quickbrief.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.quickbrief.app.R;
import com.quickbrief.app.MainActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button loginButton = findViewById(R.id.loginButton);
        Button signupButton = findViewById(R.id.signupButton);
        Button continueAsGuestButton = findViewById(R.id.continueAsGuestButton);

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.quickbrief.app.SignupActivity.class);
            startActivity(intent);
        });

        continueAsGuestButton.setOnClickListener(v -> {
            // Start MainActivity directly without authentication
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("isGuest", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
} 