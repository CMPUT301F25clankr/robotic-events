package com.example.robotic_events_test;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * VIEW: Ban activity - this is the display users will see if they're BANNED from the app.
 * Banned accounts are not deleted from the DB to ensure associated info cannot ever be used again.
 */
public class BannedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banned);

        MaterialToolbar toolbar = findViewById(R.id.bannedToolbar);

        //back navigation
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}
