package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.adapter.EventAdapter;
import com.example.eventflow.model.EventModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class AttendeeActivity extends AppCompatActivity {

    private final String TAG = "ATTENDEE_ACTIVITY";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private RecyclerView recyclerUpcomingEvents;
    private EventAdapter eventAdapter;
    private List<EventModel> upcomingEvents = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee);

        recyclerUpcomingEvents = findViewById(R.id.recyclerUpcomingEvents);
        recyclerUpcomingEvents.setLayoutManager(new LinearLayoutManager(this));

        eventAdapter = new EventAdapter(upcomingEvents, this::onEventClick); // Click listener
        recyclerUpcomingEvents.setAdapter(eventAdapter);

        db = FirebaseFirestore.getInstance();

        Log.d(TAG, "Loading upcoming events...");
        fetchUpcomingEvents();

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavAttendee);
        bottomNav.setSelectedItemId(R.id.nav_events); // Set selected

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_events) {
                return true; // Already on this page
            } else if (item.getItemId() == R.id.nav_tickets) {
                Log.d(TAG, "Navigating to MyTicketsActivity");
                startActivity(new Intent(AttendeeActivity.this, MyTicketsActivity.class));
                overridePendingTransition(0, 0); // Optional: smooth transition
                return true;
            }
            return false;
        });
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AttendeeActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        });

    }

    private void fetchUpcomingEvents() {
        try {
            db.collection("events").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    upcomingEvents.clear();
                    Date today = stripTime(new Date());

                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        try {
                            EventModel event = doc.toObject(EventModel.class);
                            event.setEventId(doc.getId()); // Save Firestore doc ID
                            Date eventDate = dateFormat.parse(event.getDate());

                            if (eventDate != null && eventDate.after(today)) {
                                upcomingEvents.add(event);
                            }

                        } catch (ParseException e) {
                            Log.e(TAG, "Date parsing error: " + e.getMessage());
                        }
                    }

                    eventAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Upcoming events loaded: " + upcomingEvents.size());

                } else {
                    Log.e(TAG, "Failed to fetch events: " + task.getException());
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception in fetchUpcomingEvents: " + e.getMessage());
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private Date stripTime(Date date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.parse(sdf.format(date));
        } catch (Exception e) {
            return date;
        }
    }

    private void onEventClick(EventModel event) {
        Log.d(TAG, "Event clicked: " + event.getName());

        Intent intent = new Intent(AttendeeActivity.this, EventDetailActivity.class);
        intent.putExtra("eventId", event.getEventId());
        intent.putExtra("name", event.getName());
        intent.putExtra("date", event.getDate());
        intent.putExtra("time", event.getTime());
        intent.putExtra("location", event.getLocation());
        intent.putExtra("imageUrl", event.getImageUrl());
        startActivity(intent);
    }

}
