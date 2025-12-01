package com.example.robotic_events_test;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;

/**
 * VIEW: Allows users to view and manage their notifications (received from event status changes,
 * admins, etc.)
 */
public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private ArrayList<Notifications> notificationsList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }
        currentUserId = auth.getCurrentUser().getUid();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.notifications_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationsList = new ArrayList<>();
        adapter = new NotificationsAdapter(this, notificationsList);
        recyclerView.setAdapter(adapter);

        loadNotifications();
    }

    // Queries and loads the user's notifications for display.
    private void loadNotifications() {
        // We remove the orderBy clause to avoid needing a composite index in Firestore.
        // We will sort the results client-side instead.
        db.collection("notifications")
                .whereEqualTo("receiverId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("NotificationsActivity", "Listen failed.", error);
                        Toast.makeText(this, "Error loading notifications: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        notificationsList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Notifications notification = doc.toObject(Notifications.class);
                            if (notification != null) {
                                notification.setId(doc.getId());
                                notificationsList.add(notification);
                            }
                        }
                        
                        // Client-side sort: Newest first
                        Collections.sort(notificationsList, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));
                        
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
