package com.quickbrief.app.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.quickbrief.app.auth.ProfileActivity;
import com.quickbrief.app.R;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private CheckBox rememberMeCheckbox;
    private AuthHelper authHelper;
    private SharedPreferences sharedPreferences;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER_ME = "remember_me";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authHelper = new AuthHelper();
        // setupGoogleSignIn(); // Temporarily disabled
        initializeViews();
        setupClickListeners();
        loadSavedCredentials();

        // Check if user is already logged in
        if (authHelper.isUserLoggedIn()) {
            startMainActivity();
            finish();
        }
    }

    private void setupGoogleSignIn() {
        // Temporarily disabled
        /*
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        authHelper.setGoogleSignInClient(googleSignInClient);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    try {
                        GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                .getResult(ApiException.class);
                        firebaseAuthWithGoogle(account);
                    } catch (ApiException e) {
                        Toast.makeText(this, "Google sign in failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
        */
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    }

    private void setupClickListeners() {
        MaterialButton loginButton = findViewById(R.id.loginButton);
        MaterialButton signupButton = findViewById(R.id.signupButton);
        MaterialButton googleSignInButton = findViewById(R.id.googleSignInButton);
        TextView forgotPasswordLink = findViewById(R.id.forgotPasswordLink);

        loginButton.setOnClickListener(v -> handleLogin());
        signupButton.setOnClickListener(v -> handleSignup());
        // googleSignInButton.setOnClickListener(v -> handleGoogleSignIn()); // Temporarily disabled
        if (googleSignInButton != null) {
            googleSignInButton.setEnabled(false); // Disable the button
        }
        forgotPasswordLink.setOnClickListener(v -> showForgotPasswordDialog());
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

    private void handleLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (validateInput(email, password)) {
            authHelper.login(email, password)
                    .addOnSuccessListener(authResult -> {
                        if (authHelper.isEmailVerified()) {
                            saveCredentials();
                            startMainActivity();
                            finish();
                        } else {
                            showEmailVerificationDialog();
                        }
                    })
                    .addOnFailureListener(e -> 
                        Toast.makeText(LoginActivity.this, 
                            "Login failed: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void handleSignup() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (validateInput(email, password)) {
            authHelper.signUp(email, password)
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(LoginActivity.this, 
                            "Account created successfully. Please verify your email.", 
                            Toast.LENGTH_LONG).show();
                        showEmailVerificationDialog();
                    })
                    .addOnFailureListener(e -> 
                        Toast.makeText(LoginActivity.this, 
                            "Signup failed: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void handleGoogleSignIn() {
        // Temporarily disabled
        /*
        Intent signInIntent = authHelper.getGoogleSignInClient().getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
        */
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        // Temporarily disabled
        /*
        authHelper.signInWithGoogle(account)
                .addOnSuccessListener(authResult -> {
                    startMainActivity();
                    finish();
                })
                .addOnFailureListener(e ->
                    Toast.makeText(LoginActivity.this,
                        "Google authentication failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show()
                );
        */
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
                    authHelper.sendPasswordResetEmail(email)
                            .addOnSuccessListener(unused ->
                                Toast.makeText(LoginActivity.this,
                                    "Password reset email sent",
                                    Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                Toast.makeText(LoginActivity.this,
                                    "Failed to send reset email: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEmailVerificationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Email Verification Required")
                .setMessage("Please verify your email address. Check your inbox for the verification link.")
                .setPositiveButton("Resend Email", (dialog, which) -> {
                    authHelper.sendEmailVerification()
                            .addOnSuccessListener(unused ->
                                Toast.makeText(LoginActivity.this,
                                    "Verification email sent",
                                    Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                Toast.makeText(LoginActivity.this,
                                    "Failed to send verification email: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("OK", (dialog, which) -> {
                    // Sign out the user since they haven't verified their email
                    authHelper.logout();
                })
                .setCancelable(false)
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
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
} 