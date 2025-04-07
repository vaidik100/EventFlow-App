package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private TextView textViewWelcome, textViewUserEmail;
    private Button logoutButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewUserEmail = findViewById(R.id.textViewUserEmail);
        logoutButton = findViewById(R.id.buttonLogout);

        if (user != null) {
            textViewUserEmail.setText(user.getEmail());
        } else {
            startActivity(new Intent(HomeActivity.this, SignInActivity.class));
            finish();
        }

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(HomeActivity.this, SignInActivity.class));
            finish();
        });
    }
}
