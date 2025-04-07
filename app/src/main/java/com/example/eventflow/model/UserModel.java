package com.example.eventflow.model;

public class UserModel {
    private String fullName; // ✅ Capital 'N' to match Firestore
    private String email;
    private String role;

    public UserModel() {
        // Required for Firestore
    }

    public UserModel(String fullName, String email, String role) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
