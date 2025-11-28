package com.example.robotic_events_test;

import com.google.firebase.firestore.Exclude;

public class Notifications {
    private String id;
    private Boolean respondable;
    private String message;
    private String senderId;
    private String receiverId;
    private long timestamp;
    private String eventId;
    private long expiryTimestamp; // NEW: Time when the invitation expires

    public Notifications() {
        // Required for Firestore
    }

    public Notifications(Boolean respondable, String message, String senderId, String receiverId, long timestamp, String eventId) {
        this.respondable = respondable;
        this.message = message;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.timestamp = timestamp;
        this.eventId = eventId;
        this.expiryTimestamp = 0;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getRespondable() {
        return respondable;
    }

    public void setRespondable(Boolean respondable) {
        this.respondable = respondable;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public long getExpiryTimestamp() {
        return expiryTimestamp;
    }

    public void setExpiryTimestamp(long expiryTimestamp) {
        this.expiryTimestamp = expiryTimestamp;
    }
}
