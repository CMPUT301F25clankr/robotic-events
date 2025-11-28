package com.example.robotic_events_test;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

/**
 * MODEL: Handles all database operations for waitlists
 * This is the data access layer - only interacts with Firestore
 */
public class WaitlistModel {
    private final CollectionReference waitlistCollection;
    private static final String TAG = "WaitlistModel";

    public WaitlistModel() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.waitlistCollection = db.collection("waitlists");
    }

    /**
     * Add a waitlist entry to database
     */
    public Task<String> addWaitlistEntry(WaitlistEntry entry) {
        return waitlistCollection.add(entry)
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        String generatedId = task.getResult().getId();
                        waitlistCollection.document(generatedId).update("id", generatedId);
                        Log.d(TAG, "Waitlist entry added: " + generatedId);
                        return generatedId;
                    } else {
                        Log.e(TAG, "Error adding waitlist entry", task.getException());
                        throw task.getException();
                    }
                });
    }

    /**
     * Remove waitlist entries matching eventId and userId
     */
    public Task<Void> removeWaitlistEntry(String eventId, String userId) {
        return waitlistCollection
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            doc.getReference().delete();
                            Log.d(TAG, "Waitlist entry removed: " + doc.getId());
                        }
                    } else {
                        Log.e(TAG, "Error removing waitlist entry", task.getException());
                    }
                    return null;
                });
    }

    /**
     * Check if a waitlist entry exists
     */
    public Task<Boolean> waitlistEntryExists(String eventId, String userId) {
        return waitlistCollection
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult() != null && !task.getResult().isEmpty();
                    }
                    return false;
                });
    }

    /**
     * Count waitlist entries for an event
     */
    public Task<Integer> countWaitlistEntries(String eventId) {
        return waitlistCollection
                .whereEqualTo("eventId", eventId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().size();
                    }
                    return 0;
                });
    }

    /**
     * Get all waitlist entries for an event
     */
    public Task<List<WaitlistEntry>> getWaitlistEntriesByEvent(String eventId) {
        return waitlistCollection
                .whereEqualTo("eventId", eventId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObjects(WaitlistEntry.class);
                    }
                    return new ArrayList<>();
                });
    }

    /**
     * Get all waitlist entries for a user
     */
    public Task<List<WaitlistEntry>> getWaitlistEntriesByUser(String userId) {
        return waitlistCollection
                .whereEqualTo("userId", userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObjects(WaitlistEntry.class);
                    }
                    return new ArrayList<>();
                });
    }
}
