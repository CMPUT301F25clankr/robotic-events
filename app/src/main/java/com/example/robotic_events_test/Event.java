package com.example.robotic_events_test;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import java.io.Serializable;

public class Event implements Serializable {
    // Rest of your code stays the same


    private String id;
    private String title;
    private String description;
    private long dateTime;
    private String location;
    private String category;
    private String organizerId;
    private int totalCapacity;
    private String status;
    private String imageUrl;
    private double price;

    public Event() {
        // Firestore requires a public no-argument constructor
    }


    public Event(
            @NonNull String title,
            @NonNull String location,
            long dateTime,
            int totalCapacity,
            double price,

            @Nullable String description,
            @Nullable String category,
            @Nullable String organizerId,
            @Nullable String imageUrl
    ) {
        this.id = null; // ID is set by Firestore
        this.title = title;
        this.dateTime = dateTime;
        this.location = location;
        this.totalCapacity = totalCapacity;
        this.description = description;
        this.category = category;
        this.organizerId = organizerId;
        this.imageUrl = imageUrl;
        this.price = price;
        this.status = "open";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public long getDateTime() { return dateTime; }
    public void setDateTime(long dateTime) { this.dateTime = dateTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public int getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }


}