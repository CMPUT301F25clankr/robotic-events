package com.example.robotic_events_test;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * VIEW: Shows the latest lottery result for an event to the organizer
 */
public class LotteryResultActivity extends AppCompatActivity {

    private TextView selectedCountText;
    private TextView notSelectedCountText;
    private TextView declinedCountText;
    private RecyclerView selectedUsersRecycler;
    private RecyclerView notSelectedUsersRecycler;
    private RecyclerView declinedUsersRecycler;
    private Button exportButton;

    private LotteryController lotteryController;
    private UserModel userModel;

    private String eventId;
    private String currentUserId;
    private LotteryResult currentResult;

    // Launcher for saving the file
    private ActivityResultLauncher<Intent> createFileLauncher;
    private String pendingCsvContent;

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
        declinedCountText = findViewById(R.id.declinedCountText);
        exportButton = findViewById(R.id.exportButton);
        
        selectedUsersRecycler = findViewById(R.id.recyclerSelectedUsers);
        notSelectedUsersRecycler = findViewById(R.id.recyclerNotSelectedUsers);
        declinedUsersRecycler = findViewById(R.id.recyclerDeclinedUsers);

        selectedUsersRecycler.setLayoutManager(new LinearLayoutManager(this));
        notSelectedUsersRecycler.setLayoutManager(new LinearLayoutManager(this));
        declinedUsersRecycler.setLayoutManager(new LinearLayoutManager(this));

        lotteryController = new LotteryController();
        userModel = new UserModel();

        eventId = getIntent().getStringExtra("eventId");
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (eventId == null || currentUserId == null) {
            Toast.makeText(this, "Missing event ID or user not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Register the file picker launcher
        createFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            writeCsvToUri(uri);
                        }
                    }
                }
        );

        exportButton.setOnClickListener(v -> prepareAndExportCsv());

        loadLotteryResult();
    }

    private void loadLotteryResult() {
        lotteryController.lotteryModel.getLatestLotteriesForEvent(eventId)
                .addOnSuccessListener(results -> {
                    if (results.isEmpty()) {
                        Toast.makeText(this, "No lottery results found for this event", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentResult = results.get(0);

                    // Update counts
                    selectedCountText.setText("Selected Winners (" + (currentResult.getSelectedUserIds() != null ? currentResult.getSelectedUserIds().size() : 0) + ")");
                    notSelectedCountText.setText("Not Selected (" + (currentResult.getNotSelectedUserIds() != null ? currentResult.getNotSelectedUserIds().size() : 0) + ")");
                    declinedCountText.setText("Declined Users (" + (currentResult.getDeclinedUserIds() != null ? currentResult.getDeclinedUserIds().size() : 0) + ")");

                    // Load user details for selected, not selected, and declined
                    loadUsers(currentResult.getSelectedUserIds(), selectedUsersRecycler);
                    loadUsers(currentResult.getNotSelectedUserIds(), notSelectedUsersRecycler);
                    loadUsers(currentResult.getDeclinedUserIds(), declinedUsersRecycler);
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

    private void prepareAndExportCsv() {
        if (currentResult == null || (currentResult.getSelectedUserIds() == null && currentResult.getNotSelectedUserIds() == null)) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        // Combine all users: Selected, Not Selected, Declined
        List<String> allIds = new ArrayList<>();
        if (currentResult.getSelectedUserIds() != null) allIds.addAll(currentResult.getSelectedUserIds());
        if (currentResult.getNotSelectedUserIds() != null) allIds.addAll(currentResult.getNotSelectedUserIds());
        if (currentResult.getDeclinedUserIds() != null) allIds.addAll(currentResult.getDeclinedUserIds());

        if (allIds.isEmpty()) {
            Toast.makeText(this, "No entrants to export", Toast.LENGTH_SHORT).show();
            return;
        }

        userModel.getUsersByIds(allIds).addOnSuccessListener(users -> {
            StringBuilder sb = new StringBuilder();
            // Changed comma separator to semicolon for better compatibility in some regions/Excel versions if needed, 
            // but comma is standard CSV. However, appending new line '\n' is crucial.
            // Also, ensure UTF-8 encoding implicitly.
            sb.append("User ID,Name,Email,Status\n");

            for (User user : users) {
                String status = "Waitlist";
                if (currentResult.getSelectedUserIds() != null && currentResult.getSelectedUserIds().contains(user.getUid())) {
                    status = "Selected";
                } else if (currentResult.getDeclinedUserIds() != null && currentResult.getDeclinedUserIds().contains(user.getUid())) {
                    status = "Declined";
                } else if (currentResult.getNotSelectedUserIds() != null && currentResult.getNotSelectedUserIds().contains(user.getUid())) {
                    status = "Not Selected";
                }

                // Sanitize fields to avoid CSV breakage (e.g. commas in names)
                String name = user.getName() != null ? user.getName().replace(",", " ") : "";
                String email = user.getEmail() != null ? user.getEmail().replace(",", " ") : "";
                String uid = user.getUid() != null ? user.getUid() : "";

                sb.append(uid).append(",")
                        .append(name).append(",")
                        .append(email).append(",")
                        .append(status).append("\n");
            }

            pendingCsvContent = sb.toString();
            launchFilePicker();

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch user data for export", Toast.LENGTH_SHORT).show();
        });
    }

    private void launchFilePicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        // Using a generic MIME type or 'text/plain' sometimes helps if 'text/csv' is not handled well by some viewers
        // But 'text/csv' is correct. 
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "entrants_" + eventId + ".csv");
        createFileLauncher.launch(intent);
    }

    private void writeCsvToUri(Uri uri) {
        try {
            // Write bytes directly
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(pendingCsvContent.getBytes());
                outputStream.close();
                Toast.makeText(this, "Export successful!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("LotteryResultActivity", "Export failed", e);
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
