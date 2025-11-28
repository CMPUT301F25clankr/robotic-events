package com.example.robotic_events_test;

import androidx.annotation.NonNull;
import java.io.Serializable;

/**
 * MODEL: Represents a single waitlist entry in the database
 */
public class WaitlistEntry implements Serializable {
    private String id;
    private String eventId;
    private String userId;
    private long joinedAt;
    private String status;

    // Empty constructor required for Firestore
    public WaitlistEntry() {
    }

    public WaitlistEntry(@NonNull String eventId, @NonNull String userId) {
        this.eventId = eventId;
        this.userId = userId;
        this.joinedAt = System.currentTimeMillis();
        this.status = "active";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(long joinedAt) {
        this.joinedAt = joinedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
