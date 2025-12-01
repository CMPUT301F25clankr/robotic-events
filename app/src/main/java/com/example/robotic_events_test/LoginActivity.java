package com.example.robotic_events_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * VIEW: Login - allows individuals to login to their accounts on the app. Accounts are assigned
 * particular roles.
 */
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText emailInput, passwordInput;
    private Button loginButton, signupButton;
    private ImageButton adminButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);
        adminButton = findViewById(R.id.adminButton);

        // Already logged in? Check role and skip login
        if (auth.getCurrentUser() != null) {
            fetchUserRoleAndProceed();
        }

        loginButton.setOnClickListener(v -> loginUser());
        signupButton.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });

        adminButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminAccessActivity.class));
        });
    }

    // Logs the user in based on their input to the fields.
    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                    fetchUserRoleAndProceed(); // Fetch role after login
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // Gets user's role based on their account information; navigates them appropriately.
    private void fetchUserRoleAndProceed() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    Boolean isBannedObj = document.getBoolean("banned");
                    boolean isBanned = (isBannedObj != null && isBannedObj);

                    if (isBanned) {
                        startActivity(new Intent(LoginActivity.this, BannedActivity.class));
                        finish();
                    } else {
                        // For any regular user login, clear admin status
                        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("isAdmin", false); // CRITICAL FIX: Clear admin status

                        Boolean isOrganizerObj = document.getBoolean("organizer");
                        boolean isOrganizer = (isOrganizerObj != null && isOrganizerObj);
                        editor.putBoolean("isOrganizer", isOrganizer);

                        editor.apply(); // Apply all changes
                        goToMain();
                    }
                })
                .addOnFailureListener(e -> {
                    // If fetching fails, default to a non-admin, non-organizer user
                    SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    prefs.edit()
                        .putBoolean("isAdmin", false)
                        .putBoolean("isOrganizer", false)
                        .apply();

                    goToMain();
                });
    }

    // Navigates to MainActivity once login is completed successfully.
    private void goToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
