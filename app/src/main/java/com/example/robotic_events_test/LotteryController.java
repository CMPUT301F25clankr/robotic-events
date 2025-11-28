package com.example.robotic_events_test;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CONTROLLER: Business logic for running lotteries.
 */
public class LotteryController {
    private static final String TAG = "LotteryController";

    private final WaitlistModel waitlistModel;
    public final LotteryModel lotteryModel;
    private final EventModel eventModel;
    private final FirebaseFirestore db;

    public LotteryController() {
        this.waitlistModel = new WaitlistModel();
        this.lotteryModel = new LotteryModel();
        this.eventModel = new EventModel(); // use your existing constructor
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Run lottery for an event.
     * Only allowed if currentUserId == event.organizerId.
     *
     * @param eventId       Event to run lottery for
     * @param currentUserId Logged-in user
     * @return Task<Boolean> true if lottery ran, false otherwise
     */
    public Task<Boolean> runLottery(String eventId, String currentUserId) {
        if (eventId == null || eventId.isEmpty()
                || currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "Invalid eventId or currentUserId");
            return Tasks.forResult(false);
        }

        // 1. Load event and check organizer
        return eventModel.getEvent(eventId)
                .continueWithTask(eventTask -> {
                    Event event = eventTask.getResult();
                    if (event == null) {
                        Log.e(TAG, "Event not found");
                        return Tasks.forResult(false);
                    }

                    if (!currentUserId.equals(event.getOrganizerId())) {
                        Log.e(TAG, "User is not organizer of this event");
                        return Tasks.forResult(false);
                    }

                    // 2. Load waitlist for this event
                    return waitlistModel.getWaitlistEntriesByEvent(eventId)
                            .continueWithTask(waitlistTask -> {
                                List<WaitlistEntry> entries = waitlistTask.getResult();
                                if (entries == null || entries.isEmpty()) {
                                    Log.d(TAG, "No users in waitlist");
                                    return Tasks.forResult(false);
                                }

                                int capacity = event.getTotalCapacity();
                                // You might also track already accepted attendees; for now assume
                                // everyone on waitlist is competing for all capacity.

                                List<String> allUserIds = new ArrayList<>();
                                for (WaitlistEntry e : entries) {
                                    allUserIds.add(e.getUserId());
                                }

                                // Shuffle for randomness
                                Collections.shuffle(allUserIds);

                                // Selected up to capacity
                                List<String> selected = new ArrayList<>();
                                List<String> notSelected = new ArrayList<>();

                                for (int i = 0; i < allUserIds.size(); i++) {
                                    if (i < capacity) {
                                        selected.add(allUserIds.get(i));
                                    } else {
                                        notSelected.add(allUserIds.get(i));
                                    }
                                }

                                // 3. Save lottery result document
                                LotteryResult result = new LotteryResult(
                                        eventId,
                                        currentUserId,
                                        selected,
                                        notSelected
                                );

                                return lotteryModel.saveLotteryResult(result)
                                        .continueWith(saveTask -> {
                                            if (!saveTask.isSuccessful()) {
                                                Log.e(TAG, "Failed saving lottery", saveTask.getException());
                                                return false;
                                            }
                                            
                                            // Calculate Expiry Time: 10% of remaining time until event starts
                                            long currentTime = System.currentTimeMillis();
                                            long eventTime = event.getDateTime();
                                            long timeUntilEvent = eventTime - currentTime;
                                            
                                            long expiryTime = 0;
                                            if (timeUntilEvent > 0) {
                                                // 10% of remaining time
                                                long allowedResponseTime = (long) (timeUntilEvent * 0.10);
                                                expiryTime = currentTime + allowedResponseTime;
                                            } else {
                                                // Event already started? Should we allow? Assume immediate expiry or small buffer.
                                                // For logic sake, if event passed, expiry is now.
                                                expiryTime = currentTime; 
                                            }

                                            // Create notifications for selected users
                                            for (String userId : selected) {
                                                Notifications notification = new Notifications(
                                                    true,
                                                    "Congratulations! You have been selected for the event: " + event.getTitle(),
                                                    currentUserId,
                                                    userId,
                                                    System.currentTimeMillis(),
                                                    eventId
                                                );
                                                notification.setExpiryTimestamp(expiryTime); // Set Expiry
                                                db.collection("notifications").add(notification);
                                            }

                                            // Create notifications for not selected users
                                            for (String userId : notSelected) {
                                                Notifications notification = new Notifications(
                                                    false,
                                                    "Unfortunately, you were not selected for the event: " + event.getTitle(),
                                                    currentUserId,
                                                    userId,
                                                    System.currentTimeMillis(),
                                                    eventId
                                                );
                                                db.collection("notifications").add(notification);
                                            }

                                            // Close the event registration
                                            event.setStatus("closed");
                                            eventModel.saveEvent(event);

                                            Log.d(TAG, "Lottery completed for event: " + eventId);
                                            return true;
                                        });
                            });
                });
    }
    
    public Task<Boolean> redrawLottery(String eventId, String currentUserId) {
        return runLottery(eventId, currentUserId);
    }

