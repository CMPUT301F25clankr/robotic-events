package com.example.clankr_roboticevents.model;

import java.util.*;

/**
 * Event that users can register for or join a waitlist.
 *
 * <p>This class keeps only in-memory state. Persistence mapping
 * will be handled by a repository later.</p>
 */
public class Event {

    private String eventId;
    private String title;
    private String description;
    private String location;              // simple text for now
    private String startIso;              // ISO-8601 string (e.g., 2025-11-01T14:00)
    private int capacity;
    private String organizerId;
    private EventCategory category = EventCategory.GENERAL;
    private int priceCents = 0;

    // Attendees and waitlist tracked by userId (UID strings)
    private final LinkedHashSet<String> attendees = new LinkedHashSet<>();
    private final LinkedList<String> waitlist = new LinkedList<>();

    /**
     * Constructs an Event.
     *
     * @param eventId     unique id
     * @param title       title
     * @param description details
     * @param location    free-form location text
     * @param startIso    ISO-8601 start date-time string
     * @param capacity    max attendees (>= 0)
     * @param organizerId organizer user id
     */
    public Event(String eventId, String title, String description, String location,
                 String startIso, int capacity, String organizerId) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startIso = startIso;
        this.capacity = Math.max(0, capacity);
        this.organizerId = organizerId;
    }

    // --- getters / setters (minimal) ---
    public String getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getStartIso() { return startIso; }
    public int getCapacity() { return capacity; }
    public String getOrganizerId() { return organizerId; }
    public EventCategory getCategory() { return category; }
    public int getPriceCents() { return priceCents; }
    public List<String> getAttendees() { return new ArrayList<>(attendees); }
    public List<String> getWaitlist() { return new ArrayList<>(waitlist); }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(String location) { this.location = location; }
    public void setStartIso(String startIso) { this.startIso = startIso; }
    public void setCapacity(int capacity) { this.capacity = Math.max(0, capacity); }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public void setCategory(EventCategory category) { this.category = category == null ? EventCategory.GENERAL : category; }
    public void setPriceCents(int priceCents) { this.priceCents = Math.max(0, priceCents); }

    // --- domain helpers ---

    /**
     * Attempts to add the user to attendees list.
     * @param userId user id
     * @return true if added as attendee; false if already present or event is full
     */
    public boolean addAttendee(String userId) {
        if (userId == null || userId.isEmpty()) return false;
        if (attendees.size() >= capacity) return false;
        if (waitlist.contains(userId)) waitlist.remove(userId);
        return attendees.add(userId);
    }

    /**
     * Adds user to waitlist if not already attending or waiting.
     * Maintains FIFO order.
     */
    public void addToWaitlist(String userId) {
        if (userId == null || userId.isEmpty()) return;
        if (attendees.contains(userId)) return;
        if (!waitlist.contains(userId)) waitlist.add(userId);
    }

    /** Removes a user entirely (from attendees and waitlist). */
    public void removeUser(String userId) {
        attendees.remove(userId);
        waitlist.remove(userId);
    }

    /** @return number of available attendee slots remaining. */
    public int remainingCapacity() {
        return Math.max(0, capacity - attendees.size());
    }

    /** @return true if user is currently an attendee. */
    public boolean isAttending(String userId) { return attendees.contains(userId); }

    /** @return true if user is currently on the waitlist. */
    public boolean isWaitlisted(String userId) { return waitlist.contains(userId); }

    /**
     * Promotes the next user from waitlist to attendees, if space exists.
     * @return promoted userId, or null if none promoted.
     */
    public String promoteNextFromWaitlist() {
        if (attendees.size() >= capacity) return null;
        if (waitlist.isEmpty()) return null;
        String next = waitlist.removeFirst();
        attendees.add(next);
        return next;
    }
}
