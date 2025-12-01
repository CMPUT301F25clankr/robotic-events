package com.example.robotic_events_test;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Event> events = new ArrayList<>();
    private EventRecyclerViewAdapter adapter;
    private boolean isOrganizer = false;
    private FloatingActionButton fabAddEvent;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private LinearLayout rootLayout;
    private ListenerRegistration eventListener;
    private boolean showMyEventsOnly = false; // Track "My Events" state


    // Filtering info
    private String searchQuery = "";
    private ArrayList<String> selectedCategories = new ArrayList<>();
    private boolean filterDate = false;
    private boolean filterTime = false;
    private Calendar selectedDT = Calendar.getInstance();

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
        ImageButton filterButton = findViewById(R.id.filter_button);
        LinearLayout filterCols = findViewById(R.id.filter_categories);
        LinearLayout filterDateTime = findViewById(R.id.filter_date_time);

        Button filterDateButton = findViewById(R.id.filter_date_button);
        Button filterTimeButton = findViewById(R.id.filter_time_button);

        // Get the filter bar container to hide it for organizers
        ConstraintLayout filterBar = findViewById(R.id.filter_bar);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        isOrganizer = prefs.getBoolean("isOrganizer", false);

        RecyclerView recyclerView = findViewById(R.id.events_container);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventRecyclerViewAdapter(this, events, isOrganizer);
        recyclerView.setAdapter(adapter);

        // Apply theme colors based on role
        if (isOrganizer) {
            // Organizer blue theme
            rootLayout.setBackgroundColor(Color.parseColor("#E3F2FD")); // Light blue background
            toolbar.setBackgroundColor(Color.parseColor("#1976D2")); // Blue toolbar
            myEventsButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1976D2"))); // Blue button
            qrButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1976D2"))); // Blue QR button
            fabAddEvent.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1976D2"))); // Blue FAB

            // HIDE QR AND MY EVENTS BUTTONS FOR ORGANIZER
            qrButton.setVisibility(View.GONE);
            myEventsButton.setVisibility(View.GONE);

            // NEW: Hide the filter bar entirely for organizers
            if (filterBar != null) {
                filterBar.setVisibility(View.GONE);
            }

        } else {
            // User purple theme
            rootLayout.setBackgroundColor(Color.parseColor("#F3E5F5")); // Light purple background
            toolbar.setBackgroundColor(Color.parseColor("#7B1FA2")); // Purple toolbar
            myEventsButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#7B1FA2"))); // Purple button
            qrButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#7B1FA2"))); // Purple QR button
            filterButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#7B1FA2"))); // Purple button
            filterBar.setBackgroundColor(Color.parseColor("#F3E5F5"));
            filterDateButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#7B1FA2")));  // Purple button
            filterTimeButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#7B1FA2")));  // Purple button

            // Ensure they are visible for regular users
            qrButton.setVisibility(View.VISIBLE);
            myEventsButton.setVisibility(View.VISIBLE);

            if (filterBar != null) {
                filterBar.setVisibility(View.VISIBLE);
                setupFilter(recyclerView);
                filterCols.setVisibility(View.GONE);
            }

            filterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (filterCols.getVisibility() == View.VISIBLE) {
                        filterCols.setVisibility(View.GONE);
                        filterDateTime.setVisibility(View.GONE);
                    } else {
                        filterCols.setVisibility(View.VISIBLE);
                        filterDateTime.setVisibility(View.VISIBLE);
                    }
                }
            });
            
            myEventsButton.setOnClickListener(v -> {
                showMyEventsOnly = !showMyEventsOnly; // Toggle state
                if (showMyEventsOnly) {
                    myEventsButton.setText("All Events");
                    // Maybe change color to indicate active filter?
                } else {
                    myEventsButton.setText("My Events");
                }
                applyAllFilters(recyclerView);
            });

            setupDTFilters(recyclerView, filterDateButton, filterTimeButton);
        }

        fabAddEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        fabAddEvent.setOnClickListener(v -> startActivity(new Intent(this, EventCreationActivity.class)));

        profileButton.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        // Set click listener for the notification button
        notificationButton.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));

        qrButton.setOnClickListener(v -> startActivity(new Intent(this, QRScanRedirect.class)));

        SearchView searchBar = findViewById(R.id.search_bar);
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                applyAllFilters(recyclerView);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQuery = newText;
                applyAllFilters(recyclerView);
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

        // Safety check: Ensure user is logged in before accessing UID
        if (auth.getCurrentUser() == null) {
            // This should theoretically not happen due to onCreate check, but safe to return
            return;
        }

        // Filter logic: 
        // If Organizer: Show only their own events.
        // If User: Show ALL events.

        if (isOrganizer) {
            String currentUserId = auth.getCurrentUser().getUid();
            eventListener = eventModel.addOrganizerEventsListener(currentUserId, (value, error) -> {
                if (error != null) {
                    Log.e("MainActivity", "Listen failed.", error);
                    Toast.makeText(this, "Error loading events: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "Error loading events: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void applyAllFilters(RecyclerView recyclerView) {
        // Combined filter method - filters all of search query strings, categories, date, time as needed by user
        ArrayList<Event> filtered = new ArrayList<>();

        long selectedMills = selectedDT.getTimeInMillis();
        long selectedDate = getEventDateFromMill(selectedMills);
        int selectedTimeMins = getTimeMinsFromMill(selectedMills);
        
        // Need WaitlistController to check "My Events"
        // This is inefficient in a loop (async call per item). 
        // Better approach: Fetch user's waitlist IDs ONCE, then filter.
        // However, `applyAllFilters` is synchronous.
        // We can't easily do async check inside this loop without refactoring.
        // For now, if showMyEventsOnly is true, we might need to reload data or use a different strategy.
        
        if (showMyEventsOnly) {
             // Async fetch of my events is needed.
             // For simplicity here, we will just fetch the user's waitlist entries and filter the main list.
             // But `applyAllFilters` is called on search/text change.
             // We should probably fetch "myEventIds" when the toggle is clicked or cache them.
             
             // Let's implement a quick fetch and then filter.
             WaitlistController wc = new WaitlistController();
             String userId = auth.getCurrentUser().getUid();
             wc.getUserWaitlists(userId).addOnSuccessListener(entries -> {
                 ArrayList<String> myEventIds = new ArrayList<>();
                 for (WaitlistEntry entry : entries) {
                     myEventIds.add(entry.getEventId());
                 }
                 
                 // Now filter
                 filterListWithMyEvents(recyclerView, myEventIds, selectedDate, selectedTimeMins);
             });
             return; // Exit and let the callback handle the update
        }

        // Standard filtering
        filterListWithMyEvents(recyclerView, null, selectedDate, selectedTimeMins);
    }
    
    private void filterListWithMyEvents(RecyclerView recyclerView, ArrayList<String> myEventIds, long selectedDate, int selectedTimeMins) {
        ArrayList<Event> filtered = new ArrayList<>();
        
        for (Event e : events) {
            // Search all events
            String title = e.getTitle() != null ? e.getTitle().toLowerCase() : "";
            long eDT = e.getDateTime();
            long eDate = getEventDateFromMill(eDT);
            int eTime = getTimeMinsFromMill(eDT);

            boolean passes = true;
            
            // My Events Filter
            if (myEventIds != null && !myEventIds.contains(e.getId())) {
                passes = false;
            }

            if (!searchQuery.isEmpty() && !title.contains(searchQuery.toLowerCase()))
                // Filter by event category/interest
                passes = false;

            if (!selectedCategories.isEmpty()) {
                // Filter by category
                String cat = e.getCategory() != null ? e.getCategory().toLowerCase() : "";
                boolean anyMatch = false;
                for (String sel : selectedCategories)
                    if (cat.contains(sel.toLowerCase()))
                        anyMatch = true;
                if (!anyMatch) passes = false;
            }

            if (filterDate && eDate != selectedDate)
                // Filter by date
                passes = false;

            if (filterTime && eTime != selectedTimeMins)
                // Filter by time
                passes = false;

            if (passes) // Only add events passing all filters
                filtered.add(e);
        }

        EventRecyclerViewAdapter adapter = new EventRecyclerViewAdapter(this, filtered, isOrganizer);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    private void setupFilter(RecyclerView recyclerView) {
        // Set up toggle buttons to filter by category
        LinearLayout filterCols = findViewById(R.id.filter_categories);
        String[] categories = {"Sports", "Art", "Food", "Games", "Community"};

        for (String cat : categories) {
            ToggleButton button = new ToggleButton(this);

            button.setTextOn(cat);
            button.setTextOff(cat);
            button.setText(cat);
            button.setTag(cat);
            button.setChecked(false);

            button.setPadding(12, 12, 12, 12);
            button.setBackgroundColor(Color.BLACK);
            button.setTextColor(Color.WHITE);

            // Filter if enabled; do not filter if not
            button.setOnCheckedChangeListener(((buttonView, isChecked) -> {
                if (isChecked) {
                    button.setBackgroundColor(Color.parseColor("#7B1FA2"));
                    selectedCategories.add(cat);
                } else {
                    button.setBackgroundColor(Color.BLACK);
                    selectedCategories.remove(cat);
                }

                applyAllFilters(recyclerView);
            }));

            filterCols.addView(button);
        }
    }

    private void setupDTFilters(RecyclerView recyclerView, Button filterDateButton, Button filterTimeButton) {
        // Set up date and time filter dialog and button logic
        filterDateButton.setOnClickListener(v -> {

            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Set up date picker dialog
            DatePickerDialog dialog = new DatePickerDialog(
                    MainActivity.this,
                    (view, yearSelected, monthSelected, daySelected) -> {
                        selectedDT.set(Calendar.YEAR, yearSelected);
                        selectedDT.set(Calendar.MONTH, monthSelected);
                        selectedDT.set(Calendar.DAY_OF_MONTH, daySelected);
                        filterDate = true;
                        applyAllFilters(recyclerView);
                        filterDateButton.setText("Date: " + (monthSelected + 1)+ "/" + daySelected + "/" + yearSelected);
                    },
                    year, month, day
            );

            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Reset", (d, which) -> {
                filterDate = false;
                filterDateButton.setText("Date: Any");
                applyAllFilters(recyclerView);
            });

            dialog.show();
        });

        // Set up time picker dialog
        filterTimeButton.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);

            TimePickerDialog dialog = new TimePickerDialog(
                    MainActivity.this,
                    (view, hourSelected, minuteSelected) -> {
                        selectedDT.set(Calendar.HOUR_OF_DAY, hourSelected);
                        selectedDT.set(Calendar.MINUTE, minuteSelected);
                        selectedDT.set(Calendar.SECOND, 0);
                        selectedDT.set(Calendar.MILLISECOND, 0);
                        filterTime = true;
                        applyAllFilters(recyclerView);
                        String formattedTime = String.format("Time: %02d:%02d",
                                hourSelected,
                                minuteSelected
                        );
                        filterTimeButton.setText(formattedTime);
                    },
                    hour,
                    min,
                    true
            );

            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Reset", (d, which) -> {
                filterTime = false;
                filterTimeButton.setText("Time: Any");
                applyAllFilters(recyclerView);
            });

            dialog.show();
        });
    }


    private long getEventDateFromMill(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);

        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return  c.getTimeInMillis();
    }

    private int getTimeMinsFromMill(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);

        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
    }
}
