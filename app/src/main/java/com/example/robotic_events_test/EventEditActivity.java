package com.example.robotic_events_test;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.Calendar;

public class EventEditActivity extends AppCompatActivity {

    private static final int PICK_ICON_IMAGE_REQUEST = 1;
    private static final int PICK_BANNER_IMAGE_REQUEST = 2;

    private EditText eventTitleEditor, eventCapacityEditor, eventIconUrlEditor, eventBannerUrlEditor;
    private DatePicker eventDatePickerEditor;
    private TimePicker eventTimePickerEditor;
    private Button eventSaveChangesConfirm, eventDeleteConfirm, uploadIconButton, uploadBannerButton;
    private ProgressBar progressBar;

    private EventModel eventModel;
    private StorageController storageController;
    private String eventId;
    private Event currentEvent;

    private Uri iconImageUri = null;
    private Uri bannerImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        eventModel = new EventModel();
        storageController = new StorageController();
        eventId = getIntent().getStringExtra("id");

        // Initialize all views
        initializeViews();

        loadEventData();

        uploadIconButton.setOnClickListener(v -> openFileChooser(PICK_ICON_IMAGE_REQUEST));
        uploadBannerButton.setOnClickListener(v -> openFileChooser(PICK_BANNER_IMAGE_REQUEST));
        eventSaveChangesConfirm.setOnClickListener(v -> saveEventChanges());
        eventDeleteConfirm.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void initializeViews() {
        eventTitleEditor = findViewById(R.id.eventTitleEditor);
        eventCapacityEditor = findViewById(R.id.eventCapacityEditor);
        eventDatePickerEditor = findViewById(R.id.eventDatePickerEditor);
        eventTimePickerEditor = findViewById(R.id.eventTimePickerEditor);
        eventSaveChangesConfirm = findViewById(R.id.eventSaveChangesConfirm);
        eventDeleteConfirm = findViewById(R.id.eventDeleteConfirm);
        eventIconUrlEditor = findViewById(R.id.eventIconUrlEditor);
        eventBannerUrlEditor = findViewById(R.id.eventBannerUrlEditor);
        uploadIconButton = findViewById(R.id.uploadIconButton);
        uploadBannerButton = findViewById(R.id.uploadBannerButton);
        progressBar = findViewById(R.id.editEventProgressBar);
    }

    private void openFileChooser(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == PICK_ICON_IMAGE_REQUEST) {
                iconImageUri = data.getData();
                eventIconUrlEditor.setText("New icon selected for upload");
            } else if (requestCode == PICK_BANNER_IMAGE_REQUEST) {
                bannerImageUri = data.getData();
                eventBannerUrlEditor.setText("New banner selected for upload");
            }
        }
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
        eventIconUrlEditor.setText(currentEvent.getImageUrl());
        eventBannerUrlEditor.setText(currentEvent.getBannerUrl());

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentEvent.getDateTime());
        eventDatePickerEditor.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        eventTimePickerEditor.setHour(cal.get(Calendar.HOUR_OF_DAY));
        eventTimePickerEditor.setMinute(cal.get(Calendar.MINUTE));
    }

    private void saveEventChanges() {
        setLoading(true);

        Task<Uri> iconUploadTask = (iconImageUri != null)
                ? storageController.uploadImage(iconImageUri, "event_images")
                : Tasks.forResult(null);

        iconUploadTask.continueWithTask(task -> {
            String iconUrl = eventIconUrlEditor.getText().toString();
            if (task.isSuccessful() && task.getResult() != null) {
                iconUrl = task.getResult().toString();
            }
            currentEvent.setImageUrl(iconUrl);

            Task<Uri> bannerUploadTask = (bannerImageUri != null)
                    ? storageController.uploadImage(bannerImageUri, "event_images")
                    : Tasks.forResult(null);

            return bannerUploadTask;
        }).continueWithTask(task -> {
            String bannerUrl = eventBannerUrlEditor.getText().toString();
            if (task.isSuccessful() && task.getResult() != null) {
                bannerUrl = task.getResult().toString();
            }
            currentEvent.setBannerUrl(bannerUrl);

            Calendar calendar = Calendar.getInstance();
            calendar.set(eventDatePickerEditor.getYear(), eventDatePickerEditor.getMonth(), eventDatePickerEditor.getDayOfMonth(),
                    eventTimePickerEditor.getHour(), eventTimePickerEditor.getMinute());
            currentEvent.setTitle(eventTitleEditor.getText().toString());
            currentEvent.setTotalCapacity(Integer.parseInt(eventCapacityEditor.getText().toString()));
            currentEvent.setDateTime(calendar.getTimeInMillis());

            eventModel.saveEvent(currentEvent);
            return Tasks.forResult(null); // Return a task to continue the chain

        }).addOnSuccessListener(aVoid -> {
            setLoading(false);
            Toast.makeText(this, "Event Updated Successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            setLoading(false);
            Toast.makeText(this, "Failed to update event: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            eventSaveChangesConfirm.setEnabled(false);
            eventDeleteConfirm.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            eventSaveChangesConfirm.setEnabled(true);
            eventDeleteConfirm.setEnabled(true);
        }
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