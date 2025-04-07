package com.example.eventflow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.adapter.EventAdapter;
import com.example.eventflow.model.EventModel;
import com.example.eventflow.room.AppDatabase;
import com.example.eventflow.room.EventDao;
import com.example.eventflow.room.LocalEventEntity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminActivity extends AppCompatActivity {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final String TAG = "ADMIN_ACTIVITY";
    private RecyclerView recyclerEvents;
    private EventAdapter eventAdapter;
    private List<EventModel> allEvents = new ArrayList<>();
    private FirebaseFirestore db;

    // TextViews for counters
    private TextView textUpcomingCount, textPastCount, textUserCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();
        recyclerEvents = findViewById(R.id.recyclerEvents);
        FloatingActionButton fabAddEvent = findViewById(R.id.fabAddEvent);
        BottomNavigationView bottomNavView = findViewById(R.id.bottomNavView);
        ImageButton btnLogout = findViewById(R.id.btnLogout);

        // Get references to the summary count TextViews
        textUpcomingCount = findViewById(R.id.textUpcomingEvents);
        textPastCount = findViewById(R.id.textPastEvents);
        textUserCount = findViewById(R.id.textTotalUsers);

        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(allEvents, true);
        recyclerEvents.setAdapter(eventAdapter);

        fabAddEvent.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, CreateEventActivity.class)));

        bottomNavView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.nav_events) {
                startActivity(new Intent(AdminActivity.this, AllEventsActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_users) {
                startActivity(new Intent(this, UsersActivity.class));
                return true;
            }
            return false;
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AdminActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        });

        fetchEvents();
        fetchUserCount(); // 🔁 Load user count
    }

    private void fetchEvents() {
        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<EventModel> todayEvents = new ArrayList<>();
                List<EventModel> upcomingEvents = new ArrayList<>();
                List<EventModel> pastEvents = new ArrayList<>();

                Date today = stripTime(new Date());

                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        EventModel event = document.toObject(EventModel.class);
                        event.setEventId(document.getId());
                        if (event.getDate() == null) continue;

                        Date eventDate = dateFormat.parse(event.getDate());

                        if (eventDate == null) continue;

                        if (eventDate.equals(today)) {
                            todayEvents.add(event);
                        } else if (eventDate.after(today)) {
                            upcomingEvents.add(event);
                        } else {
                            pastEvents.add(event);
                        }

                    } catch (ParseException e) {
                        Log.e(TAG, "Date parsing failed: " + e.getMessage());
                    }
                }

                Comparator<EventModel> dateComparator = (a, b) -> {
                    try {
                        return dateFormat.parse(a.getDate()).compareTo(dateFormat.parse(b.getDate()));
                    } catch (ParseException e) {
                        return 0;
                    }
                };

                Collections.sort(todayEvents, dateComparator);
                Collections.sort(upcomingEvents, dateComparator);
                Collections.sort(pastEvents, dateComparator);

                allEvents.clear();

                if (!todayEvents.isEmpty()) {
                    allEvents.add(EventModel.createSection("📅 Today's Events"));
                    allEvents.addAll(todayEvents);
                }

                if (!upcomingEvents.isEmpty()) {
                    allEvents.add(EventModel.createSection("🔜 Upcoming Events"));
                    allEvents.addAll(upcomingEvents);
                }

                if (!pastEvents.isEmpty()) {
                    allEvents.add(EventModel.createSection("⏳ Past Events"));
                    allEvents.addAll(pastEvents);
                }

                eventAdapter.notifyDataSetChanged();

                // ✅ Update counts
                textUpcomingCount.setText(String.valueOf(upcomingEvents.size()));
                textPastCount.setText(String.valueOf(pastEvents.size()));

                // ✅ Save upcoming events to Room
                new Thread(() -> {
                    AppDatabase dbInstance = AppDatabase.getInstance(getApplicationContext());
                    EventDao eventDao = dbInstance.eventDao();
                    eventDao.deleteAll();

                    for (EventModel event : upcomingEvents) {
                        if (event.getEventId() != null && event.getName() != null &&
                                event.getDate() != null && event.getLocation() != null) {
                            LocalEventEntity entity = new LocalEventEntity(
                                    event.getEventId(),
                                    event.getName(),
                                    event.getDate(),
                                    event.getLocation()
                            );
                            eventDao.insertEvent(entity);
                        }
                    }
                    Log.d(TAG, "Events cached locally in Room.");
                }).start();

            } else {
                Log.e(TAG, "Firestore fetch failed: " + task.getException());
                Toast.makeText(this, "Loading cached events...", Toast.LENGTH_SHORT).show();

                // 🔁 Fallback: Load from Room
                new Thread(() -> {
                    AppDatabase dbInstance = AppDatabase.getInstance(getApplicationContext());
                    List<LocalEventEntity> cachedEvents = dbInstance.eventDao().getAllEvents();

                    runOnUiThread(() -> {
                        allEvents.clear();
                        if (!cachedEvents.isEmpty()) {
                            allEvents.add(EventModel.createSection("📁 Cached Events"));
                            for (LocalEventEntity e : cachedEvents) {
                                allEvents.add(new EventModel(e.getEventId(), e.getName(), e.getDate(), "", e.getLocation(), ""));
                            }
                            eventAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(AdminActivity.this, "No cached events found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
            }
        });
    }

    private void fetchUserCount() {
        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int userCount = snapshot.size();
                    textUserCount.setText(String.valueOf(userCount));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch user count: " + e.getMessage());
                });
    }

    private Date stripTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
