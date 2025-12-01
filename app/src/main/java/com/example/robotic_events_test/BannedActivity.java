package com.example.robotic_events_test;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
/** ban display*/
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
