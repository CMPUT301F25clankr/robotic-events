package com.example.robotic_events_test;

import androidx.annotation.NonNull;
import java.io.Serializable;

/**
 * MODEL: Represents a single waitlist entry in the database
 * Extended with geolocation fields for US 02.02.02 and US 02.02.03
 */
public class WaitlistEntry implements Serializable {
    private String id;
    private String eventId;
    private String userId;
    private long joinedAt;
    private String status;

    // NEW: Geolocation fields
    private double latitude;
    private double longitude;
    private String locationName;

    // Empty constructor required for Firestore
    public WaitlistEntry() {
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.locationName = "";
    }

    public WaitlistEntry(@NonNull String eventId, @NonNull String userId) {
        this.eventId = eventId;
        this.userId = userId;
        this.joinedAt = System.currentTimeMillis();
        this.status = "active";
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.locationName = "";
    }

    // NEW: Constructor with geolocation
    public WaitlistEntry(@NonNull String eventId, @NonNull String userId,
                         double latitude, double longitude, String locationName) {
        this.eventId = eventId;
        this.userId = userId;
        this.joinedAt = System.currentTimeMillis();
        this.status = "active";
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName != null ? locationName : "";
    }

    // Existing Getters and Setters
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

    // NEW: Geolocation Getters and Setters
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName != null ? locationName : "";
    }
}