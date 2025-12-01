package com.example.robotic_events_test;

import android.content.Context;
import android.location.Location;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 34, maxSdkVersion = 34)
public class LocationHelperAndroidTest {

    private LocationHelper locationHelper;
    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        locationHelper = new LocationHelper(context);
    }

    @Test
    public void testLocationExtractionWithMockLocation() {
        // Mock a location object
        Location mockLocation = new Location("mock_provider");
        mockLocation.setLatitude(37.422); // Mountain View
        mockLocation.setLongitude(-122.084);
        mockLocation.setTime(System.currentTimeMillis());

        LocationHelper.LocationData data = locationHelper.extractLocationData(mockLocation, "TestUser");

        assertNotNull(data);
        assertEquals(37.422, data.latitude, 0.001);
        assertEquals(-122.084, data.longitude, 0.001);
        
        // Note: We cannot strictly assert the locationName here because Geocoder 
        // might not be implemented on all emulators or might fail without network.
        // However, extractLocationData should at least return a non-null object.
    }
}
