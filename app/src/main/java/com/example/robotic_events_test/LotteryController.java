package com.example.robotic_events_test;

import android.os.Handler;
import android.os.Looper;
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
            if (event == null) {
                return Tasks.forResult(false);
            }

            return waitlistModel.getWaitlistEntriesByEvent(eventId).continueWithTask(waitlistTask -> {
                List<WaitlistEntry> entries = waitlistTask.getResult();
                if (entries == null) {
                    return Tasks.forResult(false);
                }

                List<Task<Void>> notificationTasks = new ArrayList<>();
                for (WaitlistEntry entry : entries) {
                    Notifications notification = new Notifications(
                        false,
                        "A spot has opened up for \"" + event.getTitle() + "\". You've been re-entered into the lottery.",
                        event.getOrganizerId(),
                        entry.getUserId(),
                        System.currentTimeMillis(),
                        eventId
                    );
                    notificationTasks.add(db.collection("notifications").add(notification).continueWith(task -> null));
                }

                return Tasks.whenAll(notificationTasks).continueWithTask(task -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        runLottery(eventId, event.getOrganizerId());
                    }, 10000);
                    return Tasks.forResult(true);
                });
            });
        });
    }
}
