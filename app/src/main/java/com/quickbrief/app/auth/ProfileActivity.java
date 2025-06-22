package com.quickbrief.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
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

        authHelper = new AuthHelper();
        loadUserData();
        setupLogoutButton();
        initializeViews();
    }

    private void setLoading(boolean loading) {
        if (loading) {
            progressIndicator.setVisibility(View.VISIBLE);
        } else {
            progressIndicator.setVisibility(View.GONE);
        }
    }

    private void loadUserData() {
        setLoading(true);
        FirebaseUser currentUser = authHelper.getCurrentUser();
        if (currentUser == null) {
            setLoading(false);
            startLoginActivity();
            return;
        }

        // Set basic user info
        userEmailTextView.setText(currentUser.getEmail());
        userIdTextView.setText(currentUser.getUid());
        
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
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        setLoading(false);
                        // Handle error
                    }
                });
    }

    private void updateUI(UserData userData) {
        // Format creation date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(new Date(userData.getCreatedAt()));
        
        // Update UI
        verificationStatusTextView.setText(userData.isEmailVerified() ? "Verified" : "Not Verified");
        createdAtTextView.setText(formattedDate);
    }

    private void setupLogoutButton() {
        MaterialButton logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            authHelper.logout();
            startLoginActivity();
        });
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initializeViews() {
        userEmailTextView = findViewById(R.id.userEmailTextView);
        userIdTextView = findViewById(R.id.userIdTextView);
        verificationStatusTextView = findViewById(R.id.verificationStatusTextView);
        createdAtTextView = findViewById(R.id.createdAtTextView);
        progressIndicator = findViewById(R.id.progressIndicator);
    }
} 