package com.example.robotic_events_test;

public class Waitlist {

    private String eventId;
    private String userId;
    private long timestamp;
    private String state;

    public Waitlist() {}

    public Waitlist(String eventId, String userId, long timestamp) {
        this.eventId = eventId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.state = "waiting";
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}
