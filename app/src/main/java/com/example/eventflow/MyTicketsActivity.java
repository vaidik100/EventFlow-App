package com.example.eventflow;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.adapter.TicketAdapter;
import com.example.eventflow.model.TicketModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyTicketsActivity extends AppCompatActivity {

    private final String TAG = "MY_TICKETS";
    private RecyclerView recyclerTickets;
    private List<TicketModel> ticketList = new ArrayList<>();
    private TicketAdapter ticketAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        recyclerTickets = findViewById(R.id.recyclerTickets);
        recyclerTickets.setLayoutManager(new LinearLayoutManager(this));
        ticketAdapter = new TicketAdapter(ticketList);
        recyclerTickets.setAdapter(ticketAdapter);

        db = FirebaseFirestore.getInstance();

        fetchTickets();
    }

    private void fetchTickets() {
        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            db.collection("tickets")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(ticketSnapshot -> {
                        ticketList.clear();

                        for (QueryDocumentSnapshot ticketDoc : ticketSnapshot) {
                            TicketModel ticket = ticketDoc.toObject(TicketModel.class);
                            String eventId = ticket.getEventId();

                            // 🔁 Fetch latest event details using eventId
                            db.collection("events").document(eventId).get()
                                    .addOnSuccessListener(eventDoc -> {
                                        if (eventDoc.exists()) {
                                            ticket.setEventName(eventDoc.getString("name"));
                                            ticket.setDate(eventDoc.getString("date"));
                                            ticket.setTime(eventDoc.getString("time"));
                                            ticket.setLocation(eventDoc.getString("location"));
                                        }

                                        // ✅ Add to list & notify after fetching
                                        ticketList.add(ticket);
                                        ticketAdapter.notifyDataSetChanged();

                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch event: " + e.getMessage()));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading tickets: ", e);
                        Toast.makeText(this, "Failed to load tickets", Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Exception while fetching tickets: " + e.getMessage());
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }
}
