package com.example.eventflow.model;

public class EventModel {

    private String eventId;
    private String name;
    private String date;
    private String time;
    private String location;
    private String imageUrl;

    // Section header support
    private boolean isSection;
    private String sectionTitle;

    public EventModel() {
        // Required empty constructor for Firestore
    }

    // Constructor for actual events
    public EventModel(String eventId, String name, String date, String time, String location, String imageUrl) {
        this.eventId = eventId;
        this.name = name;
        this.date = date;
        this.time = time;
        this.location = location;
        this.imageUrl = imageUrl;
        this.isSection = false;
    }

    // Constructor for section headers
    public static EventModel createSection(String title) {
        EventModel section = new EventModel();
        section.isSection = true;
        section.sectionTitle = title;
        return section;
    }

    public boolean isSection() {
        return isSection;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
