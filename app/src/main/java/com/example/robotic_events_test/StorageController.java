package com.example.robotic_events_test;

import android.net.Uri;
import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.UUID;

public class StorageController {
    private final FirebaseStorage storage;
    private static final String TAG = "StorageController";

    public StorageController() {
        this.storage = FirebaseStorage.getInstance();
    }

    /**
     * Uploads an image to a specified folder in Firebase Storage.
     * @param imageUri The local URI of the image file.
     * @param folderName The destination folder in Firebase Storage (e.g., "event_images").
     * @return A Task that, on success, will contain the public download URL of the uploaded image.
     */
    public Task<Uri> uploadImage(Uri imageUri, String folderName) {
        String fileName = UUID.randomUUID().toString();
        StorageReference fileRef = storage.getReference().child(folderName + "/" + fileName);

        return fileRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Image upload failed", task.getException());
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                });
    }
}