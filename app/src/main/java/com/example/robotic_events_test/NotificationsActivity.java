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

    private void loadNotifications() {
        db.collection("notifications")
                .whereEqualTo("receiverId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("NotificationsActivity", "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        // Handle document changes for smoother updates
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            Notifications notification = dc.getDocument().toObject(Notifications.class);
                            notification.setId(dc.getDocument().getId());
                            
                            switch (dc.getType()) {
                                case ADDED:
                                    // If we are just loading initial data, we might want to just addAll.
                                    // But since we are using SnapshotListener, this will fire for initial load too.
                                    // To avoid duplication if we clear list, we can just maintain the list.
                                    // However, 'orderBy' ensures order.
                                    // Simplest strategy for RecyclerView with small lists is to clear and addAll,
                                    // but for better performance/animation:
                                    // For simplicity in this context, we'll reload the list or handle additions.
                                    // Let's stick to the clear and add all strategy as implemented before, but slightly optimized?
                                    // Actually, the previous implementation cleared the list every time. That works but flashes.
                                    // Let's keep the simple implementation for now as it ensures consistency.
                                    break;
                                case MODIFIED:
                                    break;
                                case REMOVED:
                                    break;
                            }
                        }
                        
                        // Refill list
                        notificationsList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Notifications notification = doc.toObject(Notifications.class);
                            if (notification != null) {
                                notification.setId(doc.getId());
                                notificationsList.add(notification);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
