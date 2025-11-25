package com.example.robotic_events_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Event> events = new ArrayList<>();
    private EventRecyclerViewAdapter adapter;
    private boolean isOrganizer = false;
    private FloatingActionButton fabAddEvent;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private LinearLayout rootLayout;
    private ListenerRegistration eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Require login
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        rootLayout = findViewById(R.id.rootLayout);
        fabAddEvent = findViewById(R.id.fab_add_event);
        ImageButton profileButton = findViewById(R.id.profileButton);
        ImageButton notificationButton = findViewById(R.id.notificationButton); // Notification Button
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        Button myEventsButton = findViewById(R.id.my_events_button);
        ImageButton qrButton = findViewById(R.id.qr_button);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        isOrganizer = prefs.getBoolean("isOrganizer", false);

        // Apply theme colors based on role
        if (isOrganizer) {
            // Organizer blue theme
            rootLayout.setBackgroundColor(Color.parseColor("#E3F2FD")); // Light blue background
            toolbar.setBackgroundColor(Color.parseColor("#1976D2")); // Blue toolbar
            myEventsButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1976D2"))); // Blue button
            qrButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1976D2"))); // Blue QR button
            fabAddEvent.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1976D2"))); // Blue FAB
        } else {
            // User purple theme
            rootLayout.setBackgroundColor(Color.parseColor("#F3E5F5")); // Light purple background
            toolbar.setBackgroundColor(Color.parseColor("#7B1FA2")); // Purple toolbar
            myEventsButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#7B1FA2"))); // Purple button
            qrButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#7B1FA2"))); // Purple QR button
        }

        fabAddEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        fabAddEvent.setOnClickListener(v -> startActivity(new Intent(this, EventCreationActivity.class)));

        profileButton.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        // Set click listener for the notification button
        notificationButton.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));

        qrButton.setOnClickListener(v -> startActivity(new Intent(this, QRScanRedirect.class)));

        RecyclerView recyclerView = findViewById(R.id.events_container);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventRecyclerViewAdapter(this, events, isOrganizer);
        recyclerView.setAdapter(adapter);

        SearchView searchBar = findViewById(R.id.search_bar);
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterEvents(query, recyclerView);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvents(newText, recyclerView);
                return false;
            }
        });

        loadEventsFromFirestore();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventListener != null) {
            eventListener.remove();
        }
    }

    private void loadEventsFromFirestore() {
        EventModel eventModel = new EventModel();
        
        // Filter logic: 
        // If Organizer: Show only their own events.
        // If User: Show ALL events.
        
        if (isOrganizer) {
            String currentUserId = auth.getCurrentUser().getUid();
            eventListener = eventModel.addOrganizerEventsListener(currentUserId, (value, error) -> {
                if (error != null) {
                    Log.e("MainActivity", "Listen failed.", error);
                    Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (value != null) {
                    events.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                        events.add(doc.toObject(Event.class));
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        } else {
            // Standard user sees all events
            eventListener = eventModel.addEventsListener((value, error) -> {
                if (error != null) {
                    Log.e("MainActivity", "Listen failed.", error);
                    Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (value != null) {
                    events.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                        events.add(doc.toObject(Event.class));
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void filterEvents(String query, RecyclerView recyclerView) {
        if (query == null || query.trim().isEmpty()) {
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            return;
        }
        ArrayList<Event> filteredEvents = new ArrayList<>();
        for (Event event : events) {
            String title = event.getTitle() != null ? event.getTitle().toLowerCase() : "";
            if (title.contains(query.toLowerCase())) {
                filteredEvents.add(event);
            }
        }
        EventRecyclerViewAdapter filteredAdapter = new EventRecyclerViewAdapter(this, filteredEvents, isOrganizer);
        recyclerView.setAdapter(filteredAdapter);
        filteredAdapter.notifyDataSetChanged();
    }
}
