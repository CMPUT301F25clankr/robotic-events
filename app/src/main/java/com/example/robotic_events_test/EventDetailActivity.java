package com.example.robotic_events_test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Objects;

public class EventDetailActivity extends AppCompatActivity {

    private Event event;
    private FirebaseAuth auth;
    private LotteryController lotteryController;
    private Button runLotteryButton;

    private WaitlistController waitlistController;
    private boolean isInWaitlist = false;
    private boolean isOrganizer = false;

    private Button joinLeaveWaitlistButton;
    private TextView detailWaitlist;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        lotteryController = new LotteryController();
        auth = FirebaseAuth.getInstance();
        waitlistController = new WaitlistController();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        event = (Event) getIntent().getSerializableExtra("event");
        if (event == null) {
            Toast.makeText(this, "Error: Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (auth.getCurrentUser() != null) {
            isOrganizer = event.getOrganizerId().equals(auth.getCurrentUser().getUid());
        }

        MaterialToolbar toolbar = findViewById(R.id.eventDetailToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        initializeViews();
        populateEventDetails();
        loadWaitlistData();
        setupButtonListeners();
    }

    private void initializeViews() {
        detailWaitlist = findViewById(R.id.detailWaitlist);
        joinLeaveWaitlistButton = findViewById(R.id.joinLeaveWaitlistButton);
        FloatingActionButton fabEditEvent = findViewById(R.id.fab_edit_event);
        FloatingActionButton fabGenQr = findViewById(R.id.gen_qr_code);
        runLotteryButton = findViewById(R.id.runLotteryButton);
        Button viewLotteryResultsButton = findViewById(R.id.viewLotteryResultsButton);

        if (isOrganizer) {
            fabEditEvent.setVisibility(View.VISIBLE);
            fabEditEvent.setOnClickListener(v -> Toast.makeText(this, "Edit event (not implemented yet)", Toast.LENGTH_SHORT).show());

            fabGenQr.setVisibility(View.VISIBLE);
            fabGenQr.setOnClickListener(v -> {
                Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.gen_qr_code_dialog);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.parseColor("#80000000")));

                if (!event.getId().isEmpty()) {
                    Bitmap qr = generateQRCode(event.getId());
                    ImageView qrView = dialog.findViewById(R.id.qr_code_dialog_img);
                    if (qr != null) {
                        qrView.setImageBitmap(qr);
                        dialog.show();
                    }
                }
            });

            runLotteryButton.setVisibility(View.VISIBLE);
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
    }

    private void loadWaitlistData() {
        waitlistController.getWaitlistCount(event.getId())
                .addOnSuccessListener(count -> detailWaitlist.setText("Waitlisted\n" + count))
                .addOnFailureListener(e -> detailWaitlist.setText("Waitlisted\n0"));

        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            waitlistController.isUserInWaitlist(event.getId(), userId)
                    .addOnSuccessListener(inWaitlist -> {
                        isInWaitlist = inWaitlist;
                        updateButtonText();
                    })
                    .addOnFailureListener(e -> updateButtonText());
        }
    }

    private void setupButtonListeners() {
        joinLeaveWaitlistButton.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "Please log in to join waitlist", Toast.LENGTH_SHORT).show();
                return;
            }
            handleWaitlistToggle();
        });
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
                        } else {
                            Toast.makeText(this, "Lottery could not be run", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error running lottery", Toast.LENGTH_SHORT).show());
        });
    }

    private void handleWaitlistToggle() {
        String userId = auth.getCurrentUser().getUid();

        if (isInWaitlist) {
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
                    .addOnFailureListener(e -> Toast.makeText(this, "Error leaving waitlist", Toast.LENGTH_SHORT).show());
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                joinWaitlistWithLocation();
            }
        }
    }

    private void joinWaitlistWithLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            double lat = 0.0;
            double lon = 0.0;
            if (location != null) {
                lat = location.getLatitude();
                lon = location.getLongitude();
            }
            String userId = auth.getCurrentUser().getUid();
            waitlistController.joinWaitlist(event.getId(), userId, lat, lon)
                .addOnSuccessListener(success -> {
                    if (success) {
                        isInWaitlist = true;
                        updateButtonText();
                        refreshWaitlistCount();
                        Toast.makeText(EventDetailActivity.this, "Added to waitlist!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EventDetailActivity.this, "Failed to join waitlist", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(EventDetailActivity.this, "Error joining waitlist", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                joinWaitlistWithLocation();
            } else {
                Toast.makeText(this, "Location permission is required for geolocation verification.", Toast.LENGTH_SHORT).show();
                // Still join the waitlist, but without location data
                joinWaitlistWithLocation();
            }
        }
    }

    private void updateButtonText() {
        if (isInWaitlist) {
            joinLeaveWaitlistButton.setText("Leave Waitlist");
            joinLeaveWaitlistButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(android.R.color.holo_red_dark)));
        } else {
            joinLeaveWaitlistButton.setText("Join Waitlist");
            joinLeaveWaitlistButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#008000")));
        }
    }

    private void refreshWaitlistCount() {
        waitlistController.getWaitlistCount(event.getId())
                .addOnSuccessListener(count -> detailWaitlist.setText("Waitlisted\n" + count))
                .addOnFailureListener(e -> Log.e("EventDetail", "Error refreshing waitlist count", e));
    }

    private Bitmap generateQRCode(String text) {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            return barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400);
        } catch (WriterException e) {
            Log.e("EventDetail", "Failed to generate QR code", e);
        }
        return null;
    }
<<<<<<< Updated upstream
}
=======
}
>>>>>>> Stashed changes
