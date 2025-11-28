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
            // We need to find someone who is NOT selected yet.
            // For simplicity, let's just look at all waitlist entries.
            // The declined user is supposedly removed from waitlist before this call.
            
            return waitlistModel.getWaitlistEntriesByEvent(eventId).continueWithTask(waitlistTask -> {
                List<WaitlistEntry> entries = waitlistTask.getResult();
                if (entries == null || entries.isEmpty()) {
                    Log.d(TAG, "No users left in waitlist to replace declined user.");
                    return Tasks.forResult(true); // Nothing to do, but not an error
                }

                // In a real app, we need to know who was ALREADY selected to not select them again.
                // We can fetch the latest LotteryResult.
                return lotteryModel.getLatestLotteriesForEvent(eventId).continueWithTask(lotteryTask -> {
                    List<LotteryResult> lotteries = lotteryTask.getResult();
                    List<String> alreadySelectedIds = new ArrayList<>();
                    
                    if (lotteries != null && !lotteries.isEmpty()) {
                        LotteryResult lastResult = lotteries.get(0);
                        if (lastResult.getSelectedUserIds() != null) {
                            alreadySelectedIds.addAll(lastResult.getSelectedUserIds());
                        }
                    }
                    
                    // Filter entries to find candidates (those NOT in alreadySelectedIds)
                    List<String> candidates = new ArrayList<>();
                    for (WaitlistEntry entry : entries) {
                        if (!alreadySelectedIds.contains(entry.getUserId())) {
                            candidates.add(entry.getUserId());
                        }
                    }

                    if (candidates.isEmpty()) {
                         Log.d(TAG, "No candidates available to fill the spot.");
                         return Tasks.forResult(true);
                    }

                    // 3. Pick one random candidate
                    Collections.shuffle(candidates);
                    String luckyWinnerId = candidates.get(0);

                    // 4. Notify the winner
                    Notifications notification = new Notifications(
                            true,
                            "You have been selected from the waitlist for: " + event.getTitle(),
                            organizerId, // Sent by organizer (system behalf)
                            luckyWinnerId,
                            System.currentTimeMillis(),
                            eventId
                    );
                    
                    return db.collection("notifications").add(notification).continueWith(nTask -> {
                         if (nTask.isSuccessful()) {
                             Log.d(TAG, "Replacement found and notified: " + luckyWinnerId);
                             
                             // OPTIONAL: Update LotteryResult to include this new person? 
                             // For now, we just notify them.
                             return true;
                         }
                         return false;
                    });
                });
            });
        });
    }
}
