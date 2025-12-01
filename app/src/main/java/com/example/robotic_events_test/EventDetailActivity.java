package com.example.robotic_events_test;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Objects;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

/**
 * VIEW: Displays event details and handles user interactions
 * Uses WaitlistController for all waitlist operations (MVC pattern)
 */
public class EventDetailActivity extends AppCompatActivity {

    private Event event;
    private FirebaseAuth auth;
    // fields
    private LotteryController lotteryController;
    private Button runLotteryButton;

    private WaitlistController waitlistController;  // CONTROLLER
    private boolean isInWaitlist = false;
    private boolean isOrganizer = false;
    private boolean isAdmin = false;

    // UI Elements
    private Button joinLeaveWaitlistButton;
    private Button removeEventButton;
    private TextView detailWaitlist;
    private TextView registrationDeadlineText;
    private CountDownTimer deadlineTimer;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        lotteryController = new LotteryController();

        auth = FirebaseAuth.getInstance();
        waitlistController = new WaitlistController();  // Initialize controller

        // Get passed event
        event = (Event) getIntent().getSerializableExtra("event");
        if (event == null) {
            Toast.makeText(this, "Error: Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if current user is the organizer
        if (auth.getCurrentUser() != null) {
            isOrganizer = event.getOrganizerId().equals(auth.getCurrentUser().getUid());
        }

        //checks if user is admin
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        isAdmin = prefs.getBoolean("isAdmin", false);

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.eventDetailToolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Log.d("EventDetail", "Back button clicked");
            finish();
        });

        // Initialize views
        initializeViews();

        // Populate UI with event data
        populateEventDetails();

        // Load waitlist data
        loadWaitlistData();

