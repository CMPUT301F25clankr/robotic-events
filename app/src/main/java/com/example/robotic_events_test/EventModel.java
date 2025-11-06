package com.example.robotic_events_test;

import com.google.firebase.firestore.FirebaseFirestore;

public class EventModel {
    private FirebaseFirestore firestore;
    EventModel() {
        this.firestore = FirebaseFirestore.getInstance();
    }}
