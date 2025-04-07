package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button signInButton, signUpButton;
    private FirebaseAuth auth;  // Firebase Authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("DEBUG", "MainActivity started");

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();

        if (auth != null) {
            Log.d("FirebaseInit", "FirebaseAuth instance created successfully.");
        } else {
            Log.e("FirebaseInit", "FirebaseAuth failed to initialize!");
        }

        // Link buttons
        signInButton = findViewById(R.id.buttonSignIn);
        signUpButton = findViewById(R.id.buttonSignUp);

        if (signInButton == null) {
            Log.e("ERROR", "Sign In Button not found!");
        } else {
            Log.d("DEBUG", "Sign In Button found");
        }

        if (signUpButton == null) {
            Log.e("ERROR", "Sign Up Button not found!");
        } else {
            Log.d("DEBUG", "Sign Up Button found");
        }

        // Navigate to Sign In
        signInButton.setOnClickListener(v -> {
            Log.d("DEBUG", "Sign In Button Clicked");
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        // Navigate to Sign Up
        signUpButton.setOnClickListener(v -> {
            Log.d("DEBUG", "Sign Up Button Clicked");
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}