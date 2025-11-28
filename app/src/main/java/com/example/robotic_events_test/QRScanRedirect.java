package com.example.robotic_events_test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Objects;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRScanRedirect extends AppCompatActivity {

    private ArrayList<Event> events = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan_redirect);

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.eventDetailToolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Log.d("EventDetail", "Back button clicked");
            finish();
        });

        Button ScanButton = findViewById(R.id.QRScanButton);
        ScanButton.setOnClickListener(v -> {
            IntentIntegrator intentIntegrator = new IntentIntegrator(this);
            intentIntegrator.setPrompt("Scan a QR Code");
            intentIntegrator.setOrientationLocked(false);
            intentIntegrator.initiateScan();
        });

        loadEventsFromFirestore();

        //SearchView searchBar = findViewById(R.id.search_bar_qr);
        //searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
        /*
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Query string id should be event id
                // Search for events in DB
                loadEventsFromFirestore();

                for (Event event : events) {
                    if (Objects.equals(event.getId(), query)) {
                        Intent intent = new Intent(QRScanRedirect.this, EventDetailActivity.class);
                        intent.putExtra("event", event);
                        startActivity(intent);
                    }
                }
                return false;
            }

            public boolean onQueryTextChange(String newText) {
                // Do nothing
                return false;
            }
        });
         */
    }

    private void loadEventsFromFirestore() {
        EventModel eventModel = new EventModel();
        eventModel.getAllEvents().addOnSuccessListener(eventList -> {
            events.clear();
            events.addAll(eventList);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load events.", Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "Fetch events error", e);
        });
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                // Cancelled or failed
                Toast.makeText(QRScanRedirect.this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // Has result
                String id = intentResult.getContents();

                if (events.isEmpty()) {
                    return;
                }

                for (Event event : events) {
                    // Redirect on success
                    if (Objects.equals(event.getId(), id)) {
                        Intent intent = new Intent(QRScanRedirect.this, EventDetailActivity.class);
                        intent.putExtra("event", event);
                        startActivity(intent);
                    }
                }
                Toast.makeText(QRScanRedirect.this, "Couldn't find an event", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}