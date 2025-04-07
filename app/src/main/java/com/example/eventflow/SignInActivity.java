package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private EditText email, password;
    private Button signInButton;
    private TextView signUpLink, forgotPasswordLink;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        signInButton = findViewById(R.id.buttonSignIn);
        signUpLink = findViewById(R.id.textViewSignUp);
        forgotPasswordLink = findViewById(R.id.textForgotPassword);

        signInButton.setOnClickListener(v -> loginUser());

        signUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        forgotPasswordLink.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            if (userEmail.isEmpty()) {
                Toast.makeText(SignInActivity.this, "Please enter your email first.", Toast.LENGTH_SHORT).show();
            } else {
                sendResetEmail(userEmail);
            }
        });
    }

    private void loginUser() {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(this, "Email and Password are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserRole(user.getUid());
                        }
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        if ("Admin".equalsIgnoreCase(role)) {
                            Log.d("SIGNIN", "Admin login detected.");
                            startActivity(new Intent(SignInActivity.this, AdminActivity.class));
                        } else if ("Manager".equalsIgnoreCase(role)) {
                            Log.d("SIGNIN", "Manager login detected.");
                            startActivity(new Intent(SignInActivity.this, ManagerActivity.class));
                        } else if ("Attendee".equalsIgnoreCase(role)) {
                            Log.d("SIGNIN", "Attendee login detected.");
                            startActivity(new Intent(SignInActivity.this, AttendeeActivity.class));
                        } else {
                            Toast.makeText(SignInActivity.this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
                            Log.e("SIGNIN", "Unhandled role: " + role);
                        }

                        finish();
                    } else {
                        Toast.makeText(SignInActivity.this, "User role not found!", Toast.LENGTH_SHORT).show();
                        Log.e("SIGNIN", "Document doesn't exist for user: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignInActivity.this, "Error fetching user role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("SIGNIN", "Error fetching user role: " + e.getMessage());
                });
    }

    private void sendResetEmail(String userEmail) {
        mAuth.sendPasswordResetEmail(userEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignInActivity.this, "Reset email sent to " + userEmail, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SignInActivity.this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
