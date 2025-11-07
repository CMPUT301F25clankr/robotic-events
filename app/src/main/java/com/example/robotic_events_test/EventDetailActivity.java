package com.example.robotic_events_test;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.graphics.Color;
import android.widget.Toast;

public class EventDetailActivity extends AppCompatActivity {

    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());

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
                        int count = snapshot.size();
                        Toast.makeText(this, "Total waitlisted: " + count, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Log.e("Waitlist", "Error fetching waitlist count", e)
                    );

        }

        joinButton.setOnClickListener(v -> {
            if (currentUser == null) return;

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
                                        Toast.makeText(this, "Removed from waitlist", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // add to waitlist
                            Waitlist entry = new Waitlist(id, userId, System.currentTimeMillis());
                            db.collection("waitlists").document(docId).set(entry)
                                    .addOnSuccessListener(aVoid -> {
                                        joinButton.setText("Leave Waitlist");
                                        joinButton.setBackgroundColor(Color.parseColor("#ff0f0f"));
                                        Toast.makeText(this, "Added to waitlist", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    });
        });







    }





    private String safe(String s) { return s == null ? "" : s; }
}
