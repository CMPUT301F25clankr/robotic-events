package com.example.robotic_events_test;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

/**
 * VIEW: Displays event details and handles user interactions
 * Uses WaitlistController for all waitlist operations (MVC pattern)
 */
public class EventDetailActivity extends AppCompatActivity {

    private Event event;
    private FirebaseAuth auth;
    // fields
    private LotteryController lotteryController;
    private Button runLotteryButton;

    private WaitlistController waitlistController;  // CONTROLLER
    private boolean isInWaitlist = false;
    private boolean isOrganizer = false;

    // UI Elements
    private Button joinLeaveWaitlistButton;
    private TextView detailWaitlist;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        lotteryController = new LotteryController();

        auth = FirebaseAuth.getInstance();
        waitlistController = new WaitlistController();  // Initialize controller

        // Get passed event
        event = (Event) getIntent().getSerializableExtra("event");
        if (event == null) {
            Toast.makeText(this, "Error: Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if current user is the organizer
        if (auth.getCurrentUser() != null) {
            isOrganizer = event.getOrganizerId().equals(auth.getCurrentUser().getUid());
        }

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.eventDetailToolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Log.d("EventDetail", "Back button clicked");
            finish();
        });

        // Initialize views
        initializeViews();

        // Populate UI with event data
        populateEventDetails();

        // Load waitlist data
        loadWaitlistData();

        // Setup button listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        detailWaitlist = findViewById(R.id.detailWaitlist);
        joinLeaveWaitlistButton = findViewById(R.id.joinLeaveWaitlistButton);

        FloatingActionButton fabEditEvent = findViewById(R.id.fab_edit_event);
        runLotteryButton = findViewById(R.id.runLotteryButton);
        Button viewLotteryResultsButton = findViewById(R.id.viewLotteryResultsButton);

        // Show edit FAB and lottery buttons only if user is organizer
        if (isOrganizer) {
            fabEditEvent.setVisibility(View.VISIBLE);
            fabEditEvent.setOnClickListener(v -> {
                Toast.makeText(this, "Edit event (not implemented yet)", Toast.LENGTH_SHORT).show();
            });

            runLotteryButton.setVisibility(View.VISIBLE);
            // runLotteryButton click listener is in setupButtonListeners()

            viewLotteryResultsButton.setVisibility(View.VISIBLE);
            viewLotteryResultsButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, LotteryResultActivity.class);
                intent.putExtra("eventId", event.getId());
                startActivity(intent);
            });
        } else {
            fabEditEvent.setVisibility(View.GONE);
            runLotteryButton.setVisibility(View.GONE);
            viewLotteryResultsButton.setVisibility(View.GONE);
        }
    }


    private void populateEventDetails() {
        TextView detailTitle = findViewById(R.id.detailTitle);
        TextView detailWhen = findViewById(R.id.detailWhen);
        TextView detailWhere = findViewById(R.id.detailWhere);
        TextView detailCapacity = findViewById(R.id.detailCapacity);
        TextView detailPrice = findViewById(R.id.detailPrice);
        TextView detailCategory = findViewById(R.id.detailCategory);
        TextView detailOrganizer = findViewById(R.id.detailOrganizer);
        TextView detailDescription = findViewById(R.id.detailDescription);

        detailTitle.setText(event.getTitle());
        detailWhen.setText(DateFormat.format("MMM dd, yyyy h:mm a", event.getDateTime()));
        detailWhere.setText(event.getLocation());
        detailCapacity.setText("Capacity\n" + event.getTotalCapacity());
        detailPrice.setText(String.format("$%.2f", event.getPrice()));
        detailCategory.setText(event.getCategory());
        detailOrganizer.setText("Organizer ID: " + event.getOrganizerId());
        detailDescription.setText(event.getDescription() != null ? event.getDescription() : "No description available");
    }

    private void loadWaitlistData() {
        // Load waitlist count through controller
        waitlistController.getWaitlistCount(event.getId())
                .addOnSuccessListener(count -> {
                    detailWaitlist.setText("Waitlisted\n" + count);
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDetail", "Error loading waitlist count", e);
                    detailWaitlist.setText("Waitlisted\n0");
                });

        // Check if current user is in waitlist
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            waitlistController.isUserInWaitlist(event.getId(), userId)
                    .addOnSuccessListener(inWaitlist -> {
                        isInWaitlist = inWaitlist;
                        updateButtonText();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("EventDetail", "Error checking waitlist status", e);
                        updateButtonText();
                    });
        } else {
            updateButtonText();
        }
    }

    private void setupButtonListeners() {
        joinLeaveWaitlistButton.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "Please log in to join waitlist", Toast.LENGTH_SHORT).show();
                return;
            }
            handleWaitlistToggle();
        });
        runLotteryButton.setOnClickListener(v -> {
            if (!isOrganizer) {
                Toast.makeText(this, "Only the organizer can run the lottery", Toast.LENGTH_SHORT).show();
                return;
            }
            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentUserId = auth.getCurrentUser().getUid();

            lotteryController.runLottery(event.getId(), currentUserId)
                    .addOnSuccessListener(success -> {
                        if (success) {
                            Toast.makeText(this, "Lottery run successfully", Toast.LENGTH_SHORT).show();
                            // Optionally refresh UI, show results, send notifications, etc.
                        } else {
                            Toast.makeText(this, "Lottery could not be run", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error running lottery", Toast.LENGTH_SHORT).show();
                        Log.e("EventDetail", "Error running lottery", e);
                    });
        });

    }

    private void handleWaitlistToggle() {
        String userId = auth.getCurrentUser().getUid();

        if (isInWaitlist) {
            // Leave waitlist through controller
            waitlistController.leaveWaitlist(event.getId(), userId)
                    .addOnSuccessListener(success -> {
                        if (success) {
                            isInWaitlist = false;
                            updateButtonText();
                            refreshWaitlistCount();
                            Toast.makeText(this, "Removed from waitlist", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to leave waitlist", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error leaving waitlist", Toast.LENGTH_SHORT).show();
                        Log.e("EventDetail", "Error leaving waitlist", e);
                    });
        } else {
            // Join waitlist through controller
            waitlistController.joinWaitlist(event.getId(), userId)
                    .addOnSuccessListener(success -> {
                        if (success) {
                            isInWaitlist = true;
                            updateButtonText();
                            refreshWaitlistCount();
                            Toast.makeText(this, "Added to waitlist!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to join waitlist", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error joining waitlist", Toast.LENGTH_SHORT).show();
                        Log.e("EventDetail", "Error joining waitlist", e);
                    });
        }
    }

    private void updateButtonText() {
        joinLeaveWaitlistButton.setText(isInWaitlist ? "Leave Waitlist" : "Join Waitlist");
    }

    private void refreshWaitlistCount() {
        waitlistController.getWaitlistCount(event.getId())
                .addOnSuccessListener(count -> {
                    detailWaitlist.setText("Waitlisted\n" + count);
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDetail", "Error refreshing waitlist count", e);
                });
    }
}
