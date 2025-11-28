package com.example.robotic_events_test;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LocationHelperTest {

    @Mock
    private Context context;

    @Mock
    private Location location;

    private LocationHelper locationHelper;

    @Before
    public void setUp() {
        locationHelper = new LocationHelper(context);
    }

    @Test
    public void testExtractLocationData() {
        when(location.getLatitude()).thenReturn(37.7749);
        when(location.getLongitude()).thenReturn(-122.4194);

        // We cannot mock Geocoder easily in unit tests as it's an Android class requiring context
        // However, we can verify that extractLocationData handles the location object correctly.
        // The getLocationName method inside might fail or return "Unknown Location" if Geocoder isn't mocked,
        // but basic extraction should work.
        
        // Note: LocationHelper.getLocationName uses Geocoder which might throw exception in unit tests
        // or return null if backend service is not available.
        // For pure unit testing without Robolectric, we test what we can.

        LocationHelper.LocationData data = locationHelper.extractLocationData(location, "TestUser");

        assertNotNull(data);
        assertEquals(37.7749, data.latitude, 0.0001);
        assertEquals(-122.4194, data.longitude, 0.0001);
        // locationName might be "Unknown Location" or null depending on Geocoder behavior in mock environment
    }

    @Test
    public void testExtractLocationData_NullLocation() {
        LocationHelper.LocationData data = locationHelper.extractLocationData(null, "TestUser");
        assertNull(data);
    }
}
