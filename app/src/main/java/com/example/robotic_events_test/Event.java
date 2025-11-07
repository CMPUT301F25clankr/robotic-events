package com.example.robotic_events_test;

/**
 * Represents an event in the Robotic Events application.
 * This class contains all details about an event such as its title, description, date/time, location, organizer, capacity, price, and image resource.
 */
public class Event {

    /** Unique identifier for the event. */
    private String id;

    /** Name of the event. */
    private String title;

    /** Description of the event. */
    private String description;

    /** The date and time of the event */
    private long dateTime;

    /** Location of the event. */
    private String location;

    /** Category of the event */
    private String category;

    /** ID of the organizer who created the event. */
    private String organizerId;

    /** Capacity of the event */
    private int totalCapacity;

    /** Status of the event. */
    private String status;

    private String imageUrl;

    /** Entry fee for the event. */
    private double price;

    /**
     * Default constructor required for Firebase deserialization.
     */
    public Event() {}

    /**
     * Constructs an Event with the most common fields.
     *
     * @param id            Unique identifier for the event
     * @param title         Title of the event
     * @param dateTime      Date and time (Unix timestamp)
     * @param location      Location or venue name
     * @param imageUrl      URL of the event's image
     * @param totalCapacity Maximum allowed participants
     * @param price         Entry fee or price for the event
     */
    public Event(String id, String title, long dateTime,
                 String location, String imageUrl,
                 int totalCapacity, double price) {

        this.id = id;
        this.title = title;
        this.dateTime = dateTime;
        this.location = location;
        this.imageUrl = imageUrl;
        this.totalCapacity = totalCapacity;
        this.price = price;
        this.status = "open";
    }

    /** @return the event ID */
    public String getId() { return id; }

    /** @param id sets the event ID */
    public void setId(String id) { this.id = id; }

    /** @return the title of the event */
    public String getTitle() { return title; }

    /** @param title sets the title of the event */
    public void setTitle(String title) { this.title = title; }

    /** @return the event description */
    public String getDescription() { return description; }

    /** @param description sets the event description */
    public void setDescription(String description) { this.description = description; }

    /** @return the event's timestamp */
    public long getDateTime() { return dateTime; }

    /** @param dateTime sets the event timestamp */
    public void setDateTime(long dateTime) { this.dateTime = dateTime; }

    /** @return the event location */
    public String getLocation() { return location; }

    /** @param location sets the event location */
    public void setLocation(String location) { this.location = location; }

    /** @return the event category */
    public String getCategory() { return category; }

    /** @param category sets the event category */
    public void setCategory(String category) { this.category = category; }

    /** @return the organizer's ID */
    public String getOrganizerId() { return organizerId; }

    /** @param organizerId sets the organizer's ID */
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    /** @return total capacity of the event */
    public int getTotalCapacity() { return totalCapacity; }

    /** @param totalCapacity sets total capacity */
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }

    /** @return event status */
    public String getStatus() { return status; }

    /** @param status sets the event status */
    public void setStatus(String status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /** @return event price */
    public double getPrice() { return price; }

    /** @param price sets event price */
    public void setPrice(double price) { this.price = price; }
}
