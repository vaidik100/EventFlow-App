package com.example.eventflow.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "events")
public class LocalEventEntity {

    @PrimaryKey
    @NonNull
    private String eventId;

    private String name;
    private String date;
    private String location;

    public LocalEventEntity(@NonNull String eventId, String name, String date, String location) {
        this.eventId = eventId;
        this.name = name;
        this.date = date;
        this.location = location;
    }

    @NonNull
    public String getEventId() {
        return eventId;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }
}
