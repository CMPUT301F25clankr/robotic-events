// src/main/java/com/example/robotic_events_test/EventCreationActivity.java
package com.example.robotic_events_test;

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

public class EventCreationActivity extends AppCompatActivity {

    private EditText eventTitleSetter, eventCapacitySetter;
    private DatePicker eventDatePicker;
    private TimePicker eventTimePicker;
    private Button eventCreationConfirm;
    private EventModel eventModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);

        eventModel = new EventModel();

        Toolbar toolbar = findViewById(R.id.eventCreationToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create an Event");


        /*
        eventTimePicker
        eventDatePicker
        eventCapacitySetter
        eventNameSetter
         */
        eventTitleSetter = findViewById(R.id.eventTitleSetter);
        eventCapacitySetter = findViewById(R.id.eventCapacitySetter);
        eventDatePicker = findViewById(R.id.eventDatePicker);
        eventTimePicker = findViewById(R.id.eventTimePicker);
        // button to send form data to this activity
        eventCreationConfirm = findViewById(R.id.eventCreationConfirm);

        eventCreationConfirm.setOnClickListener(v -> createEvent());
    }

    private void createEvent() {
        // to get organizer ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to create an event.", Toast.LENGTH_SHORT).show();
            return;
        }
        String organizerId = currentUser.getUid();

        String eventTitle = eventTitleSetter.getText().toString().trim();
        String eventCapacityStr = eventCapacitySetter.getText().toString().trim();

        if (eventTitle.isEmpty() || eventCapacityStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        int eventCapacity = Integer.parseInt(eventCapacityStr);

        Calendar calendar = Calendar.getInstance();
        calendar.set(
                eventDatePicker.getYear(),
                eventDatePicker.getMonth(),
                eventDatePicker.getDayOfMonth(),
                eventTimePicker.getHour(),
                eventTimePicker.getMinute()
        );
        long dateTime = calendar.getTimeInMillis();

        eventModel.saveEvent(
                null, // autogen id
                eventTitle,
                "Default Description",
                dateTime,
                "Default Location",
                eventCapacity,
                0.0,
                organizerId,
                "General",
                null
        );

        Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show();
        Log.d("EventCreationActivity", "Event creation data sent to model.");

        finish();
    }

    // Back button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}