    /**
     * Handle logic when a user declines a spot.
     * 1. Remove user from selected list (conceptually, waitlist entry removed by WaitlistController)
     * 2. Find a replacement from the pool of non-selected users.
     * 3. Update LotteryResult to track the declined user.
     *
     * @param eventId  The event ID
     * @param declinedUserId The ID of the user who declined
     * @return Task<Boolean> indicating success/failure of finding replacement
     */
    public Task<Boolean> processDecline(String eventId, String declinedUserId) {
        if (eventId == null || declinedUserId == null) {
            return Tasks.forResult(false);
        }

        // 1. Get the event to get capacity and organizer ID (for notification sender)
        return eventModel.getEvent(eventId).continueWithTask(eventTask -> {
            Event event = eventTask.getResult();
            if (event == null) return Tasks.forResult(false);

            String organizerId = event.getOrganizerId();

            // 2. Get current waitlist (user declining should have been removed already or will be)
            
            return waitlistModel.getWaitlistEntriesByEvent(eventId).continueWithTask(waitlistTask -> {
                List<WaitlistEntry> entries = waitlistTask.getResult();
                // entries might be empty if declined user was the last one and removed, but we need to find replacement.
                // If entries is empty, we can't find replacement.
                // Note: declined user is removed from waitlist BEFORE this is called in NotificationsAdapter.
                
                // 3. Fetch and UPDATE the LotteryResult
                return lotteryModel.getLatestLotteriesForEvent(eventId).continueWithTask(lotteryTask -> {
                    List<LotteryResult> lotteries = lotteryTask.getResult();
                    if (lotteries == null || lotteries.isEmpty()) {
                         Log.e(TAG, "No lottery result found to update.");
                         return Tasks.forResult(false);
                    }
                    
                    LotteryResult lastResult = lotteries.get(0);
                    List<String> selectedIds = lastResult.getSelectedUserIds();
                    List<String> declinedIds = lastResult.getDeclinedUserIds();
                    if (declinedIds == null) declinedIds = new ArrayList<>();
                    
                    // Move user to declined
                    if (selectedIds != null && selectedIds.contains(declinedUserId)) {
                        selectedIds.remove(declinedUserId);
                    }
                    if (!declinedIds.contains(declinedUserId)) {
                        declinedIds.add(declinedUserId);
                    }
                    
                    lastResult.setSelectedUserIds(selectedIds);
                    lastResult.setDeclinedUserIds(declinedIds);
                    
                    // Find replacement from waitlist entries who are NOT selected and NOT declined
                    // We need to track who is already selected/declined to avoid re-picking them.
                    // Waitlist entries contains EVERYONE currently in waitlist DB.
                    // If declined user was removed from DB, they are not in `entries`.
                    
                    List<String> candidates = new ArrayList<>();
                    List<String> finalSelectedIds = selectedIds != null ? selectedIds : new ArrayList<>();
                    List<String> finalDeclinedIds = declinedIds;

                    if (entries != null) {
                        for (WaitlistEntry entry : entries) {
                            String uid = entry.getUserId();
                            // Candidate if NOT selected AND NOT declined
                            if (!finalSelectedIds.contains(uid) && !finalDeclinedIds.contains(uid)) {
                                candidates.add(uid);
                            }
                        }
                    }

                    boolean replacementFound = false;
                    String luckyWinnerId = null;

                    if (!candidates.isEmpty()) {
                        // Pick one random candidate
                        Collections.shuffle(candidates);
                        luckyWinnerId = candidates.get(0);
                        
                        // Add to selected
                        finalSelectedIds.add(luckyWinnerId);
                        lastResult.setSelectedUserIds(finalSelectedIds);
                        replacementFound = true;
                    } else {
                         Log.d(TAG, "No candidates available to fill the spot.");
                    }
                    
                    // SAVE updated LotteryResult
                    String docId = lastResult.getId();
                    if (docId != null) {
                        db.collection("lotteries").document(docId).set(lastResult);
                    }

                    // 4. Notify the winner if found
                    if (replacementFound && luckyWinnerId != null) {
                        // Calculate Expiry for replacement too
                        long currentTime = System.currentTimeMillis();
                        long eventTime = event.getDateTime();
                        long timeUntilEvent = eventTime - currentTime;
                        long expiryTime = 0;
                        if (timeUntilEvent > 0) {
                            long allowedResponseTime = (long) (timeUntilEvent * 0.10);
                            expiryTime = currentTime + allowedResponseTime;
                        } else {
                            expiryTime = currentTime;
                        }
                        
                        Notifications notification = new Notifications(
                                true,
                                "You have been selected from the waitlist for: " + event.getTitle(),
                                organizerId, 
                                luckyWinnerId,
                                System.currentTimeMillis(),
                                eventId
                        );
                        notification.setExpiryTimestamp(expiryTime);
                        
                        return db.collection("notifications").add(notification).continueWith(nTask -> true);
                    }
                    
                    return Tasks.forResult(replacementFound);
                });
            });
        });
    }
}
