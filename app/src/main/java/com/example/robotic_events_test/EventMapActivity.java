package com.example.robotic_events_test;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.CircleOptions;
import java.util.List;

/**
 * VIEW: Displays a map showing where waitlist entrants joined from
 * US 02.02.02: As an organizer I want to see on a map where entrants joined my event waiting list from.
 */
public class EventMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private WaitlistController waitlistController;
    private String eventId;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_map);

        // Get event ID from intent
        eventId = getIntent().getStringExtra("eventId");
        event = (Event) getIntent().getSerializableExtra("event");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        waitlistController = new WaitlistController();

        // Set toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Waitlist Map");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get map fragment and request callback
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set default zoom to show wider area
        if (event != null && event.getEventLatitude() != 0 && event.getEventLongitude() != 0) {
            LatLng eventLocation = new LatLng(event.getEventLatitude(), event.getEventLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 12));

            // Add circle around event to show event location
            CircleOptions circleOptions = new CircleOptions()
                    .center(eventLocation)
                    .radius(500) // 500 meters
                    .fillColor(0x220099FF)
                    .strokeColor(0xFF0099FF)
                    .strokeWidth(2);
            mMap.addCircle(circleOptions);
        }

        // Load and display waitlist entries
        loadWaitlistOnMap();
    }

    private void loadWaitlistOnMap() {
        waitlistController.getEventWaitlist(eventId)
                .addOnSuccessListener(entries -> {
                    if (entries.isEmpty()) {
                        Toast.makeText(this, "No waitlist entries with location data", Toast.LENGTH_SHORT).show();
                    } else {
                        displayEntriesOnMap(entries);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventMapActivity", "Error loading waitlist", e);
                    Toast.makeText(this, "Error loading waitlist data", Toast.LENGTH_SHORT).show();
                });
    }

    private void displayEntriesOnMap(List<WaitlistEntry> entries) {
        for (WaitlistEntry entry : entries) {
            // Only show markers for entries with valid coordinates
            if (entry.getLatitude() != 0 && entry.getLongitude() != 0) {
                LatLng markerLocation = new LatLng(entry.getLatitude(), entry.getLongitude());

                // Use userId as title since WaitlistEntry doesn't have userName
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(markerLocation)
                        .title("User: " + entry.getUserId())
                        .snippet(entry.getLocationName() != null ? entry.getLocationName() : "No location name");

                mMap.addMarker(markerOptions);
            }
        }
        Log.d("EventMapActivity", "Displayed " + entries.size() + " markers on map");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}