        // Setup button listeners
        setupButtonListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deadlineTimer != null) {
            deadlineTimer.cancel();
        }
    }

    private void initializeViews() {
        detailWaitlist = findViewById(R.id.detailWaitlist);
        joinLeaveWaitlistButton = findViewById(R.id.joinLeaveWaitlistButton);
        registrationDeadlineText = findViewById(R.id.registrationDeadlineText);
        removeEventButton = findViewById(R.id.removeEventButton);

        FloatingActionButton fabEditEvent = findViewById(R.id.fab_edit_event);
        FloatingActionButton fabGenQr = findViewById(R.id.gen_qr_code);
        runLotteryButton = findViewById(R.id.runLotteryButton);
        Button viewLotteryResultsButton = findViewById(R.id.viewLotteryResultsButton);


        if (isAdmin) {
            joinLeaveWaitlistButton.setVisibility(View.GONE);
            removeEventButton.setVisibility(View.VISIBLE);
        } else {
            joinLeaveWaitlistButton.setVisibility(View.VISIBLE);
            removeEventButton.setVisibility(View.GONE);
        }

        // Show edit FAB and lottery buttons only if user is organizer
        if (isOrganizer) {
            fabEditEvent.setVisibility(View.VISIBLE);
            fabEditEvent.setOnClickListener(v -> {
                // Launch EventEditActivity with event ID
                Intent intent = new Intent(this, EventEditActivity.class);
                intent.putExtra("id", event.getId());
                startActivity(intent);
            });

            fabGenQr.setVisibility(View.VISIBLE);
            fabGenQr.setOnClickListener(v -> {
                // Generate QR Code stuff
                Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.gen_qr_code_dialog);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.parseColor("#80000000")));

                if (!event.getId().isEmpty() && event.getId() != null) {
                    Bitmap qr = generateQRCode(event.getId());
                    ImageView qrView = dialog.findViewById(R.id.qr_code_dialog_img);
                    if (qr != null) {
                        Toast.makeText(this, "Showing QR code", Toast.LENGTH_SHORT).show();
                        qrView.setImageBitmap(qr);
                    }
                }

                // Show dialog
                dialog.show();
            });
            //geolocation stuff
            Button viewMapButton = findViewById(R.id.viewMapButton);
            Button geolocationSettingsButton = findViewById(R.id.geolocationSettingsButton);

            if (isOrganizer) {
                viewMapButton.setVisibility(View.VISIBLE);
                viewMapButton.setOnClickListener(v -> {
                    Intent intent = new Intent(this, EventMapActivity.class);
                    intent.putExtra("eventId", event.getId());
                    intent.putExtra("event", event);
                    startActivity(intent);
                });

                geolocationSettingsButton.setVisibility(View.VISIBLE);
                geolocationSettingsButton.setOnClickListener(v -> {
                    Intent intent = new Intent(this, EventGeolocationSettingsActivity.class);
                    intent.putExtra("eventId", event.getId());
                    intent.putExtra("event", event);
                    startActivity(intent);
                });
            }
            
            // Hide Run Lottery button if event is closed
            if ("closed".equals(event.getStatus())) {
                runLotteryButton.setVisibility(View.GONE);
            } else {
                runLotteryButton.setVisibility(View.VISIBLE);
            }
            // runLotteryButton click listener is in setupButtonListeners()

            viewLotteryResultsButton.setVisibility(View.VISIBLE);
            viewLotteryResultsButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, LotteryResultActivity.class);
                intent.putExtra("eventId", event.getId());
                startActivity(intent);
            });
        } else {
            fabEditEvent.setVisibility(View.GONE);
            runLotteryButton.setVisibility(View.GONE);
            viewLotteryResultsButton.setVisibility(View.GONE);
        }
        
        // Handle Join Waitlist Button state based on event status and deadline
        checkDeadlineAndStatus();
    }

    private void checkDeadlineAndStatus() {
        boolean closed = "closed".equals(event.getStatus());
        boolean deadlinePassed = event.getRegistrationDeadline() > 0 && System.currentTimeMillis() > event.getRegistrationDeadline();

        if (closed) {
            joinLeaveWaitlistButton.setEnabled(false);
            joinLeaveWaitlistButton.setText("Event Closed");
            joinLeaveWaitlistButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.GRAY));
        } else if (deadlinePassed) {
            joinLeaveWaitlistButton.setEnabled(false);
            joinLeaveWaitlistButton.setText("Registration Closed");
            joinLeaveWaitlistButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.GRAY));
        }
    }


    private void populateEventDetails() {
        TextView detailTitle = findViewById(R.id.detailTitle);
        TextView detailWhen = findViewById(R.id.detailWhen);
        TextView detailWhere = findViewById(R.id.detailWhere);
        TextView detailCapacity = findViewById(R.id.detailCapacity);
        TextView detailPrice = findViewById(R.id.detailPrice);
        TextView detailCategory = findViewById(R.id.detailCategory);
        TextView detailOrganizer = findViewById(R.id.detailOrganizer);
        TextView detailDescription = findViewById(R.id.detailDescription);

        detailTitle.setText(event.getTitle());
        detailWhen.setText(DateFormat.format("MMM dd, yyyy h:mm a", event.getDateTime()));
        detailWhere.setText(event.getLocation());
        detailCapacity.setText("Capacity\n" + event.getTotalCapacity());
        detailPrice.setText(String.format("$%.2f", event.getPrice()));
        detailCategory.setText(event.getCategory());
        detailOrganizer.setText("Organizer ID: " + event.getOrganizerId());
        detailDescription.setText(event.getDescription() != null ? event.getDescription() : "No description available");
        
        // Registration Deadline Logic
        if (event.getRegistrationDeadline() > 0) {
            long now = System.currentTimeMillis();
            if (now > event.getRegistrationDeadline()) {
                registrationDeadlineText.setText("Registration Closed");
                registrationDeadlineText.setTextColor(Color.RED);
            } else {
                registrationDeadlineText.setVisibility(View.VISIBLE);
                startDeadlineCountdown(event.getRegistrationDeadline());
            }
        } else {
            registrationDeadlineText.setVisibility(View.GONE);
        }
    }
    
    private void startDeadlineCountdown(long deadlineMillis) {
        long millisInFuture = deadlineMillis - System.currentTimeMillis();
        if (millisInFuture <= 0) return;

        deadlineTimer = new CountDownTimer(millisInFuture, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;

                String timeString;
                if (days > 0) {
                    timeString = String.format("%dd %02dh left", days, hours % 24);
                } else {
                    timeString = String.format("%02d:%02d:%02d left", hours, minutes % 60, seconds % 60);
                }
                registrationDeadlineText.setText("Deadline: " + DateFormat.format("MMM dd, h:mm a", deadlineMillis) + " (" + timeString + ")");
            }

            public void onFinish() {
                registrationDeadlineText.setText("Registration Closed");
                registrationDeadlineText.setTextColor(Color.RED);
                checkDeadlineAndStatus(); // Update button
            }
        }.start();
    }

    private void loadWaitlistData() {
        // Load waitlist count through controller
        waitlistController.getWaitlistCount(event.getId())
                .addOnSuccessListener(count -> {
                    detailWaitlist.setText("Waitlisted\n" + count);
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDetail", "Error loading waitlist count", e);
                    detailWaitlist.setText("Waitlisted\n0");
                });

        // Check if current user is in waitlist
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            waitlistController.isUserInWaitlist(event.getId(), userId)
                    .addOnSuccessListener(inWaitlist -> {
                        isInWaitlist = inWaitlist;
                        updateButtonText();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("EventDetail", "Error checking waitlist status", e);
                        updateButtonText();
                    });
        } else {
            updateButtonText();
        }
    }

    private void setupButtonListeners() {
        joinLeaveWaitlistButton.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "Please log in to join waitlist", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if ("closed".equals(event.getStatus())) {
                Toast.makeText(this, "Event is closed.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (event.getRegistrationDeadline() > 0 && System.currentTimeMillis() > event.getRegistrationDeadline()) {
                Toast.makeText(this, "Registration deadline has passed.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            handleWaitlistToggle();
        });

        removeEventButton.setOnClickListener(v -> removeEvent());
        runLotteryButton.setOnClickListener(v -> {
            if (!isOrganizer) {
                Toast.makeText(this, "Only the organizer can run the lottery", Toast.LENGTH_SHORT).show();
                return;
            }
            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentUserId = auth.getCurrentUser().getUid();

            lotteryController.runLottery(event.getId(), currentUserId)
                    .addOnSuccessListener(success -> {
                        if (success) {
                            Toast.makeText(this, "Lottery run successfully", Toast.LENGTH_SHORT).show();
                            
                            // Update local event status and UI
                            event.setStatus("closed");
                            initializeViews(); // Refresh button states
                            
                        } else {
                            Toast.makeText(this, "Lottery could not be run", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error running lottery", Toast.LENGTH_SHORT).show();
                        Log.e("EventDetail", "Error running lottery", e);
                    });
        });

    }

    //Admin deletes events and associated waitlists
    private void removeEvent() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String eventId = event.getId();

        db.collection("waitlists").whereEqualTo("eventId", eventId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }

                    db.collection("events").document(eventId).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Event removed successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error removing event", Toast.LENGTH_SHORT).show();
                                Log.e("EventDetailActivity", "Error removing event", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error cleaning up waitlist", Toast.LENGTH_SHORT).show();
                    Log.e("EventDetailActivity", "Error cleaning up waitlist", e);
                });
    }

    private void captureLocationAndJoinWaitlist(String userId) {
        LocationHelper locationHelper = new LocationHelper(this);

        if (!locationHelper.hasLocationPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{LocationHelper.PERMISSION},
                    LocationHelper.PERMISSION_REQUEST_CODE);
            return;
        }

        locationHelper.getCurrentLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        LocationHelper.LocationData locData = locationHelper.extractLocationData(location, "User");

                        waitlistController.joinWaitlistWithLocation(event.getId(), userId,
                                        locData.latitude, locData.longitude, locData.locationName)
                                .addOnSuccessListener(success -> {
                                    if (success) {
                                        isInWaitlist = true;
                                        updateButtonText();
                                        refreshWaitlistCount();
                                        Toast.makeText(this, "Added to waitlist!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Failed to join waitlist", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error joining waitlist", Toast.LENGTH_SHORT).show();
                                    Log.e("EventDetail", "Error joining waitlist", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show();
                    Log.e("EventDetail", "Error getting location", e);
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationHelper.PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handleWaitlistToggle();
            } else {
                Toast.makeText(this, "Location permission required to join", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void handleWaitlistToggle() {
        String userId = auth.getCurrentUser().getUid();

        if (isInWaitlist) {
            // Leave waitlist - keep existing code
            waitlistController.leaveWaitlist(event.getId(), userId)
                    .addOnSuccessListener(success -> {
                        if (success) {
                            isInWaitlist = false;
                            updateButtonText();
                            refreshWaitlistCount();
                            Toast.makeText(this, "Removed from waitlist", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to leave waitlist", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error leaving waitlist", Toast.LENGTH_SHORT).show();
                        Log.e("EventDetail", "Error leaving waitlist", e);
                    });
        } else {
            // Join waitlist - check if geolocation required
            if (event.isGeolocationRequired()) {
                captureLocationAndJoinWaitlist(userId);
            } else {
                // No geolocation required, use existing join method
                waitlistController.joinWaitlist(event.getId(), userId)
                        .addOnSuccessListener(success -> {
                            if (success) {
                                isInWaitlist = true;
                                updateButtonText();
                                refreshWaitlistCount();
                                Toast.makeText(this, "Added to waitlist!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Failed to join waitlist", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error joining waitlist", Toast.LENGTH_SHORT).show();
                            Log.e("EventDetail", "Error joining waitlist", e);
                        });
            }
        }
    }

    private void updateButtonText() {
        if ("closed".equals(event.getStatus())) {
            // Double check status in updateButtonText as well
            joinLeaveWaitlistButton.setText("Event Closed");
            joinLeaveWaitlistButton.setEnabled(false);
            joinLeaveWaitlistButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.GRAY));
            return;
        }
        
        if (event.getRegistrationDeadline() > 0 && System.currentTimeMillis() > event.getRegistrationDeadline()) {
            joinLeaveWaitlistButton.setText("Registration Closed");
            joinLeaveWaitlistButton.setEnabled(false);
            joinLeaveWaitlistButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.GRAY));
            return;
        }

        if (isInWaitlist) {
            joinLeaveWaitlistButton.setText("Leave Waitlist");
            joinLeaveWaitlistButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(android.R.color.holo_red_dark)));
            joinLeaveWaitlistButton.setEnabled(true);
        } else {
            joinLeaveWaitlistButton.setText("Join Waitlist");
            joinLeaveWaitlistButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#008000")));
            joinLeaveWaitlistButton.setEnabled(true);
        }
    }

    private void refreshWaitlistCount() {
        waitlistController.getWaitlistCount(event.getId())
                .addOnSuccessListener(count -> {
                    detailWaitlist.setText("Waitlisted\n" + count);
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDetail", "Error refreshing waitlist count", e);
                });
    }

    private Bitmap generateQRCode(String text)
    {
        BarcodeEncoder barcodeEncoder
                = new BarcodeEncoder();
        try {

            // This method returns a Bitmap image of the
            // encoded text with a height and width of 400
            // pixels.
            return barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400);
        }
        catch (WriterException e) {
            Log.e("EventDetail", "Failed");
        }
        return null;
    }
}
