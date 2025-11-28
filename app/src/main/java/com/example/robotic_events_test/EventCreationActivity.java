package com.example.robotic_events_test;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Objects;

public class EventCreationActivity extends AppCompatActivity {

    private EditText eventTitleSetter, eventCapacitySetter, eventLocationSetter;
    private DatePicker eventDatePicker;
    private TimePicker eventTimePicker;
    private EventModel eventModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);

        eventModel = new EventModel();

        Toolbar toolbar = findViewById(R.id.eventCreationToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create an Event");

        eventTitleSetter = findViewById(R.id.eventTitleSetter);
        eventLocationSetter = findViewById(R.id.eventLocationSetter);
        eventCapacitySetter = findViewById(R.id.eventCapacitySetter);
        eventDatePicker = findViewById(R.id.eventDatePicker);
        eventTimePicker = findViewById(R.id.eventTimePicker);
        Button eventCreationConfirm = findViewById(R.id.eventCreationConfirm);

        eventCreationConfirm.setOnClickListener(v -> createEvent());
    }

    private void createEvent() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to create an event.", Toast.LENGTH_SHORT).show();
            return;
        }
        String organizerId = currentUser.getUid();

        String eventTitle = eventTitleSetter.getText().toString().trim();
        String eventLocation = eventLocationSetter.getText().toString().trim();
        String eventCapacityStr = eventCapacitySetter.getText().toString().trim();

        if (eventTitle.isEmpty() || eventCapacityStr.isEmpty() || eventLocation.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        int eventCapacity = Integer.parseInt(eventCapacityStr);

        NominatimHelper.getCoordinatesFromAddress(eventLocation, new NominatimHelper.NominatimCallback() {
            @Override
            public void onCoordinatesResolved(Location location) {
                saveEvent(eventTitle, eventLocation, eventCapacity, organizerId, location.getLatitude(), location.getLongitude());
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(EventCreationActivity.this, "Could not find coordinates for the event location.", Toast.LENGTH_SHORT).show();
                saveEvent(eventTitle, eventLocation, eventCapacity, organizerId, 0.0, 0.0);
            }
        });
    }

    private void saveEvent(String eventTitle, String eventLocation, int eventCapacity, String organizerId, double latitude, double longitude) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(
                eventDatePicker.getYear(),
                eventDatePicker.getMonth(),
                eventDatePicker.getDayOfMonth(),
                eventTimePicker.getHour(),
                eventTimePicker.getMinute()
        );
        long dateTime = calendar.getTimeInMillis();

        eventModel.saveEvent(new Event(
                eventTitle,
                eventLocation,
                dateTime,
                eventCapacity,
                0.0,
                "Default Description",
                "General",
                organizerId,
                null,
                latitude,
                longitude
        ));

        Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show();
        Log.d("EventCreationActivity", "Event creation data sent to model.");

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
