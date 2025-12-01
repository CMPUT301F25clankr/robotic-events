package com.example.robotic_events_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
        db.collection("users").document(uid).delete();
        auth.getCurrentUser().delete();
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }
}
