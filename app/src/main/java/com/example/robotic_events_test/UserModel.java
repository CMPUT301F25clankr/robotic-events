package com.example.robotic_events_test;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

/**
 * MODEL: Handles all database operations for users
 */
public class UserModel {
    private final CollectionReference usersCollection;
    private static final String TAG = "UserModel";

    public UserModel() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.usersCollection = db.collection("users");
    }

    /**
     * Save or update a user
     */
    public Task<Void> saveUser(User user) {
        String uid = user.getUid();
        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "User UID cannot be null or empty");
            return Tasks.forException(new IllegalArgumentException("User UID cannot be null"));
        }

        return usersCollection.document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User saved with UID: " + uid);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user", e);
                });
    }

    /**
     * Get a single user by UID
     */
    public Task<User> getUser(String uid) {
        return usersCollection.document(uid).get()
                .continueWith(task -> {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        return document.toObject(User.class);
                    }
                    return null;
                });
    }

    /**
     * Get multiple users by their UIDs
     */
    public Task<List<User>> getUsersByIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        // Firestore "in" query limits to 10 items, so we batch if needed
        if (userIds.size() <= 10) {
            return usersCollection.whereIn("uid", userIds)
                    .get()
                    .continueWith(task -> {
                        if (task.isSuccessful()) {
                            return task.getResult().toObjects(User.class);
                        }
                        return new ArrayList<>();
                    });
        } else {
            // For more than 10 users, batch queries
            List<Task<List<User>>> tasks = new ArrayList<>();
            for (int i = 0; i < userIds.size(); i += 10) {
                int end = Math.min(i + 10, userIds.size());
                List<String> batch = userIds.subList(i, end);

                Task<List<User>> batchTask = usersCollection.whereIn("uid", batch)
                        .get()
                        .continueWith(task -> {
                            if (task.isSuccessful()) {
                                return task.getResult().toObjects(User.class);
                            }
                            return new ArrayList<User>();
                        });
                tasks.add(batchTask);
            }

            // Combine all batch results
            return Tasks.whenAllSuccess(tasks)
                    .continueWith(task -> {
                        List<User> allUsers = new ArrayList<>();
                        for (Object result : task.getResult()) {
                            allUsers.addAll((List<User>) result);
                        }
                        return allUsers;
                    });
        }
    }

    /**
     * Get all users
     */
    public Task<List<User>> getAllUsers() {
        return usersCollection.get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().toObjects(User.class);
                    }
                    return new ArrayList<>();
                });
    }

    /**
     * Delete a user
     */
    public Task<Void> deleteUser(String uid) {
        if (uid == null || uid.isEmpty()) {
            throw new IllegalArgumentException("User UID cannot be null or empty");
        }
        return usersCollection.document(uid).delete();
    }
}
