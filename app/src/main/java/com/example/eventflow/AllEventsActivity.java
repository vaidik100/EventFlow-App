package com.example.eventflow;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.adapter.EventAdapter;
import com.example.eventflow.model.EventModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AllEventsActivity extends AppCompatActivity {

    private final String TAG = "ALL_EVENTS";
    private RecyclerView recyclerAllEvents;
    private EventAdapter eventAdapter;
    private List<EventModel> eventList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_events);

        recyclerAllEvents = findViewById(R.id.recyclerAllEvents);
        db = FirebaseFirestore.getInstance();

        eventList = new ArrayList<>();

        eventAdapter = new EventAdapter(eventList, true);

        recyclerAllEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerAllEvents.setAdapter(eventAdapter);

        fetchAllEvents();
    }

    private void fetchAllEvents() {
        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                eventList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    EventModel event = document.toObject(EventModel.class);
                    event.setEventId(document.getId());
                    eventList.add(event);
                }
                eventAdapter.notifyDataSetChanged();
                Log.d(TAG, "Events loaded: " + eventList.size());
            } else {
                Log.e(TAG, "Error loading events: ", task.getException());
                Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
