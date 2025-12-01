package com.example.robotic_events_test;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

/**
 VIEW: Lets admin decide to manage users, events, notifications, pictures; redirects based on their input
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private Button manageUsersButton;
    private Button manageEventsButton;
    private Button manageImagesButton;
    private TextView adminDashboardTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.adminDashboardToolbar);
        setSupportActionBar(toolbar);

        // Enable the back button (Up button)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        manageUsersButton = findViewById(R.id.manageUsersButton);
        manageEventsButton = findViewById(R.id.manageEventsButton);
        manageImagesButton = findViewById(R.id.manageImagesButton);
        adminDashboardTitle = findViewById(R.id.adminDashboardTitle);

        Intent intent = getIntent();
        String adminName = intent.getStringExtra("ADMIN_NAME");

        if (adminName != null && !adminName.isEmpty()) {
            adminDashboardTitle.setText("Welcome, " + adminName);
        } else {
            adminDashboardTitle.setText("Welcome, Admin");
        }

        //button navigation
        manageUsersButton.setOnClickListener(v -> {
            Toast.makeText(AdminDashboardActivity.this, "Navigating to Manage Users...", Toast.LENGTH_SHORT).show();
            Intent userIntent = new Intent(AdminDashboardActivity.this, AdminUserActivity.class);
            startActivity(userIntent);
        });

        manageEventsButton.setOnClickListener(v -> {
            Toast.makeText(this, "Navigating to Manage Events...", Toast.LENGTH_SHORT).show();
            Intent eventIntent = new Intent(AdminDashboardActivity.this, AdminEventActivity.class);
            startActivity(eventIntent);
        });

        manageImagesButton.setOnClickListener(v -> {
            Intent imageIntent = new Intent(AdminDashboardActivity.this, ManageImagesActivity.class);
            startActivity(imageIntent);
        });


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to AdminAccessActivity
            Intent intent = new Intent(AdminDashboardActivity.this, AdminAccessActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // close this activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
