package com.example.robotic_events_test;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailActivity extends AppCompatActivity {

    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());

    private Button joinLeaveWaitlistButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String eventId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageView image   = findViewById(R.id.detailImage);
        TextView title    = findViewById(R.id.detailTitle);
        TextView when     = findViewById(R.id.detailWhen);
        TextView where    = findViewById(R.id.detailWhere);
        TextView price    = findViewById(R.id.detailPrice);
//      TextView status   = findViewById(R.id.detailStatus);
        TextView category = findViewById(R.id.detailCategory);
        TextView org      = findViewById(R.id.detailOrganizer);
        TextView cap      = findViewById(R.id.detailCapacity);
        TextView desc     = findViewById(R.id.detailDescription);

        String id          = getIntent().getStringExtra("id");
        String t           = safe(getIntent().getStringExtra("title"));
        String d           = safe(getIntent().getStringExtra("description"));
        long dateTime      = getIntent().getLongExtra("dateTime", 0L);
        String loc         = safe(getIntent().getStringExtra("location"));
        String cat         = safe(getIntent().getStringExtra("category"));
        String organizerId = safe(getIntent().getStringExtra("organizerId"));
        int totalCapacity  = getIntent().getIntExtra("totalCapacity", 0);
        String st          = safe(getIntent().getStringExtra("status"));
        int imgResId       = getIntent().getIntExtra("imageResId", 0);
        double pr          = getIntent().getDoubleExtra("price", 0.0);

        if (imgResId != 0) image.setImageResource(imgResId);
        title.setText(t.isEmpty() ? "(Untitled)" : t);
        when.setText(dateTime > 0 ? sdf.format(new Date(dateTime)) : "");
        where.setText(loc);
        price.setText(pr > 0 ? String.format(Locale.getDefault(), "$%.2f", pr) : "Free");
//        status.setText(st);
        category.setText(cat);
        org.setText(organizerId);
        cap.setText(String.valueOf(totalCapacity));
        desc.setText(d);

        this.eventId = id;

        joinLeaveWaitlistButton = findViewById(R.id.joinLeaveWaitlistButton);
        joinLeaveWaitlistButton.setOnClickListener(v -> joinLeaveWaitlist());
    }

    private void joinLeaveWaitlist() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(this, "You must be logged in to join the waitlist.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference eventRef = db.collection("events").document(eventId);
        eventRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isOnWaitlist = snapshot.contains("waitlist") &&
                    snapshot.get("waitlist") instanceof java.util.List &&
                    ((java.util.List<?>) snapshot.get("waitlist")).contains(uid);

            // remove from waitlist
            if (isOnWaitlist) {
                eventRef.update("waitlist", FieldValue.arrayRemove(uid))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "You left the waitlist.", Toast.LENGTH_SHORT).show();
                            joinLeaveWaitlistButton.setText("Join Waitlist");
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error leaving waitlist: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } else { //add to waitlist
                eventRef.update("waitlist", FieldValue.arrayUnion(uid))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "You joined the waitlist!", Toast.LENGTH_SHORT).show();
                            joinLeaveWaitlistButton.setText("Leave Waitlist");
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error joining waitlist: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void updateButtonText() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            joinLeaveWaitlistButton.setText("Join Waitlist");
            return;
        }

        db.collection("events").document(this.eventId).get()
                .addOnSuccessListener(snapshot -> {
                    boolean isOnWaitlist = snapshot.contains("waitlist") &&
                            snapshot.get("waitlist") instanceof java.util.List &&
                            ((java.util.List<?>) snapshot.get("waitlist")).contains(uid);

                    if(isOnWaitlist){
                        joinLeaveWaitlistButton.setText("Leave Waitlist");
                    } else {
                        joinLeaveWaitlistButton.setText("Join Waitlist");
                    }

                });
    }

    private String safe(String s) { return s == null ? "" : s; }
}
