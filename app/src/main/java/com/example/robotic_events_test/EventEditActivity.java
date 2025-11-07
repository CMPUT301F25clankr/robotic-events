package com.example.robotic_events_test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class EventEditActivity extends AppCompatActivity {

    private EditText eventTitleEditor, eventCapacityEditor;
    private DatePicker eventDatePickerEditor;
    private TimePicker eventTimePickerEditor;
    private Button eventSaveChangesConfirm;
    private Button eventDeleteConfirm;
    private EventModel eventModel;
    private String eventId;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        eventModel = new EventModel();
        eventId = getIntent().getStringExtra("id");

        eventTitleEditor = findViewById(R.id.eventTitleEditor);
        eventCapacityEditor = findViewById(R.id.eventCapacityEditor);
        eventDatePickerEditor = findViewById(R.id.eventDatePickerEditor);
        eventTimePickerEditor = findViewById(R.id.eventTimePickerEditor);
        eventSaveChangesConfirm = findViewById(R.id.eventSaveChangesConfirm);
        eventDeleteConfirm = findViewById(R.id.eventDeleteConfirm);

        loadEventData();

        eventSaveChangesConfirm.setOnClickListener(v -> saveEventChanges());
        eventDeleteConfirm.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void loadEventData() {
        eventModel.getEvent(eventId).addOnSuccessListener(event -> {
            if (event != null) {
                currentEvent = event;
                populateFields();
            }
        });
    }

    private void populateFields() {
        eventTitleEditor.setText(currentEvent.getTitle());
        eventCapacityEditor.setText(String.valueOf(currentEvent.getTotalCapacity()));

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentEvent.getDateTime());
        eventDatePickerEditor.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        eventTimePickerEditor.setHour(cal.get(Calendar.HOUR_OF_DAY));
        eventTimePickerEditor.setMinute(cal.get(Calendar.MINUTE));
    }

    private void saveEventChanges() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(eventDatePickerEditor.getYear(), eventDatePickerEditor.getMonth(), eventDatePickerEditor.getDayOfMonth(),
                eventTimePickerEditor.getHour(), eventTimePickerEditor.getMinute());
        long dateTime = calendar.getTimeInMillis();

        currentEvent.setTitle(eventTitleEditor.getText().toString());
        currentEvent.setTotalCapacity(Integer.parseInt(eventCapacityEditor.getText().toString()));
        currentEvent.setDateTime(dateTime);

        eventModel.saveEvent(currentEvent);
        Toast.makeText(this, "Event Updated", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event permanently?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent())
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteEvent() {
        eventModel.deleteEvent(eventId).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Event Deleted", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }
}