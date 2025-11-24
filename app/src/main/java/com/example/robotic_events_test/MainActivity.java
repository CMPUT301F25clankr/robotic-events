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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Event> events = new ArrayList<>();
    private EventRecyclerViewAdapter adapter;
    private boolean isOrganizer = false;
    private FloatingActionButton fabAddEvent;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private LinearLayout rootLayout;  // Changed from ConstraintLayout to LinearLayout

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


    private void loadEventsFromFirestore() {
        EventModel eventModel = new EventModel();
        eventModel.getAllEvents().addOnSuccessListener(eventList -> {
            events.clear();
            events.addAll(eventList);
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load events.", Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "Fetch events error", e);
        });
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
