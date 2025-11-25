package com.example.robotic_events_test;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;  // ADD THIS IMPORT
import java.util.List;

/**
 * CONTROLLER: Handles business logic for waitlist operations
 * Sits between View (Activity) and Model (Database)
 */
public class WaitlistController {
    private final WaitlistModel waitlistModel;
    private static final String TAG = "WaitlistController";

    public WaitlistController() {
        this.waitlistModel = new WaitlistModel();
    }

    /**
     * Join an event's waitlist
     * Business logic: Validate inputs, create entry, add to database
     */
    public Task<Boolean> joinWaitlist(String eventId, String userId) {
        // Validation
        if (eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "Invalid eventId");
            return Tasks.forResult(false);  // FIXED: Tasks instead of Task
        }
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Invalid userId");
            return Tasks.forResult(false);  // FIXED: Tasks instead of Task
        }

        // Check if already in waitlist
        return waitlistModel.waitlistEntryExists(eventId, userId)
                .continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult()) {
                        Log.d(TAG, "User already in waitlist");
                        return Tasks.forResult(false);  // FIXED: Tasks instead of Task
                    }

                    // Create new entry and add to database
                    WaitlistEntry entry = new WaitlistEntry(eventId, userId);
                    return waitlistModel.addWaitlistEntry(entry)
                            .continueWith(addTask -> {
                                if (addTask.isSuccessful()) {
                                    Log.d(TAG, "Successfully joined waitlist");
                                    return true;
                                } else {
                                    Log.e(TAG, "Failed to join waitlist", addTask.getException());
                                    return false;
                                }
                            });
                });
    }

    /**
     * Leave an event's waitlist
     * Business logic: Validate inputs, remove from database
     */
    public Task<Boolean> leaveWaitlist(String eventId, String userId) {
        // Validation
        if (eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "Invalid eventId");
            return Tasks.forResult(false);  // FIXED: Tasks instead of Task
        }
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Invalid userId");
            return Tasks.forResult(false);  // FIXED: Tasks instead of Task
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
}
