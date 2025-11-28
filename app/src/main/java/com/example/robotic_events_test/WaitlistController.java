package com.example.robotic_events_test;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * CONTROLLER: Handles business logic for waitlist operations
 * Sits between View (Activity) and Model (Database)
 * Extended with geolocation support for US 02.02.02 and US 02.02.03
 */
public class WaitlistController {
    private final WaitlistModel waitlistModel;
    private final EventModel eventModel;
    private static final String TAG = "WaitlistController";
    private final FirebaseFirestore db;

    public WaitlistController() {
        this.waitlistModel = new WaitlistModel();
        this.eventModel = new EventModel();
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Join an event's waitlist WITH geolocation data
     * NEW: For use when event requires geolocation (US 02.02.02, 02.02.03)
     */
    public Task<Boolean> joinWaitlistWithLocation(String eventId, String userId,
                                                  double latitude, double longitude, String locationName) {
        // Validation
        if (eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "Invalid eventId");
            return Tasks.forResult(false);
        }
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Invalid userId");
            return Tasks.forResult(false);
        }

        // Check if already in waitlist
        return waitlistModel.waitlistEntryExists(eventId, userId)
                .continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult()) {
                        Log.d(TAG, "User already in waitlist");
                        return Tasks.forResult(false);
                    }

                    // Create new entry with geolocation
                    WaitlistEntry entry = new WaitlistEntry(eventId, userId, latitude, longitude, locationName);
                    return waitlistModel.addWaitlistEntry(entry)
                            .continueWithTask(addTask -> {
                                if (addTask.isSuccessful()) {
                                    Log.d(TAG, "Successfully joined waitlist with location: " + locationName);
                                    checkAndSendMilestoneNotifications(eventId);
                                    return Tasks.forResult(true);
                                } else {
                                    Log.e(TAG, "Failed to join waitlist", addTask.getException());
                                    return Tasks.forResult(false);
                                }
                            });
                });
    }

    /**
     * Join an event's waitlist WITHOUT geolocation data
     * EXISTING: Original method for backward compatibility
     * Business logic: Validate inputs, create entry, add to database
     */
    public Task<Boolean> joinWaitlist(String eventId, String userId) {
        // Validation
        if (eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "Invalid eventId");
            return Tasks.forResult(false);
        }
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Invalid userId");
            return Tasks.forResult(false);
        }

        // Check if already in waitlist
        return waitlistModel.waitlistEntryExists(eventId, userId)
                .continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult()) {
                        Log.d(TAG, "User already in waitlist");
                        return Tasks.forResult(false);
                    }

                    // Create new entry and add to database
                    WaitlistEntry entry = new WaitlistEntry(eventId, userId);
                    return waitlistModel.addWaitlistEntry(entry)
                            .continueWithTask(addTask -> {
                                if (addTask.isSuccessful()) {
                                    Log.d(TAG, "Successfully joined waitlist");

                                    // Check milestones after successful join
                                    checkAndSendMilestoneNotifications(eventId);

                                    return Tasks.forResult(true);
                                } else {
                                    Log.e(TAG, "Failed to join waitlist", addTask.getException());
                                    return Tasks.forResult(false);
                                }
                            });
                });
    }

    private void checkAndSendMilestoneNotifications(String eventId) {
        // Get current count
        waitlistModel.countWaitlistEntries(eventId).addOnSuccessListener(count -> {
            // Get event details for capacity and organizer ID
            eventModel.getEvent(eventId).addOnSuccessListener(event -> {
                if (event == null) return;

                int capacity = event.getTotalCapacity();
                if (capacity <= 0) return;

                String organizerId = event.getOrganizerId();
                String title = event.getTitle();

                // Calculate percentage
                double percentage = (double) count / capacity * 100;

                // 1. First participant
                if (count == 1) {
                    sendNotification(organizerId, "First person has joined the waitlist for " + title, eventId);
                }

                // 2. Milestones: 25%, 50%, 75%, 100% (Capacity reached)
                // We need to be careful not to spam. A simple way is to check if the count EXACTLY hits the threshold number.
                // Or check if it just crossed it.
                // Since we increment one by one, checking equality is usually safe enough unless concurrent writes skip it.
                // However, floating point math might be tricky. Let's use integer thresholds.

                int threshold25 = (int) Math.ceil(capacity * 0.25);
                int threshold50 = (int) Math.ceil(capacity * 0.50);
                int threshold75 = (int) Math.ceil(capacity * 0.75);
                int threshold100 = capacity;

                if (count == threshold25) {
                    sendNotification(organizerId, "Waitlist for " + title + " has reached 25% capacity.", eventId);
                } else if (count == threshold50) {
                    sendNotification(organizerId, "Waitlist for " + title + " has reached 50% capacity.", eventId);
                } else if (count == threshold75) {
                    sendNotification(organizerId, "Waitlist for " + title + " has reached 75% capacity.", eventId);
                } else if (count == threshold100) {
                    sendNotification(organizerId, "Waitlist for " + title + " has reached FULL capacity!", eventId);
                }
            });
        });
    }

    private void sendNotification(String receiverId, String message, String eventId) {
        if (receiverId == null) return;

        Notifications notification = new Notifications(
                false, // Not respondable, just info
                message,
                "SYSTEM", // Sender is system
                receiverId,
                System.currentTimeMillis(),
                eventId
        );

        db.collection("notifications").add(notification)
                .addOnSuccessListener(doc -> Log.d(TAG, "Milestone notification sent: " + message))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send notification", e));
    }

    /**
     * Leave an event's waitlist
     * Business logic: Validate inputs, remove from database
     */
    public Task<Boolean> leaveWaitlist(String eventId, String userId) {
        // Validation
        if (eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "Invalid eventId");
            return Tasks.forResult(false);
        }
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Invalid userId");
            return Tasks.forResult(false);
        }

        // Remove from database
        return waitlistModel.removeWaitlistEntry(eventId, userId)
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Successfully left waitlist");
                        return true;
                    } else {
                        Log.e(TAG, "Failed to leave waitlist", task.getException());
                        return false;
                    }
                });
    }

    /**
     * Check if user is in waitlist
     */
    public Task<Boolean> isUserInWaitlist(String eventId, String userId) {
        return waitlistModel.waitlistEntryExists(eventId, userId);
    }

    /**
     * Get waitlist count for an event
     */
    public Task<Integer> getWaitlistCount(String eventId) {
        return waitlistModel.countWaitlistEntries(eventId);
    }

    /**
     * Get all users in an event's waitlist
     * (For organizers to view)
     */
    public Task<List<WaitlistEntry>> getEventWaitlist(String eventId) {
        return waitlistModel.getWaitlistEntriesByEvent(eventId);
    }

    /**
     * Get all events a user has joined
     * (For user profile "My Waitlisted Events")
     */
    public Task<List<WaitlistEntry>> getUserWaitlists(String userId) {
        return waitlistModel.getWaitlistEntriesByUser(userId);
    }

    /**
     * NEW: Update user location in waitlist
     * Allows users to share/update their location after joining
     */
    public Task<Boolean> updateWaitlistLocation(String eventId, String userId,
                                                double latitude, double longitude, String locationName) {
        return waitlistModel.updateWaitlistEntryLocation(eventId, userId, latitude, longitude, locationName)
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Waitlist location updated for user: " + userId);
                        return true;
                    } else {
                        Log.e(TAG, "Failed to update location", task.getException());
                        return false;
                    }
                });
    }
}