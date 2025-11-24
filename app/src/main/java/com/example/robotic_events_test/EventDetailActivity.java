package com.example.robotic_events_test;

import android.annotation.SuppressLint;
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
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class EventDetailActivity extends AppCompatActivity {

    private Event event;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean isInWaitlist = false;
    private boolean isOrganizer = false;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

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

        // Setup Toolbar with back button
        MaterialToolbar toolbar = findViewById(R.id.eventDetailToolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Log.d("EventDetail", "Back button clicked");
            finish();
        });

        // Views from XML
        ImageView detailImage = findViewById(R.id.detailImage);
        TextView detailTitle = findViewById(R.id.detailTitle);
        TextView detailWhen = findViewById(R.id.detailWhen);
        TextView detailWhere = findViewById(R.id.detailWhere);
        TextView detailWaitlist = findViewById(R.id.detailWaitlist);
        TextView detailCapacity = findViewById(R.id.detailCapacity);
        TextView detailPrice = findViewById(R.id.detailPrice);
        TextView detailCategory = findViewById(R.id.detailCategory);
        TextView detailOrganizer = findViewById(R.id.detailOrganizer);
        TextView detailDescription = findViewById(R.id.detailDescription);
        Button joinLeaveWaitlistButton = findViewById(R.id.joinLeaveWaitlistButton);
        FloatingActionButton fabEditEvent = findViewById(R.id.fab_edit_event);

        // Show edit FAB only if user is organizer
        if (isOrganizer) {
            fabEditEvent.setVisibility(View.VISIBLE);
            fabEditEvent.setOnClickListener(v -> {
                // TODO: Navigate to EventEditActivity
                Toast.makeText(this, "Edit event (not implemented yet)", Toast.LENGTH_SHORT).show();
            });
        } else {
            fabEditEvent.setVisibility(View.GONE);
        }

        // Populate UI
        detailTitle.setText(event.getTitle());
        detailWhen.setText(DateFormat.format("MMM dd, yyyy h:mm a", event.getDateTime()));
        detailWhere.setText(event.getLocation());

        int waitlistSize = (event.getWaitlist() != null ? event.getWaitlist().size() : 0);
        detailWaitlist.setText("Waitlisted\n" + waitlistSize);
        detailCapacity.setText("Capacity\n" + event.getTotalCapacity());
        detailPrice.setText(String.format("$%.2f", event.getPrice()));
        detailCategory.setText(event.getCategory());
        detailOrganizer.setText("Organizer ID: " + event.getOrganizerId());
        detailDescription.setText(event.getDescription() != null ? event.getDescription() : "No description available");

        // Check if user is already in waitlist
        checkWaitlistStatus(joinLeaveWaitlistButton);

        // Waitlist button
        joinLeaveWaitlistButton.setOnClickListener(v -> {
            Log.d("EventDetail", "Waitlist button clicked");
            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "Please log in to join waitlist", Toast.LENGTH_SHORT).show();
                return;
            }
            toggleWaitlist(joinLeaveWaitlistButton, detailWaitlist);
        });

        // TODO: Load event image using Glide or Picasso if imageUrl exists
        // if (event.getImageUrl() != null) {
        //     Glide.with(this).load(event.getImageUrl()).into(detailImage);
        // }
    }

    private void checkWaitlistStatus(Button button) {
        if (auth.getCurrentUser() == null) {
            button.setText("Join Waitlist");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        List<String> waitlist = event.getWaitlist();

        if (waitlist != null && waitlist.contains(userId)) {
            isInWaitlist = true;
            button.setText("Leave Waitlist");
        } else {
            isInWaitlist = false;
            button.setText("Join Waitlist");
        }
    }

    private void toggleWaitlist(Button button, TextView waitlistTextView) {
        String userId = auth.getCurrentUser().getUid();

        if (isInWaitlist) {
            // Remove from waitlist
            db.collection("events").document(event.getId())
                    .update("waitlist", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
                    .addOnSuccessListener(aVoid -> {
                        isInWaitlist = false;
                        button.setText("Join Waitlist");
                        Toast.makeText(this, "Removed from waitlist", Toast.LENGTH_SHORT).show();

                        // Update local event object and UI
                        if (event.getWaitlist() != null) {
                            event.getWaitlist().remove(userId);
                            int newSize = event.getWaitlist().size();
                            waitlistTextView.setText("Waitlisted\n" + newSize);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to leave waitlist", Toast.LENGTH_SHORT).show();
                        Log.e("EventDetail", "Error removing from waitlist", e);
                    });
        } else {
            // Add to waitlist
            db.collection("events").document(event.getId())
                    .update("waitlist", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
                    .addOnSuccessListener(aVoid -> {
                        isInWaitlist = true;
                        button.setText("Leave Waitlist");
                        Toast.makeText(this, "Added to waitlist!", Toast.LENGTH_SHORT).show();

                        // Update local event object and UI
                        if (event.getWaitlist() == null) {
                            event.setWaitlist(new ArrayList<>());
                        }
                        event.getWaitlist().add(userId);
                        int newSize = event.getWaitlist().size();
                        waitlistTextView.setText("Waitlisted\n" + newSize);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to join waitlist", Toast.LENGTH_SHORT).show();
                        Log.e("EventDetail", "Error adding to waitlist", e);
                    });
        }
    }
}
