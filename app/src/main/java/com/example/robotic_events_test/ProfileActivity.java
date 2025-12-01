package com.example.robotic_events_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * VIEW: Allows all types of users to modify attributes about their account, including personal
 * information. Also enables users to delete their accounts/profiles and associated info.
 */
public class ProfileActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, phoneInput, locationInput;
    private Switch notificationsSwitch;
    private Button saveButton, logoutButton, deleteButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        uid = auth.getCurrentUser().getUid();

        nameInput = findViewById(R.id.profileName);
        emailInput = findViewById(R.id.profileEmail);
        phoneInput = findViewById(R.id.profilePhone);
        locationInput = findViewById(R.id.profileLocation);
        notificationsSwitch = findViewById(R.id.profileNotificationsSwitch);

        saveButton = findViewById(R.id.profileSaveButton);
        logoutButton = findViewById(R.id.profileLogoutButton);
        deleteButton = findViewById(R.id.profileDeleteButton);

        loadUserData();

        saveButton.setOnClickListener(v -> updateProfile());
        logoutButton.setOnClickListener(v -> logout());
        deleteButton.setOnClickListener(v -> deleteAccount());


        MaterialToolbar toolbar = findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    // Loads user's data from the database
    private void loadUserData() {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        nameInput.setText(user.getName());
                        emailInput.setText(user.getEmail());
                        phoneInput.setText(user.getPhone());
                        locationInput.setText(user.getLocation());
                        notificationsSwitch.setChecked(user.isNotificationsEnabled());
                    }
                });
    }

    // Updates user's changed data (based on input fields) to the database
    private void updateProfile() {
        db.collection("users").document(uid)
                .update(
                        "name", nameInput.getText().toString(),
                        "email", emailInput.getText().toString(),
                        "phone", phoneInput.getText().toString(),
                        "location", locationInput.getText().toString(),
                        "notificationsEnabled", notificationsSwitch.isChecked()
                );
        Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
    }

    // Logs out and navigates to the login activity
    private void logout() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().remove("dont_show_lottery_info").apply();

        auth.signOut();
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }

    // Deletes account on button press when called
    private void deleteAccount() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isOrganizer = prefs.getBoolean("isOrganizer", false);
        
        if (isOrganizer) {
            deleteOrganizerData(uid);
        } else {
            deleteUserData(uid);
        }
    }
    
    private void deleteUserData(String userId) {
        WaitlistModel waitlistModel = new WaitlistModel();
        
        // 1. Get all waitlist entries for user
        waitlistModel.getWaitlistEntriesByUser(userId)
            .continueWithTask(task -> {
                List<com.google.android.gms.tasks.Task<Void>> deletionTasks = new ArrayList<>();
                
                if (task.isSuccessful() && task.getResult() != null) {
                    for (WaitlistEntry entry : task.getResult()) {
                        // 2. Remove user from each waitlist
                        deletionTasks.add(waitlistModel.removeWaitlistEntry(entry.getEventId(), userId));
                    }
                }
                
                return Tasks.whenAll(deletionTasks);
            })
            .continueWithTask(task -> {
                // 3. Delete User Profile
                return db.collection("users").document(userId).delete();
            })
            .addOnSuccessListener(aVoid -> {
                // 4. Delete Auth Account
                if (auth.getCurrentUser() != null) {
                    auth.getCurrentUser().delete()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                finishAffinity(); 
                                Intent intent = new Intent(this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(this, "Failed to delete authentication record.", Toast.LENGTH_SHORT).show();
                            }
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e("ProfileActivity", "Error deleting user data", e);
                Toast.makeText(this, "Failed to delete user data.", Toast.LENGTH_SHORT).show();
            });
    }

    private void deleteOrganizerData(String organizerId) {
        EventModel eventModel = new EventModel();
        WaitlistModel waitlistModel = new WaitlistModel();
        
        // 1. Get all events by organizer
        eventModel.getOrganizerEvents(organizerId)
            .continueWithTask(task -> {
                List<com.google.android.gms.tasks.Task<Void>> cleanupTasks = new ArrayList<>();
                
                if (task.isSuccessful() && task.getResult() != null) {
                    for (Event event : task.getResult()) {
                        String eventId = event.getId();
                        
                        // 2. Get waitlist for each event
                        cleanupTasks.add(waitlistModel.getWaitlistEntriesByEvent(eventId).continueWithTask(wlTask -> {
                            List<com.google.android.gms.tasks.Task<Void>> wlDeleteTasks = new ArrayList<>();
                            
                            if (wlTask.isSuccessful() && wlTask.getResult() != null) {
                                // 3. Delete each waitlist entry
                                for (WaitlistEntry entry : wlTask.getResult()) {
                                    // We delete by document ID essentially via removeWaitlistEntry logic
                                    // Ideally we'd delete the document directly if we had ID, but removeWaitlistEntry works
                                    wlDeleteTasks.add(waitlistModel.removeWaitlistEntry(eventId, entry.getUserId()));
                                }
                            }
                            
                            // 4. Delete the event itself
                            wlDeleteTasks.add(eventModel.deleteEvent(eventId));
                            
                            return Tasks.whenAll(wlDeleteTasks);
                        }).continueWith(t -> null)); 
                    }
                }
                
                return Tasks.whenAll(cleanupTasks);
            })
            .continueWithTask(task -> {
                // 5. Delete User Profile
                return db.collection("users").document(organizerId).delete();
            })
            .addOnSuccessListener(aVoid -> {
                // 6. Delete Auth Account
                if (auth.getCurrentUser() != null) {
                    auth.getCurrentUser().delete()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                finishAffinity(); 
                                Intent intent = new Intent(this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(this, "Failed to delete authentication record.", Toast.LENGTH_SHORT).show();
                            }
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e("ProfileActivity", "Error deleting organizer data", e);
                Toast.makeText(this, "Failed to delete organizer data.", Toast.LENGTH_SHORT).show();
            });
    }
}
