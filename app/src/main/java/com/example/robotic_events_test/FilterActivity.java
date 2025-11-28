package com.example.robotic_events_test;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class FilterActivity extends AppCompatActivity {

    private EditText categoryFilterInput;
    private RadioGroup priceFilterGroup;
    private Slider distanceSlider;
    private TextView distanceValueText;
    private Button applyFilterButton;
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        Toolbar toolbar = findViewById(R.id.filterToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        categoryFilterInput = findViewById(R.id.categoryFilterInput);
        priceFilterGroup = findViewById(R.id.priceFilterGroup);
        distanceSlider = findViewById(R.id.distanceSlider);
        distanceValueText = findViewById(R.id.distanceValueText);
        applyFilterButton = findViewById(R.id.applyFilterButton);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        distanceSlider.addOnChangeListener((slider, value, fromUser) ->
                distanceValueText.setText(String.format(Locale.US, "%.0f km", value)));

        applyFilterButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                getLastLocationAndApplyFilters();
            }
        });
    }

    private void getLastLocationAndApplyFilters() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            lastKnownLocation = location;
                            applyFilters();
                        } else {
                            Toast.makeText(this, "Device location not found. Trying profile location.", Toast.LENGTH_SHORT).show();
                            fetchUserLocationFromProfile();
                        }
                    });
        } else {
            fetchUserLocationFromProfile();
        }
    }

    private void fetchUserLocationFromProfile() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Log in to use your profile location.", Toast.LENGTH_SHORT).show();
            applyFilters();
            return;
        }

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String locationName = documentSnapshot.getString("location");
                        if (locationName != null && !locationName.isEmpty()) {
                            geocodeProfileLocation(locationName);
                        } else {
                            Toast.makeText(this, "No location found in your profile.", Toast.LENGTH_SHORT).show();
                            applyFilters();
                        }
                    } else {
                        Toast.makeText(this, "Could not load user profile.", Toast.LENGTH_SHORT).show();
                        applyFilters();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile.", Toast.LENGTH_SHORT).show();
                    applyFilters();
                });
    }

    private void geocodeProfileLocation(String locationName) {
        NominatimHelper.getCoordinatesFromAddress(locationName, new NominatimHelper.NominatimCallback() {
            @Override
            public void onCoordinatesResolved(Location location) {
                lastKnownLocation = location;
                Toast.makeText(FilterActivity.this, "Using profile location: " + locationName, Toast.LENGTH_SHORT).show();
                applyFilters();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(FilterActivity.this, "Could not find coordinates for: " + locationName, Toast.LENGTH_SHORT).show();
                applyFilters();
            }
        });
    }

    private void applyFilters() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("category", categoryFilterInput.getText().toString());

        int selectedPriceId = priceFilterGroup.getCheckedRadioButtonId();
        if (selectedPriceId == R.id.freePriceRadio) {
            resultIntent.putExtra("price", "free");
        } else if (selectedPriceId == R.id.paidPriceRadio) {
            resultIntent.putExtra("price", "paid");
        } else {
            resultIntent.putExtra("price", "any");
        }

        resultIntent.putExtra("distance", distanceSlider.getValue());
        if (lastKnownLocation != null) {
            resultIntent.putExtra("latitude", lastKnownLocation.getLatitude());
            resultIntent.putExtra("longitude", lastKnownLocation.getLongitude());
        }

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocationAndApplyFilters();
            } else {
                Toast.makeText(this, "Location permission denied. Trying profile location.", Toast.LENGTH_SHORT).show();
                fetchUserLocationFromProfile();
            }
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
}
