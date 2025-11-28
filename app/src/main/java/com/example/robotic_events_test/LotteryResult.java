package com.example.robotic_events_test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * MODEL: Represents the result of a lottery run for one event.
 */
public class LotteryResult implements Serializable {
    private String id;              // Firestore doc ID
    private String eventId;         // Event this lottery is for
    private String organizerId;     // Who ran it
    private long runAt;             // Timestamp
    private List<String> selectedUserIds;   // Pending (Invited but not yet accepted)
    private List<String> acceptedUserIds;   // Confirmed Attendees (Accepted invitation)
    private List<String> declinedUserIds;   // Declined invitation
    private List<String> notSelectedUserIds; // Others in waitlist at that time

    public LotteryResult() {
        // Required for Firestore
        this.selectedUserIds = new ArrayList<>();
        this.acceptedUserIds = new ArrayList<>();
        this.declinedUserIds = new ArrayList<>();
        this.notSelectedUserIds = new ArrayList<>();
    }

    public LotteryResult(String eventId, String organizerId,
                         List<String> selectedUserIds,
                         List<String> notSelectedUserIds) {
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.selectedUserIds = selectedUserIds;
        this.notSelectedUserIds = notSelectedUserIds;
        this.acceptedUserIds = new ArrayList<>();
        this.declinedUserIds = new ArrayList<>();
        this.runAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public long getRunAt() { return runAt; }
    public void setRunAt(long runAt) { this.runAt = runAt; }

    public List<String> getSelectedUserIds() { return selectedUserIds; }
    public void setSelectedUserIds(List<String> selectedUserIds) { this.selectedUserIds = selectedUserIds; }

    public List<String> getAcceptedUserIds() { return acceptedUserIds; }
    public void setAcceptedUserIds(List<String> acceptedUserIds) { this.acceptedUserIds = acceptedUserIds; }

    public List<String> getDeclinedUserIds() { return declinedUserIds; }
    public void setDeclinedUserIds(List<String> declinedUserIds) { this.declinedUserIds = declinedUserIds; }

    public List<String> getNotSelectedUserIds() { return notSelectedUserIds; }
    public void setNotSelectedUserIds(List<String> notSelectedUserIds) { this.notSelectedUserIds = notSelectedUserIds; }
}
