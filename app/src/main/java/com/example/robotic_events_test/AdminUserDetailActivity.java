package com.example.robotic_events_test;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;


/** Displays the clicked user details and lets admin ban*/
public class AdminUserDetailActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private TextView organizerStatusTextView;
    private Button banButton;

    private FirebaseFirestore db;
    private String userId;
    private boolean isBanned;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_detail);

        MaterialToolbar toolbar = findViewById(R.id.adminUserDetailToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();

        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        organizerStatusTextView = findViewById(R.id.organizerStatusTextView);
        banButton = findViewById(R.id.banButton);

        userId = getIntent().getStringExtra("USER_ID");
        //load user from userid
        if (userId != null) {
            loadUserDetails(userId);
        }

        banButton.setOnClickListener(v -> toggleBanStatus());
    }

    //loads users details
    private void loadUserDetails(String userId) {
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    nameTextView.setText(user.getName());
                    emailTextView.setText(user.getEmail());
                    phoneTextView.setText(user.getPhone());
                    organizerStatusTextView.setText(user.isOrganizer() ? "Organizer" : "Regular User");
                    profileImageView.setImageResource(R.drawable.ic_profile); // Placeholder

                    isBanned = user.isBanned();
                    updateBanButton();
                }
            }
        });
    }
    //toggles the ban status
    private void toggleBanStatus() {
        boolean newBanStatus = !isBanned;
        db.collection("users").document(userId).update("banned", newBanStatus)
                .addOnSuccessListener(aVoid -> {
                    isBanned = newBanStatus;
                    Toast.makeText(AdminUserDetailActivity.this, "User status updated", Toast.LENGTH_SHORT).show();
                    updateBanButton();
                })
                .addOnFailureListener(e -> Toast.makeText(AdminUserDetailActivity.this, "Failed to update user status", Toast.LENGTH_SHORT).show());
    }
    //changes the ban button color when clicked
    private void updateBanButton() {
        if (isBanned) {
            banButton.setText("UNBAN");
            banButton.setBackgroundColor(Color.parseColor("#4CAF50")); // Green color
        } else {
            banButton.setText("BAN");
            banButton.setBackgroundColor(Color.RED);
        }
    }
}
