package com.example.robotic_events_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Event> events = new ArrayList<>();
    private EventRecyclerViewAdapter adapter;
    private boolean isOrganizer = false;
    private FloatingActionButton fabAddEvent;
    private FirebaseAuth auth;
    private LinearLayout rootLayout;
    private ListenerRegistration eventListener;
    private ActivityResultLauncher<Intent> filterLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

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
        ImageButton notificationButton = findViewById(R.id.notificationButton);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        Button myEventsButton = findViewById(R.id.my_events_button);
        ImageButton qrButton = findViewById(R.id.qr_button);
        ImageButton filterButton = findViewById(R.id.filter_button);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        isOrganizer = prefs.getBoolean("isOrganizer", false);

        if (isOrganizer) {
            rootLayout.setBackgroundColor(Color.parseColor("#E3F2FD"));
            toolbar.setBackgroundColor(Color.parseColor("#1976D2"));
            myEventsButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1976D2")));
            qrButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1976D2")));
            fabAddEvent.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1976D2")));
        } else {
            rootLayout.setBackgroundColor(Color.parseColor("#F3E5F5"));
            toolbar.setBackgroundColor(Color.parseColor("#7B1FA2"));
            myEventsButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#7B1FA2")));
            qrButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#7B1FA2")));
        }

        fabAddEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        fabAddEvent.setOnClickListener(v -> startActivity(new Intent(this, EventCreationActivity.class)));

        profileButton.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
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
                filterEvents(query, null, "any", -1, 0, 0);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvents(newText, null, "any", -1, 0, 0);
                return false;
            }
        });

        filterLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String category = result.getData().getStringExtra("category");
                    String price = result.getData().getStringExtra("price");
                    float distance = result.getData().getFloatExtra("distance", -1);
                    double latitude = result.getData().getDoubleExtra("latitude", 0);
                    double longitude = result.getData().getDoubleExtra("longitude", 0);
                    filterEvents(searchBar.getQuery().toString(), category, price, distance, latitude, longitude);
                }
            });

        filterButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, FilterActivity.class);
            filterLauncher.launch(intent);
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
<<<<<<< Updated upstream
<<<<<<< Updated upstream

        // Safety check: Ensure user is logged in before accessing UID
        if (auth.getCurrentUser() == null) {
            // This should theoretically not happen due to onCreate check, but safe to return
            return;
        }
        
        // Filter logic: 
        // If Organizer: Show only their own events.
        // If User: Show ALL events.
        
=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
        if (isOrganizer) {
            String currentUserId = auth.getCurrentUser().getUid();
            eventListener = eventModel.addOrganizerEventsListener(currentUserId, (value, error) -> {
                if (error != null) {
                    Log.e("MainActivity", "Listen failed.", error);
<<<<<<< Updated upstream
<<<<<<< Updated upstream
                    Toast.makeText(this, "Error loading events: " + error.getMessage(), Toast.LENGTH_SHORT).show();
=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
                    return;
                }
                if (value != null) {
                    events.clear();
                    value.forEach(doc -> events.add(doc.toObject(Event.class)));
                    adapter.notifyDataSetChanged();
                }
            });
        } else {
            eventListener = eventModel.addEventsListener((value, error) -> {
                if (error != null) {
                    Log.e("MainActivity", "Listen failed.", error);
<<<<<<< Updated upstream
<<<<<<< Updated upstream
                    Toast.makeText(this, "Error loading events: " + error.getMessage(), Toast.LENGTH_SHORT).show();
=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
                    return;
                }
                if (value != null) {
                    events.clear();
                    value.forEach(doc -> events.add(doc.toObject(Event.class)));
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void filterEvents(String query, String category, String price, float distance, double latitude, double longitude) {
        RecyclerView recyclerView = findViewById(R.id.events_container);
        ArrayList<Event> filteredEvents = new ArrayList<>();
        for (Event event : events) {
            boolean matches = true;

            if (query != null && !query.trim().isEmpty()) {
                String title = event.getTitle() != null ? event.getTitle().toLowerCase() : "";
                String location = event.getLocation() != null ? event.getLocation().toLowerCase() : "";
                if (!title.contains(query.toLowerCase()) && !location.contains(query.toLowerCase())) {
                    matches = false;
                }
            }

            if (category != null && !category.trim().isEmpty()) {
                String eventCategory = event.getCategory() != null ? event.getCategory().toLowerCase() : "";
                if (!eventCategory.contains(category.toLowerCase())) {
                    matches = false;
                }
            }

            if (price != null && !price.equals("any")) {
                if (price.equals("free") && event.getPrice() != 0) {
                    matches = false;
                }
                if (price.equals("paid") && event.getPrice() == 0) {
                    matches = false;
                }
            }

            if (distance != -1 && latitude != 0 && longitude != 0) {
                Location eventLocation = new Location("");
                eventLocation.setLatitude(event.getLatitude());
                eventLocation.setLongitude(event.getLongitude());

                Location userLocation = new Location("");
                userLocation.setLatitude(latitude);
                userLocation.setLongitude(longitude);

                if (userLocation.distanceTo(eventLocation) / 1000 > distance) {
                    matches = false;
                }
            }

            if (matches) {
                filteredEvents.add(event);
            }
        }
        EventRecyclerViewAdapter filteredAdapter = new EventRecyclerViewAdapter(this, filteredEvents, isOrganizer);
        recyclerView.setAdapter(filteredAdapter);
        filteredAdapter.notifyDataSetChanged();
    }
}
