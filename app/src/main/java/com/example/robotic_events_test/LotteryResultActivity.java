package com.example.robotic_events_test;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * VIEW: Shows the latest lottery result for an event to the organizer
 */
public class LotteryResultActivity extends AppCompatActivity {

    private TextView selectedCountText;
    private TextView notSelectedCountText;
    private TextView declinedCountText; // NEW
    private RecyclerView selectedUsersRecycler;
    private RecyclerView notSelectedUsersRecycler;
    private RecyclerView declinedUsersRecycler; // NEW

    private LotteryController lotteryController;
    private UserModel userModel;

    private String eventId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lottery_result);

        // Setup toolbar with back button
        MaterialToolbar toolbar = findViewById(R.id.lotteryResultToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lottery Results");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        selectedCountText = findViewById(R.id.selectedCountText);
        notSelectedCountText = findViewById(R.id.notSelectedCountText);
        declinedCountText = findViewById(R.id.declinedCountText); // NEW
        
        selectedUsersRecycler = findViewById(R.id.recyclerSelectedUsers);
        notSelectedUsersRecycler = findViewById(R.id.recyclerNotSelectedUsers);
        declinedUsersRecycler = findViewById(R.id.recyclerDeclinedUsers); // NEW

        selectedUsersRecycler.setLayoutManager(new LinearLayoutManager(this));
        notSelectedUsersRecycler.setLayoutManager(new LinearLayoutManager(this));
        declinedUsersRecycler.setLayoutManager(new LinearLayoutManager(this)); // NEW

        lotteryController = new LotteryController();
        userModel = new UserModel();

        eventId = getIntent().getStringExtra("eventId");
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (eventId == null || currentUserId == null) {
            Toast.makeText(this, "Missing event ID or user not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadLotteryResult();
    }

    private void loadLotteryResult() {
        lotteryController.lotteryModel.getLatestLotteriesForEvent(eventId)
                .addOnSuccessListener(results -> {
                    if (results.isEmpty()) {
                        Toast.makeText(this, "No lottery results found for this event", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    LotteryResult latest = results.get(0);

                    // Update counts
                    selectedCountText.setText("Selected Winners (" + (latest.getSelectedUserIds() != null ? latest.getSelectedUserIds().size() : 0) + ")");
                    notSelectedCountText.setText("Not Selected (" + (latest.getNotSelectedUserIds() != null ? latest.getNotSelectedUserIds().size() : 0) + ")");
                    declinedCountText.setText("Declined Users (" + (latest.getDeclinedUserIds() != null ? latest.getDeclinedUserIds().size() : 0) + ")"); // NEW

                    // Load user details for selected, not selected, and declined
                    loadUsers(latest.getSelectedUserIds(), selectedUsersRecycler);
                    loadUsers(latest.getNotSelectedUserIds(), notSelectedUsersRecycler);
                    loadUsers(latest.getDeclinedUserIds(), declinedUsersRecycler); // NEW
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load results: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("LotteryResultActivity", "Error loading lottery", e);
                });
    }

    private void loadUsers(List<String> userIds, RecyclerView recyclerView) {
        if (userIds == null || userIds.isEmpty()) {
            recyclerView.setAdapter(new UserAdapter(new ArrayList<>()));
            return;
        }

        userModel.getUsersByIds(userIds)
                .addOnSuccessListener(users -> {
                    recyclerView.setAdapter(new UserAdapter(users));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("LotteryResultActivity", "Error loading users", e);
                });
    }
}
