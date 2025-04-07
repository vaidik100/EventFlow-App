package com.example.eventflow.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventflow.EditEventActivity;
import com.example.eventflow.R;
import com.example.eventflow.model.EventModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SECTION = 0;
    private static final int TYPE_EVENT = 1;

    private List<EventModel> eventList;
    private OnEventClickListener listener;
    private boolean isAdminMode;

    // Admin constructor
    public EventAdapter(List<EventModel> eventList, boolean isAdminMode) {
        this.eventList = eventList;
        this.isAdminMode = isAdminMode;
    }

    // Attendee constructor
    public EventAdapter(List<EventModel> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
        this.isAdminMode = false;
    }

    @Override
    public int getItemViewType(int position) {
        return eventList.get(position).isSection() ? TYPE_SECTION : TYPE_EVENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SECTION) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_section_header, parent, false);
            return new SectionViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_event, parent, false);
            return new EventViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        EventModel event = eventList.get(position);

        if (holder instanceof SectionViewHolder) {
            ((SectionViewHolder) holder).sectionTitle.setText(event.getSectionTitle());

        } else if (holder instanceof EventViewHolder) {
            EventViewHolder h = (EventViewHolder) holder;
            Context context = h.itemView.getContext();

            h.eventTitle.setText(event.getName());
            h.eventDate.setText(event.getDate() + " - " + event.getTime());
            h.eventLocation.setText(event.getLocation());

            Glide.with(context)
                    .load(event.getImageUrl())
                    .placeholder(R.drawable.sample_event)
                    .into(h.eventImage);

            if (listener != null && !isAdminMode) {
                h.itemView.setOnClickListener(v -> listener.onEventClick(event));
            }

            Log.d("EVENT_ADAPTER", "isAdminMode: " + isAdminMode + ", position: " + position);

            if (isAdminMode && !event.isSection()) {
                h.adminButtonContainer.setVisibility(View.VISIBLE);

                h.btnEdit.setOnClickListener(v -> {
                    Intent intent = new Intent(context, EditEventActivity.class);
                    intent.putExtra("eventId", event.getEventId());
                    intent.putExtra("name", event.getName());
                    intent.putExtra("date", event.getDate());
                    intent.putExtra("time", event.getTime());
                    intent.putExtra("location", event.getLocation());
                    intent.putExtra("imageUrl", event.getImageUrl());
                    context.startActivity(intent);
                });

                h.btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete Event")
                            .setMessage("Are you sure you want to delete this event?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                FirebaseFirestore.getInstance().collection("events")
                                        .document(event.getEventId())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show();
                                            eventList.remove(position);
                                            notifyItemRemoved(position);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

            } else {
                h.adminButtonContainer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public interface OnEventClickListener {
        void onEventClick(EventModel event);
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle, eventDate, eventLocation;
        ImageView eventImage;
        Button btnEdit, btnDelete;
        LinearLayout adminButtonContainer;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.textEventTitle);
            eventDate = itemView.findViewById(R.id.textEventDate);
            eventLocation = itemView.findViewById(R.id.textEventLocation);
            eventImage = itemView.findViewById(R.id.imageEvent);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            adminButtonContainer = itemView.findViewById(R.id.adminButtonContainer);
        }
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView sectionTitle;

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.textSectionTitle);
        }
    }
}
