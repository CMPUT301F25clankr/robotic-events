package com.example.robotic_events_test;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * VIEW: Allows organizer to enable/disable geolocation requirement for their event
 * US 02.02.03: As an organizer I want to enable or disable the geolocation requirement for my event.
 */
public class EventGeolocationSettingsActivity extends AppCompatActivity {

    private SwitchCompat geolocationSwitch;
    private Button saveButton;
    private TextView descriptionText;
    private EventModel eventModel;
    private String eventId;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_geolocation_settings);

        // Get event from intent
        eventId = getIntent().getStringExtra("eventId");
        event = (Event) getIntent().getSerializableExtra("event");

        if (eventId == null || event == null) {
            Toast.makeText(this, "Error: Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        eventModel = new EventModel();

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.geolocationSettingsToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Geolocation Settings");
        }

        // Initialize UI elements
        geolocationSwitch = findViewById(R.id.geolocationSwitch);
        saveButton = findViewById(R.id.saveGeolocationButton);
        descriptionText = findViewById(R.id.geolocationDescriptionText);

        // Set initial switch state
        geolocationSwitch.setChecked(event.isGeolocationRequired());

        // Update description based on switch state
        updateDescription();

        // Listen for switch changes
        geolocationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateDescription());

        // Save button listener
        saveButton.setOnClickListener(v -> saveGeolocationSettings());
    }

    private void updateDescription() {
        if (geolocationSwitch.isChecked()) {
            descriptionText.setText("Geolocation ENABLED: Users must provide their location when joining the waitlist. You will be able to see where they are located on a map.");
        } else {
            descriptionText.setText("Geolocation DISABLED: Users will NOT be required to provide their location when joining the waitlist.");
        }
    }

    private void saveGeolocationSettings() {
        boolean isEnabled = geolocationSwitch.isChecked();
        event.setGeolocationRequired(isEnabled);

        eventModel.saveEvent(event);

        Toast.makeText(this,
                "Geolocation " + (isEnabled ? "enabled" : "disabled") + " for this event",
                Toast.LENGTH_SHORT).show();

        Log.d("GeolocationSettings", "Geolocation setting saved: " + isEnabled);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}