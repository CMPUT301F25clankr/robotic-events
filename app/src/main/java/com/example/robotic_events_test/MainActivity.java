package com.example.robotic_events_test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ArrayList<Event> events = new ArrayList<>();
    private ImageButton profileButton;
    // for organizers
    private FloatingActionButton fabAddEvent;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.events_container);
        EventRecyclerViewAdapter adapter = new EventRecyclerViewAdapter(this, events);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        profileButton = findViewById(R.id.profileButton);

        // grab events from model
        EventModel eventModel = new EventModel();
        Task<List<Event>> allEvents = eventModel.getAllEvents();
        allEvents.addOnSuccessListener(eventList -> {
            this.events.addAll(eventList);
            adapter.notifyDataSetChanged();
        });

        // FAB SETUP FOR ORGANIZER
        fabAddEvent = findViewById(R.id.fab_add_event);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupFabForOrganizer(); // makes it visible if user is organizer

        fabAddEvent.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EventCreationActivity.class);
            startActivity(intent);
        });
        // END FAB SETUP FOR OGANIZER

        fabAddEvent.setOnClickListener(view -> {
            // Start the EventCreationActivity
            Intent intent = new Intent(MainActivity.this, EventCreationActivity.class);
            startActivity(intent);
        });

        androidx.appcompat.widget.SearchView searchBar = findViewById(R.id.search_bar);
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<Event> filteredEvents = new ArrayList<>();
                EventRecyclerViewAdapter filteredAdapter = new EventRecyclerViewAdapter(MainActivity.this, filteredEvents);

                if (query.isEmpty()) {
                    recyclerView.setAdapter(adapter);
                }

                for (Event event : events) {
                    String title = event.getTitle().toLowerCase();
                    if (title.contains(query.toLowerCase())) {
                        filteredEvents.add(event);
                    }
                }
                filteredAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(filteredAdapter);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<Event> filteredEvents = new ArrayList<>();
                EventRecyclerViewAdapter filteredAdapter = new EventRecyclerViewAdapter(MainActivity.this, filteredEvents);

                if (newText.isEmpty()) {
                    recyclerView.setAdapter(adapter);
                }

                for (Event event : events) {
                    String title = event.getTitle().toLowerCase();
                    if (title.contains(newText.toLowerCase())) {
                        filteredEvents.add(event);
                    }
                }
                filteredAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(filteredAdapter);
                return false;
            }
        });
        profileButton.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

    }

    private void setupFabForOrganizer() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            if (user != null && user.isOrganizer()) {
                fabAddEvent.setVisibility(View.VISIBLE);
            } else {
                fabAddEvent.setVisibility(View.GONE);
            }
        });
    }
}