package com.example.robotic_events_test;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventModel {
    private FirebaseFirestore db;
    private String collectionName;
    private CollectionReference eventsCollection;
    EventModel(String collectionName) {
        this.collectionName = collectionName;
        this.db = FirebaseFirestore.getInstance();
        this.eventsCollection = db.collection(collectionName);

        EventModel eventModel = new EventModel("events_mock");
        eventModel.saveEvent("2", "Community Cooking Workshop", "",
                1732237200000L,
                "Downtown Community Kitchen",
                "", "0", 12, 10.0, "");

    }

    EventModel() {
        this("events");
    }

    public void saveEvent(
            String id,
            String title,
            String description,
            long dateTime,
            String location,
            String category,
            String organizerId,
            int totalCapacity,
            double price,
            String imageUrl
    ) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", id);
        eventData.put("title", title);
        eventData.put("description", description);
        eventData.put("dateTime", dateTime);
        eventData.put("location", location);
        eventData.put("category", category);
        eventData.put("organizerId", organizerId);
        eventData.put("totalCapacity", totalCapacity);
        eventData.put("status", "open");
        eventData.put("price", price);
        eventData.put("imageUrl", imageUrl);
        eventData.put("waitlist", new ArrayList<>());

        if (id != null && !id.isEmpty()) {
            eventsCollection.document(id)
                    .set(eventData)
                    .addOnSuccessListener(aVoid ->
                            Log.d("EventModel", "Event added with ID: " + id))
                    .addOnFailureListener(e ->
                            Log.e("EventModel", "Error adding event: " + e.getMessage(), e));
        } else {
            eventsCollection.add(eventData)
                    .addOnSuccessListener(documentRef -> {
                        String generatedId = documentRef.getId();
                        documentRef.update("id", generatedId);
                        Log.d("EventModel", "Event added with generated ID: " + generatedId);
                    })
                    .addOnFailureListener(e ->
                            Log.e("EventModel", "Error adding event: " + e.getMessage(), e));
        }
    }
}
