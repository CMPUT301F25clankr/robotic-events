package com.example.robotic_events_test;

/**
 * Represents a user's position on the waitlist for a specific event.
 * Each waitlist entry links a user to an event, along with the time they joined and the current waitlist state (e.g., "waiting" or "confirmed").
 * This class is typically stored and managed within Firebase Firestore.
 */
public class Waitlist {

    /** The unique identifier of the event the user is waitlisted for. */
    private String eventId;

    /** The unique identifier of the user who joined the waitlist. */
    private String userId;

    /** The joinedAt (in milliseconds) indicating when the user joined the waitlist. */
    private long joinedAt;

    /** The current state of the waitlist entry (default: "waiting"). */
    private String state;

    /**
     * Default constructor required for Firebase deserialization.
     */
    public Waitlist() {}

    /**
     * Constructs a Waitlist entry with essential details.
     *
     * @param eventId   The ID of the event the user is waitlisted for
     * @param userId    The ID of the user joining the waitlist
     * @param joinedAt The time (in milliseconds) when the user joined the waitlist
     */
    public Waitlist(String eventId, String userId, long joinedAt) {
        this.eventId = eventId;
        this.userId = userId;
        this.joinedAt = joinedAt;
        this.state = "waiting";
    }

    /**
     * Returns the event ID.
     *
     * @return the event ID
     */
    public String getEventId() { return eventId; }

    /**
     * Sets the event ID.
     *
     * @param eventId the event ID
     */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /**
     * Returns the user ID.
     *
     * @return the user ID
     */
    public String getUserId() { return userId; }

    /**
     * Sets the user ID.
     *
     * @param userId the user ID
     */
    public void setUserId(String userId) { this.userId = userId; }

    /**
     * Returns the joinedAt representing when the user joined the waitlist.
     *
     * @return the joinedAt in milliseconds
     */
    public long getJoinedAt() { return joinedAt; }

    /**
     * Sets the joinedAt for when the user joined the waitlist.
     *
     * @param joinedAt the joinedAt in milliseconds
     */
    public void setJoinedAt(long joinedAt) { this.joinedAt = joinedAt; }

    /**
     * Returns the current state of the waitlist entry.
     *
     * @return the state of the waitlist (e.g., "waiting")
     */
    public String getState() { return state; }

    /**
     * Sets the current state of the waitlist entry.
     *
     * @param state the new state (e.g., "waiting", "confirmed", or "removed")
     */
    public void setState(String state) { this.state = state; }
}
