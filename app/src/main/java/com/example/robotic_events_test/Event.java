package com.example.robotic_events_test;

/**
 * Represents an event in the Robotic Events application.
 * This class contains all details about an event such as its title, description, date/time, location, organizer, capacity, price, and image resource.
 */
public class Event {

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
    private List<String> waitlist;

    public Event() {
        // Firestore requires a public no-argument constructor
    }

    /**
     * Comprehensive constructor for creating a new Event.
     * @param title The name of the event (Required).
     * @param dateTime The event's start time in milliseconds (Required).
     * @param location The event's location (Required).
     * @param totalCapacity The maximum number of attendees (Required).
     * @param description A nullable description of the event.
     * @param category A nullable category for the event.
     * @param organizerId A nullable ID for the event organizer.
     * @param imageUrl A nullable URL for the event's image.
     * @param price The price of the event. Can be 0.0.
     */
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
        // default values for new events
        this.status = "open";
        this.waitlist = new ArrayList<>();
    }


    // --- All Getters and Setters Below ---

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
    public List<String> getWaitlist() { return waitlist; }
    public void setWaitlist(List<String> waitlist) { this.waitlist = waitlist; }
}