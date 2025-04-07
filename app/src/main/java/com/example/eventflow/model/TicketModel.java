package com.example.eventflow.model;

public class TicketModel {
    private String ticketId;
    private String userId;
    private String eventId;
    private String eventName;
    private String date;
    private String time;
    private String location;
    private String qrUrl; // ✅ Add this line

    public TicketModel() {
        // Needed for Firestore
    }

    public TicketModel(String ticketId, String userId, String eventId, String eventName, String date, String time, String location, String qrUrl) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.date = date;
        this.time = time;
        this.location = location;
        this.qrUrl = qrUrl; // ✅
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getUserId() {
        return userId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
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

    public String getQrUrl() {
        return qrUrl; // ✅
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public void setLocation(String location) {
        this.location = location;
    }

}
