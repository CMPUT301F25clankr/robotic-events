package com.example.robotic_events_test;

import com.google.firebase.firestore.Exclude;

/**
 * MODEL: Represents a notification for an event.
 */

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

    /**
     * Constructs a new Notifications object.
     * @param respondable Whether the notification is respondable.
     * @param message The message content.
     * @param senderId The ID of the sender.
     * @param receiverId The ID of the receiver.
     * @param timestamp The timestamp when the notification was sent.
     * @param eventId The ID of the event.
     */

    public Notifications(Boolean respondable, String message, String senderId, String receiverId, long timestamp, String eventId) {
        this.respondable = respondable;
        this.message = message;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.timestamp = timestamp;
        this.eventId = eventId;
        this.expiryTimestamp = 0;
    }

    /**
     * @return The ID of the notification.
     */
    @Exclude
    public String getId() {
        return id;
    }

    /**
     * @param id The ID of the notification.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return Whether the notification is respondable.
     */
    public Boolean getRespondable() {
        return respondable;
    }

    /**
     * @param respondable Whether the notification is respondable.
     */
    public void setRespondable(Boolean respondable) {
        this.respondable = respondable;
    }

    /**
     * @return The message content.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message The message content.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return The ID of the sender.
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * @param senderId The ID of the sender.
     */
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    /**
     * @return The ID of the receiver.
     */
    public String getReceiverId() {
        return receiverId;
    }

    /**
     * @param receiverId The ID of the receiver.
     */
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    /**
     * @return The timestamp when the notification was sent.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp The timestamp when the notification was sent.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return The ID of the event.
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * @param eventId The ID of the event.
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * @return The timestamp when the invitation expires.
     */
    public long getExpiryTimestamp() {
        return expiryTimestamp;
    }

    /**
     * @param expiryTimestamp The timestamp when the invitation expires.
     */
    public void setExpiryTimestamp(long expiryTimestamp) {
        this.expiryTimestamp = expiryTimestamp;
    }
}
