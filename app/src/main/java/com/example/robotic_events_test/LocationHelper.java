package com.example.robotic_events_test;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for handling device geolocation
 * Handles permissions, location fetching, and reverse geocoding
 */
public class LocationHelper {
    private static final String TAG = "LocationHelper";
    public static final int PERMISSION_REQUEST_CODE = 100;
    public static final String PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private final Geocoder geocoder;

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.geocoder = new Geocoder(context, Locale.getDefault());
    }

    /**
     * Check if location permission is granted
     */
    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context, PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Get current device location
     * Must be called after verifying permissions
     */
    public Task<Location> getCurrentLocation() {
        if (hasLocationPermission()) {
            return fusedLocationClient.getLastLocation();
        } else {
            Log.e(TAG, "Location permission not granted");
            return null;
        }
    }

    /**
     * Get location name from coordinates (reverse geocoding)
     */
    public String getLocationName(double latitude, double longitude) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder locationName = new StringBuilder();

                if (address.getThoroughfare() != null) {
                    locationName.append(address.getThoroughfare());
                }
                if (address.getLocality() != null) {
                    if (locationName.length() > 0) locationName.append(", ");
                    locationName.append(address.getLocality());
                }
                if (address.getAdminArea() != null) {
                    if (locationName.length() > 0) locationName.append(", ");
                    locationName.append(address.getAdminArea());
                }

                return locationName.toString();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting location name", e);
        }
        return "Unknown Location";
    }

    /**
     * Check if context has location permission and return full result
     */
    public LocationData extractLocationData(Location location, String userName) {
        if (location == null) {
            return null;
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String locationName = getLocationName(latitude, longitude);

        return new LocationData(latitude, longitude, locationName);
    }

    /**
     * Inner class to hold location data
     */
    public static class LocationData {
        public double latitude;
        public double longitude;
        public String locationName;

        public LocationData(double latitude, double longitude, String locationName) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.locationName = locationName;
        }
    }
}