package com.example.robotic_events_test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/** Does the admin access validation */
public class AdminAccessActivity extends AppCompatActivity {

    private TextInputEditText adminCodeInput;
    private Button submitAdminCodeButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_access);

        db = FirebaseFirestore.getInstance();
        adminCodeInput = findViewById(R.id.adminCodeInput);
        submitAdminCodeButton = findViewById(R.id.submitAdminCodeButton);

        MaterialToolbar toolbar = findViewById(R.id.adminAccessToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        submitAdminCodeButton.setOnClickListener(v -> {
            String enteredCode = adminCodeInput.getText().toString().trim();

            //prevent empty text
            if (enteredCode.isEmpty()) {
                Toast.makeText(this, "Please enter an admin code", Toast.LENGTH_SHORT).show();
                return;
            }
            validateAdminCode(enteredCode);
        });
    }

    //validates the entered admin code
    private void validateAdminCode(String code) {
        db.collection("admins")
                .whereEqualTo("code", code)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Incorrect Admin Code", Toast.LENGTH_SHORT).show();
                    } else {
                        // get first matching admin
                        DocumentSnapshot adminDocument = queryDocumentSnapshots.getDocuments().get(0);
                        // get name of admin
                        String adminName = adminDocument.getString("name");

                        // Save admin status
                        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("isAdmin", true);
                        editor.apply();

                        Toast.makeText(this, "Admin Access Granted!", Toast.LENGTH_SHORT).show();

                        proceedToAdminDashboard(adminName);

                    }
                })
                //failure
                .addOnFailureListener(e -> {
                    Log.e("AdminAccess", "Error validating admin code", e);
                    Toast.makeText(this, "Error checking code: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    //goes to the dashboard page
    private void proceedToAdminDashboard(String adminName) {
        Intent intent = new Intent(AdminAccessActivity.this, AdminDashboardActivity.class);
        //pass admin name to dashboard
        intent.putExtra("ADMIN_NAME", adminName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close this activity
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to LoginActivity
            Intent intent = new Intent(AdminAccessActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
