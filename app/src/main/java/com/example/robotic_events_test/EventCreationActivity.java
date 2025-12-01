// src/main/java/com/example/robotic_events_test/EventCreationActivity.java
package com.example.robotic_events_test;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Objects;

/**
* VIEW: Allows users of Organizer role to create new events. Created events are added to the DB
 * and are viewable to all users.
 * Associated with Organizer user stories.
 */
public class EventCreationActivity extends AppCompatActivity {

    private static final int PICK_ICON_IMAGE_REQUEST = 1;
    private static final int PICK_BANNER_IMAGE_REQUEST = 2;

    private EditText eventTitleSetter, eventCapacitySetter, eventIconUrlSetter, eventBannerUrlSetter;
    private DatePicker eventDatePicker;
    private TimePicker eventTimePicker;
    private Button eventCreationConfirm, uploadIconButton, uploadBannerButton;
    private ProgressBar progressBar;

    private EventModel eventModel;
    private StorageController storageController;

    private Uri iconImageUri = null;
    private Uri bannerImageUri = null;

    private String chosenCategory = "General";

    @Override
    // Method to initialize the page for user display.
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_creation);

        eventModel = new EventModel();
        storageController = new StorageController();

        Toolbar toolbar = findViewById(R.id.eventCreationToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create an Event");

        initializeViews();

        uploadIconButton.setOnClickListener(v -> openFileChooser(PICK_ICON_IMAGE_REQUEST));
        uploadBannerButton.setOnClickListener(v -> openFileChooser(PICK_BANNER_IMAGE_REQUEST));
        eventCreationConfirm.setOnClickListener(v -> createEvent());
    }

    // Initializes references for views; calls another method to create category selection radio buttons.
    private void initializeViews() {
        eventTitleSetter = findViewById(R.id.eventTitleSetter);
        eventCapacitySetter = findViewById(R.id.eventCapacitySetter);
        eventDatePicker = findViewById(R.id.eventDatePicker);
        eventTimePicker = findViewById(R.id.eventTimePicker);
        eventIconUrlSetter = findViewById(R.id.eventIconUrlSetter);
        eventBannerUrlSetter = findViewById(R.id.eventBannerUrlSetter);
        uploadIconButton = findViewById(R.id.uploadIconButton);
        uploadBannerButton = findViewById(R.id.uploadBannerButton);
        eventCreationConfirm = findViewById(R.id.eventCreationConfirm);
        progressBar = findViewById(R.id.createEventProgressBar);

        RadioGroup radioGroup = findViewById(R.id.category_radio_group);
        createRadioButtons(radioGroup);
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
                eventIconUrlSetter.setText("New icon selected for upload");
                eventIconUrlSetter.setEnabled(false); // Prevent editing after selection
            } else if (requestCode == PICK_BANNER_IMAGE_REQUEST) {
                bannerImageUri = data.getData();
                eventBannerUrlSetter.setText("New banner selected for upload");
                eventBannerUrlSetter.setEnabled(false); // Prevent editing after selection
            }
        }
    }

    // Method to create an event object and accordingly post its information to the DB.
    private void createEvent() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to create an event.", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventTitle = eventTitleSetter.getText().toString().trim();
        String eventCapacityStr = eventCapacitySetter.getText().toString().trim();

        if (eventTitle.isEmpty() || eventCapacityStr.isEmpty()) {
            Toast.makeText(this, "Please fill in Event Name and Capacity.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Task 1: Determine the final Icon URL as a String
        Task<String> iconUrlTask;
        if (iconImageUri != null) {
            // If a new image was uploaded, get its download URL
            iconUrlTask = storageController.uploadImage(iconImageUri, "event_images")
                    .onSuccessTask(uri -> Tasks.forResult(uri.toString()));
        } else {
            // Otherwise, use the text from the EditText field
            iconUrlTask = Tasks.forResult(eventIconUrlSetter.getText().toString().trim());
        }

        // Task 2: Determine the final Banner URL as a String
        Task<String> bannerUrlTask;
        if (bannerImageUri != null) {
            // If a new image was uploaded, get its download URL
            bannerUrlTask = storageController.uploadImage(bannerImageUri, "event_images")
                    .onSuccessTask(uri -> Tasks.forResult(uri.toString()));
        } else {
            // Otherwise, use the text from the EditText field
            bannerUrlTask = Tasks.forResult(eventBannerUrlSetter.getText().toString().trim());
        }

        // Combine both tasks. This will only run after both URLs are ready.
        Tasks.whenAllSuccess(iconUrlTask, bannerUrlTask).addOnSuccessListener(results -> {
            // 'results' is a List containing the outcomes of the tasks in order.
            String iconUrlResult = (String) results.get(0);
            String bannerUrlResult = (String) results.get(1);

            // Apply defaults if the URLs are empty
            String finalIconUrl = TextUtils.isEmpty(iconUrlResult) ? Constants.DEFAULT_EVENT_ICON_URL : iconUrlResult;
            String finalBannerUrl = TextUtils.isEmpty(bannerUrlResult) ? Constants.DEFAULT_EVENT_BANNER_URL : bannerUrlResult;

            Calendar calendar = Calendar.getInstance();
            calendar.set(
                    eventDatePicker.getYear(),
                    eventDatePicker.getMonth(),
                    eventDatePicker.getDayOfMonth(),
                    eventTimePicker.getHour(),
                    eventTimePicker.getMinute()
            );
            long dateTime = calendar.getTimeInMillis();
            int eventCapacity = Integer.parseInt(eventCapacityStr);
            String organizerId = currentUser.getUid();
            //String category = eventCategory;

            Event newEvent = new Event(
                    eventTitle,
                    "Default Location",
                    dateTime,
                    eventCapacity,
                    0.0,
                    "Default Description",
                    chosenCategory,
                    organizerId,
                    finalIconUrl,
                    finalBannerUrl
            );

            eventModel.saveEvent(newEvent);
            setLoading(false);
            Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show();
            finish();

        }).addOnFailureListener(e -> {
            setLoading(false);
            Toast.makeText(this, "Failed to create event: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            eventCreationConfirm.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            eventCreationConfirm.setEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Creates radio button category selection for an event. Creates a button per category.
    private void createRadioButtons(RadioGroup radioGroup) {
        String[] categories = {"Sports", "Art", "Food", "Games", "Community"};

        for (String cat : categories) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(cat);
            radioButton.setTextSize(16f);
            radioButton.setPadding(10,10,10,10);
            radioButton.setId(View.generateViewId());
            radioGroup.addView(radioButton);
        }

        radioGroup.setOnCheckedChangeListener(((group, checkedId) -> {
            RadioButton selectedButton = group.findViewById(checkedId);
            if (selectedButton != null) {
                chosenCategory = selectedButton.getText().toString();
            }
        }));
    }
}