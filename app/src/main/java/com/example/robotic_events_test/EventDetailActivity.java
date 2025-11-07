package com.example.robotic_events_test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import android.graphics.Color;
import android.widget.Toast;

public class EventDetailActivity extends AppCompatActivity {
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
    private FloatingActionButton fabEditEvent;

    private String organizerId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        ImageView image   = findViewById(R.id.detailImage);
        TextView title    = findViewById(R.id.detailTitle);
        TextView when     = findViewById(R.id.detailWhen);
        TextView where    = findViewById(R.id.detailWhere);
        TextView price    = findViewById(R.id.detailPrice);

//        TextView status   = findViewById(R.id.detailStatus);
        TextView category = findViewById(R.id.detailCategory);
        TextView org      = findViewById(R.id.detailOrganizer);
        TextView cap      = findViewById(R.id.detailCapacity);
        TextView desc     = findViewById(R.id.detailDescription);
        TextView waitlist = findViewById(R.id.detailWaitlist);

        String id          = getIntent().getStringExtra("id");
        String t           = safe(getIntent().getStringExtra("title"));
        String d           = safe(getIntent().getStringExtra("description"));
        long dateTime      = getIntent().getLongExtra("dateTime", 0L);
        String loc         = safe(getIntent().getStringExtra("location"));
        String cat         = safe(getIntent().getStringExtra("category"));
        organizerId        = safe(getIntent().getStringExtra("organizerId"));
        int totalCapacity  = getIntent().getIntExtra("totalCapacity", 0);
        String st          = safe(getIntent().getStringExtra("status"));
        int imgResId       = getIntent().getIntExtra("imageResId", 0);
        double pr          = getIntent().getDoubleExtra("price", 0.0);

        fabEditEvent = findViewById(R.id.fab_edit_event);
        setupFabForOrganizer();
        fabEditEvent.setOnClickListener(view -> {
            Intent intent = new Intent(EventDetailActivity.this, EventEditActivity.class);
            intent.putExtra("id", id);
            startActivity(intent);
        });

        // LOCAL (user) waitlistCount - queried once, only modified for local user
        AtomicInteger waitlistCount = new AtomicInteger();

        if (imgResId != 0) image.setImageResource(imgResId);
        title.setText(t.isEmpty() ? "(Untitled Event)" : t);
        when.setText(dateTime > 0 ? sdf.format(new Date(dateTime)) : "");
        where.setText(loc);
        price.setText(pr > 0 ? String.format(Locale.getDefault(), "$%.2f", pr) : "Free");

//        status.setText(st);
        cap.setText(totalCapacity > 0 ? String.format(Locale.getDefault(),
                "Capacity\n%d", totalCapacity) : "Unlimited");


// Get parent
        ViewGroup parent = (ViewGroup) desc.getParent();

        // Delete empty views
        if (organizerId.isEmpty()) { parent.removeView(org); }

        if (d.isEmpty()) { parent.removeView(desc); }

        if (cat.isEmpty()) { parent.removeView(category); }

        org.setText(organizerId);
        desc.setText(d);
        category.setText(cat);

        // Get toolbar; allow navigation back to home page
        MaterialToolbar toolbar = findViewById(R.id.eventDetailToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


        Button joinButton = findViewById(R.id.joinLeaveWaitlistButton);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String docId = id + "_" + userId;

            db.collection("waitlists").document(docId).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            joinButton.setText("Leave Waitlist");
                            joinButton.setBackgroundColor(Color.parseColor("#ff0f0f"));
                        } else {
                            joinButton.setText("Join Waitlist");
                            joinButton.setBackgroundColor(Color.parseColor("#008000"));
                        }
                    });

            //COUNT STORES WAITLIST COUNT
            db.collection("waitlists")
                    .whereEqualTo("eventId", id)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        waitlistCount.set(snapshot.size());
                        waitlist.setText(String.format(Locale.getDefault(), "Waitlisted\n%d", waitlistCount.get()));
                        //Toast.makeText(this, "Total waitlisted: " + waitlistCount, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Log.e("Waitlist", "Error fetching waitlist count", e)
                    );

        }

        joinButton.setOnClickListener(v -> {
            if (currentUser == null) return;
            if (waitlistDebounce) return; // ignore rapid clicks
            waitlistDebounce = true;

            String userId = currentUser.getUid();
            String docId = id + "_" + userId;
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("waitlists").document(docId).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            //remove from wait list
                            db.collection("waitlists").document(docId).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        joinButton.setText("Join Waitlist");
                                        joinButton.setBackgroundColor(Color.parseColor("#008000"));
                                        waitlistCount.set(waitlistCount.get() - 1);
                                        waitlist.setText(String.format(Locale.getDefault(), "Waitlisted\n%d", waitlistCount.get()));
                                        //Toast.makeText(this, "Removed from waitlist", Toast.LENGTH_SHORT).show();

                                        // Debounce
                                        new Handler(Looper.getMainLooper()).postDelayed(() -> waitlistDebounce = false, 500);
                                    });
                        } else {
                            // add to waitlist
                            Waitlist entry = new Waitlist(id, userId, System.currentTimeMillis());
                            db.collection("waitlists").document(docId).set(entry)
                                    .addOnSuccessListener(aVoid -> {
                                        joinButton.setText("Leave Waitlist");
                                        joinButton.setBackgroundColor(Color.parseColor("#ff0f0f"));
                                        waitlistCount.set(waitlistCount.get() + 1);
                                        waitlist.setText(String.format(Locale.getDefault(), "Waitlisted\n%d", waitlistCount.get()));
                                        //Toast.makeText(this, "Added to waitlist", Toast.LENGTH_SHORT).show();

                                        // Debounce
                                        new Handler(Looper.getMainLooper()).postDelayed(() -> waitlistDebounce = false, 500);
                                    });
                        }
                    });
        });
    }

    private void setupFabForOrganizer() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            if (user != null && user.isOrganizer()) {
                fabEditEvent.setVisibility(View.VISIBLE);
            } else {
                fabEditEvent.setVisibility(View.GONE);
            }
        });/*
        if (organizerId.equals(uid)) {
            fabEditEvent.setVisibility(View.VISIBLE);
        } else {
            fabEditEvent.setVisibility(View.GONE);
        }*/
    }

    private String safe(String s) { return s == null ? "" : s; }
    private boolean waitlistDebounce = false;

}