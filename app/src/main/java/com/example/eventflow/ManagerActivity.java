package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ManagerActivity extends AppCompatActivity {

    private Button btnScanQR, btnVerifyById;
    private ImageButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        btnScanQR = findViewById(R.id.btnScanQR);
        btnVerifyById = findViewById(R.id.btnVerifyById);
        btnLogout = findViewById(R.id.btnLogout);

        btnScanQR.setOnClickListener(v -> {
            startActivity(new Intent(ManagerActivity.this, ScanTicketActivity.class));
        });

        btnVerifyById.setOnClickListener(v -> {
            startActivity(new Intent(ManagerActivity.this, VerifyTicketActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ManagerActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        });
    }
}
