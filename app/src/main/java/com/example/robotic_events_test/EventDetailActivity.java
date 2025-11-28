package com.example.robotic_events_test;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

public class EventDetailActivity extends AppCompatActivity {

    private Event event;
    private FirebaseAuth auth;
    private LotteryController lotteryController;
    private Button runLotteryButton;
    private WaitlistController waitlistController;
    private boolean isInWaitlist = false;
    private boolean isOrganizer = false;
    private boolean isAdmin = false;

    private Button joinLeaveWaitlistButton;
    private Button removeEventButton;
    private TextView detailWaitlist;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        lotteryController = new LotteryController();
        auth = FirebaseAuth.getInstance();
        waitlistController = new WaitlistController();

        event = (Event) getIntent().getSerializableExtra("event");
        if (event == null) {
            Toast.makeText(this, "Error: Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        isAdmin = prefs.getBoolean("isAdmin", false);

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

        if (isOrganizer) {
            fabEditEvent.setVisibility(View.VISIBLE);
            fabEditEvent.setOnClickListener(v -> {
                Toast.makeText(this, "Edit event (not implemented yet)", Toast.LENGTH_SHORT).show();
            });

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
                        Toast.makeText(this, "Showing QR code", Toast.LENGTH_SHORT).show();
                        qrView.setImageBitmap(qr);
                    }
                }
                dialog.show();
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
                .addOnSuccessListener(count -> {
                    detailWaitlist.setText("Waitlisted\n" + count);
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDetail", "Error loading waitlist count", e);
                    detailWaitlist.setText("Waitlisted\n0");
                });

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
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error leaving waitlist", Toast.LENGTH_SHORT).show();
                        Log.e("EventDetail", "Error leaving waitlist", e);
                    });
        } else {
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

    private void updateButtonText() {
        joinLeaveWaitlistButton.setText(isInWaitlist ? "Leave Waitlist" : "Join Waitlist");
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

    private Bitmap generateQRCode(String text) {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        try {
            return barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400);
        } catch (WriterException e) {
            Log.e("EventDetail", "Failed");
        }
        return null;
    }
}
