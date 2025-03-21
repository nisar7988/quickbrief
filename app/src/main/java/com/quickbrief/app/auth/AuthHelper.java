package com.quickbrief.app.auth;

import android.app.Activity;
import androidx.annotation.NonNull;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AuthHelper {
    private final FirebaseAuth mAuth;
    private final DatabaseReference usersRef;
    private GoogleSignInClient googleSignInClient;

    public AuthHelper() {
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public Task<AuthResult> signUp(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        // Send email verification
                        user.sendEmailVerification();
                        
                        // Save user data to Realtime Database
                        UserData userData = new UserData(
                            user.getUid(),
                            user.getEmail(),
                            false // emailVerified initially false
                        );
                        usersRef.child(user.getUid()).setValue(userData);
                    }
                });
    }

    public Task<AuthResult> login(String email, String password) {
        return mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null && user.isEmailVerified()) {
                        // Update email verification status in database
                        usersRef.child(user.getUid()).child("emailVerified").setValue(true);
                    }
                });
    }

    public boolean isEmailVerified() {
        FirebaseUser user = getCurrentUser();
        return user != null && user.isEmailVerified();
    }

    public Task<Void> sendEmailVerification() {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            return user.sendEmailVerification();
        }
        return null;
    }

    public Task<Void> sendPasswordResetEmail(String email) {
        return mAuth.sendPasswordResetEmail(email);
    }

    public Task<AuthResult> signInWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        return mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        // Save Google user data to database
                        UserData userData = new UserData(
                            user.getUid(),
                            user.getEmail(),
                            true  // Google accounts are pre-verified
                        );
                        usersRef.child(user.getUid()).setValue(userData);
                    }
                });
    }

    public void setGoogleSignInClient(GoogleSignInClient client) {
        this.googleSignInClient = client;
    }

    public GoogleSignInClient getGoogleSignInClient() {
        return googleSignInClient;
    }

    public void logout() {
        if (googleSignInClient != null) {
            googleSignInClient.signOut();
        }
        mAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    public DatabaseReference getUsersRef() {
        return usersRef;
    }
} 