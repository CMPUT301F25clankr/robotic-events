package com.example.robotic_events_test;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
/**
 * MODEL: Event object and details
 * stores information for a single event
 */
public class Event implements Serializable {
    private String id;
    private String title;
    private String description;
    private long dateTime;
    private long registrationDeadline; // Added for registration deadline
    private String location;
    private String category;
    private String organizerId;
    private int totalCapacity;
    private String status;
    private String imageUrl; // This will be the icon
    private String bannerUrl; // This is the new banner
    private double price;

    private boolean geolocationRequired;
    private double eventLatitude;
    private double eventLongitude;
    public Event() {
        // Firestore requires a public no-argument constructor
    }
    /**
     * Constructs a new Event.
     * @param title The event title.
     * @param location The event location.
     * @param dateTime The event date and time.
     * @param totalCapacity The maximum number of attendees.
     * @param price The cost to attend.
     * @param description A detailed description of the event.
     * @param category The event category (e.g., "Workshop").
     * @param organizerId The ID of the event organizer.
     * @param imageUrl The URL for the event's icon.
     * @param bannerUrl The URL for the event's banner image.
     * @param registrationDeadline The deadline for registration, in milliseconds.
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
            @Nullable String imageUrl,
            @Nullable String bannerUrl,
            long registrationDeadline // Add deadline to constructor
    ) {
        this.id = null; // ID is set by Firestore
        this.title = title;
        this.dateTime = dateTime;
        this.registrationDeadline = registrationDeadline; // Assign deadline
        this.location = location;
        this.totalCapacity = totalCapacity;
        this.description = description;
        this.category = category;
        this.organizerId = organizerId;
        this.imageUrl = imageUrl;
        this.bannerUrl = bannerUrl; 
        this.price = price;
        this.status = "open";
        this.geolocationRequired = false;
        this.eventLatitude = 0.0;
        this.eventLongitude = 0.0;
    }

    // --- Existing Getters and Setters ---
    /** @return The ID of the event*/
    public String getId() { return id; }

    /** Sets the ID of the event.
     * @param id The ID of the event*/
    public void setId(String id) { this.id = id; }

    /** @return The title of the event.*/
    public String getTitle() { return title; }

    /** Sets the title of the event.
     * @param title The title of the event*/
    public void setTitle(String title) { this.title = title; }

    /** @return The description of the event.*/
    public String getDescription() { return description; }

    /** Sets the description of the event.
     * @param description The description of the event*/
    public void setDescription(String description) { this.description = description; }

    /** @return The date and time of the event.*/
    public long getDateTime() { return dateTime; }

    /** Sets the date and time of the event.
     * @param dateTime The date and time of the event*/
    public void setDateTime(long dateTime) { this.dateTime = dateTime; }

    /** @return The title of the event.*/
    public long getRegistrationDeadline() { return registrationDeadline; }

    /** Sets the title of the event.
     * @param registrationDeadline The title of the event*/
    public void setRegistrationDeadline(long registrationDeadline) { this.registrationDeadline = registrationDeadline; }

    /** @return The location of the event.*/
    public String getLocation() { return location; }

    /** Sets the location of the event.
     * @param location The location of the event*/
    public void setLocation(String location) { this.location = location; }

    /** @return The category of the event.*/
    public String getCategory() { return category; }

    /** Sets the category of the event.
     * @param category The category of the event*/
    public void setCategory(String category) { this.category = category; }

    /** @return The category of the event.*/
    public String getOrganizerId() { return organizerId; }

    /** Sets the category of the event.
     * @param organizerId The category of the event*/
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }


    /** @return The total capacity of the event.*/
    public int getTotalCapacity() { return totalCapacity; }

    /** Sets the total capacity of the event.
     * @param totalCapacity The total capacity of the event*/
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }

    /** @return The status of the event. (open or closed)*/
    public String getStatus() { return status; }

    /** Sets the status of the event.
     * @param status The status of the event*/
    public void setStatus(String status) { this.status = status; }

    /** @return The image banner of the event.*/
    public String getImageUrl() { return imageUrl; }

    /** Sets the image banner of the event.
     * @param imageUrl The image banner of the event*/
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /** @return The price of the event.*/
    public double getPrice() { return price; }

    /** Sets the price of the event.
     * @param price The price of the event*/
    public void setPrice(double price) { this.price = price; }

    /** @return True if geolocation is required for the event.*/
    public boolean isGeolocationRequired() { return geolocationRequired; }

    /** Sets whether geolocation is required for the event.
     * @param geolocationRequired True if geolocation is required.*/
    public void setGeolocationRequired(boolean geolocationRequired) { this.geolocationRequired = geolocationRequired; }

    /** @return The latitude of the event's location.*/
    public double getEventLatitude() { return eventLatitude; }

    /** Sets the latitude of the event's location.
     * @param eventLatitude The latitude of the event's location*/
    public void setEventLatitude(double eventLatitude) { this.eventLatitude = eventLatitude; }

    /** @return The longitude of the event's location.*/
    public double getEventLongitude() { return eventLongitude; }

    /** Sets the longitude of the event's location.
     * @param eventLongitude The longitude of the event's location*/
    public void setEventLongitude(double eventLongitude) { this.eventLongitude = eventLongitude; }

    /** @return The banner URL of the event.*/
    public String getBannerUrl() { return bannerUrl; }

    /** Sets the banner URL of the event.
     * @param bannerUrl The banner URL of the event.*/
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }
}
