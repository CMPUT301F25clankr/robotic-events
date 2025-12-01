package com.example.robotic_events_test;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
/**
 * MODEL: Lottery model
 * performs operations on the db relating to lotteries
 */

public class LotteryModel {
    private final CollectionReference lotteriesCollection;
    private static final String TAG = "LotteryModel";

    public LotteryModel() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.lotteriesCollection = db.collection("lotteries");
    }
    /**
     * Get the latest lottery result for an event
     * @param eventId The ID of the event
     * @return A task that completes with the latest lottery result
     */
    public Task<List<LotteryResult>> getLatestLotteriesForEvent(String eventId) {
        return lotteriesCollection
                .whereEqualTo("eventId", eventId)
                .orderBy("runAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObjects(LotteryResult.class);
                    }
                    return new ArrayList<>();
                });
    }

    /**
     * Save a lottery result to the database
     * @param result The lottery result to save
     * @return A task that completes when the result is saved
     */
    public Task<String> saveLotteryResult(LotteryResult result) {
        return lotteriesCollection.add(result)
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        String id = task.getResult().getId();
                        lotteriesCollection.document(id).update("id", id);
                        Log.d(TAG, "Lottery saved: " + id);
                        return id;
                    } else {
                        Log.e(TAG, "Error saving lottery", task.getException());
                        throw task.getException();
                    }
                });
    }
}
