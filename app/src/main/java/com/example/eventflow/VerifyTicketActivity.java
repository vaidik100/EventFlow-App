package com.example.eventflow;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class VerifyTicketActivity extends AppCompatActivity {

    private static final String TAG = "VERIFY_TICKET";
    private EditText editTicketId;
    private Button btnVerify;
    private TextView textTicketDetails;
    private LinearLayout ticketResultLayout;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_ticket);

        editTicketId = findViewById(R.id.editTicketId);
        btnVerify = findViewById(R.id.btnVerify);
        textTicketDetails = findViewById(R.id.textTicketDetails);
        ticketResultLayout = findViewById(R.id.ticketResultLayout);

        db = FirebaseFirestore.getInstance();

        // Check if ticket ID passed from QR scan
        String scannedTicketId = getIntent().getStringExtra("ticketId");
        if (scannedTicketId != null && !scannedTicketId.isEmpty()) {
            Log.d(TAG, "Ticket ID from QR scan: " + scannedTicketId);
            editTicketId.setText(scannedTicketId);
            verifyTicket(scannedTicketId); // Auto-verify if passed
        }

        btnVerify.setOnClickListener(v -> {
            String inputId = editTicketId.getText().toString().trim();
            verifyTicket(inputId);
        });
    }

    private void verifyTicket(String ticketId) {
        if (ticketId.isEmpty()) {
            Toast.makeText(this, "Please enter a Ticket ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Verifying ticket ID: " + ticketId);
        ticketResultLayout.setVisibility(View.GONE); // Hide result initially

        db.collection("tickets").document(ticketId).get()
                .addOnSuccessListener(ticketSnapshot -> {
                    if (ticketSnapshot.exists()) {
                        String eventName = ticketSnapshot.getString("eventName");
                        String userId = ticketSnapshot.getString("userId");
                        String status = ticketSnapshot.getString("status");

                        Log.d(TAG, "Ticket found. Event: " + eventName + ", UserID: " + userId + ", Status: " + status);

                        // Optional: Prevent double attendance
                        if ("attended".equalsIgnoreCase(status)) {
                            String details = "⚠️ Ticket Already Marked as Attended\n\n" +
                                    "🎟 Ticket ID: " + ticketId + "\n" +
                                    "📅 Event: " + eventName;
                            textTicketDetails.setText(details);
                            ticketResultLayout.setVisibility(View.VISIBLE);
                            return;
                        }

                        // ✅ Mark as attended
                        db.collection("tickets").document(ticketId)
                                .update("status", "attended")
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Ticket marked as attended"))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to update ticket status: " + e.getMessage()));

                        // ✅ Fetch user details
                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(userSnapshot -> {
                                    if (userSnapshot.exists()) {
                                        String userName = userSnapshot.getString("fullName");

                                        String details = "✅ Ticket Verified & Marked as Attended\n\n" +
                                                "🎟 Ticket ID: " + ticketId + "\n" +
                                                "📅 Event: " + eventName + "\n" +
                                                "👤 User: " + userName + "\n" +
                                                "📍 Status: Attended";

                                        textTicketDetails.setText(details);
                                        ticketResultLayout.setVisibility(View.VISIBLE);
                                    } else {
                                        Toast.makeText(this, "User details not found", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to load user info", Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        Log.w(TAG, "Ticket not found in Firestore");
                        Toast.makeText(this, "Invalid Ticket ID", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching ticket: " + e.getMessage());
                    Toast.makeText(this, "Verification failed", Toast.LENGTH_SHORT).show();
                });
    }
}
