package com.quickbrief.app.auth;

public class UserData {
    private String uid;
    private String email;
    private boolean emailVerified;
    private long createdAt;

    // Required empty constructor for Firebase
    public UserData() {
    }

    public UserData(String uid, String email, boolean emailVerified) {
        this.uid = uid;
        this.email = email;
        this.emailVerified = emailVerified;
        this.createdAt = System.currentTimeMillis();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
} 