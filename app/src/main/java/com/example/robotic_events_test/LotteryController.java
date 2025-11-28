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
                                List<String> allUserIds = new ArrayList<>();
                                for (WaitlistEntry e : entries) {
                                    allUserIds.add(e.getUserId());
                                }

                                Collections.shuffle(allUserIds);

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
                                            
                                            // Calculate Expiry Time: 10% of remaining time
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

                                            // Create notifications
                                            for (String userId : selected) {
                                                Notifications notification = new Notifications(
                                                    true,
                                                    "Congratulations! You have been selected for the event: " + event.getTitle(),
                                                    currentUserId,
                                                    userId,
                                                    System.currentTimeMillis(),
                                                    eventId
                                                );
                                                notification.setExpiryTimestamp(expiryTime);
                                                db.collection("notifications").add(notification);
                                            }

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

                                            // Close event
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

    public Task<Boolean> processDecline(String eventId, String declinedUserId) {
        if (eventId == null || declinedUserId == null) {
            return Tasks.forResult(false);
        }

        return eventModel.getEvent(eventId).continueWithTask(eventTask -> {
            Event event = eventTask.getResult();
            if (event == null) return Tasks.forResult(false);

            String organizerId = event.getOrganizerId();

            return waitlistModel.getWaitlistEntriesByEvent(eventId).continueWithTask(waitlistTask -> {
                List<WaitlistEntry> entries = waitlistTask.getResult();
                
                return lotteryModel.getLatestLotteriesForEvent(eventId).continueWithTask(lotteryTask -> {
                    List<LotteryResult> lotteries = lotteryTask.getResult();
                    if (lotteries == null || lotteries.isEmpty()) {
                         return Tasks.forResult(false);
                    }
                    
                    LotteryResult lastResult = lotteries.get(0);
                    List<String> selectedIds = lastResult.getSelectedUserIds();
                    List<String> declinedIds = lastResult.getDeclinedUserIds();
                    if (declinedIds == null) declinedIds = new ArrayList<>();
                    
                    if (selectedIds != null && selectedIds.contains(declinedUserId)) {
                        selectedIds.remove(declinedUserId);
                    }
                    if (!declinedIds.contains(declinedUserId)) {
                        declinedIds.add(declinedUserId);
                    }
                    
                    List<String> candidates = new ArrayList<>();
                    List<String> finalSelectedIds = selectedIds != null ? selectedIds : new ArrayList<>();
                    List<String> finalDeclinedIds = declinedIds;
                    List<String> finalAcceptedIds = lastResult.getAcceptedUserIds() != null ? lastResult.getAcceptedUserIds() : new ArrayList<>();

                    if (entries != null) {
                        for (WaitlistEntry entry : entries) {
                            String uid = entry.getUserId();
                            if (!finalSelectedIds.contains(uid) && !finalDeclinedIds.contains(uid) && !finalAcceptedIds.contains(uid)) {
                                candidates.add(uid);
                            }
                        }
                    }

                    boolean replacementFound = false;
                    String luckyWinnerId = null;

                    if (!candidates.isEmpty()) {
                        Collections.shuffle(candidates);
                        luckyWinnerId = candidates.get(0);
                        finalSelectedIds.add(luckyWinnerId);
                        replacementFound = true;
                    }
                    
                    // Update object
                    lastResult.setSelectedUserIds(finalSelectedIds);
                    lastResult.setDeclinedUserIds(finalDeclinedIds);
                    
                    // Use update() instead of set() for safety
                    String docId = lastResult.getId();
                    if (docId != null) {
                        db.collection("lotteries").document(docId).update(
                            "selectedUserIds", finalSelectedIds,
                            "declinedUserIds", finalDeclinedIds
                        );
                    }

                    if (replacementFound && luckyWinnerId != null) {
                        // Expiry logic...
                        long currentTime = System.currentTimeMillis();
                        long eventTime = event.getDateTime();
                        long timeUntilEvent = eventTime - currentTime;
                        long expiryTime = (timeUntilEvent > 0) ? currentTime + (long)(timeUntilEvent * 0.10) : currentTime;
                        
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

    public Task<Boolean> processAccept(String eventId, String acceptedUserId) {
        if (eventId == null || acceptedUserId == null) {
            return Tasks.forResult(false);
        }

        return lotteryModel.getLatestLotteriesForEvent(eventId).continueWithTask(lotteryTask -> {
            List<LotteryResult> lotteries = lotteryTask.getResult();
            if (lotteries == null || lotteries.isEmpty()) {
                 return Tasks.forResult(false);
            }
            
            LotteryResult lastResult = lotteries.get(0);
            List<String> selectedIds = lastResult.getSelectedUserIds();
            List<String> acceptedIds = lastResult.getAcceptedUserIds();
            if (acceptedIds == null) acceptedIds = new ArrayList<>();
            
            if (selectedIds != null && selectedIds.contains(acceptedUserId)) {
                selectedIds.remove(acceptedUserId);
            }
            if (!acceptedIds.contains(acceptedUserId)) {
                acceptedIds.add(acceptedUserId);
            }
            
            String docId = lastResult.getId();
            if (docId != null) {
                // Use update() for specific fields
                return db.collection("lotteries").document(docId)
                        .update("selectedUserIds", selectedIds, "acceptedUserIds", acceptedIds)
                        .continueWith(t -> true);
            }
            
            return Tasks.forResult(false);
        });
    }
}
