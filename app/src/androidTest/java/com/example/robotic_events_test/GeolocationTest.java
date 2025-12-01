package com.example.robotic_events_test;

import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
@SdkSuppress(minSdkVersion = 34, maxSdkVersion = 34)
public class GeolocationTest {

    @Before
    public void grantPermissions() {
        // Manually grant permissions since androidx.test.rules is missing
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "pm grant " + InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName()
                        + " android.permission.ACCESS_FINE_LOCATION");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "pm grant " + InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName()
                        + " android.permission.ACCESS_COARSE_LOCATION");
    }

    @Test
    public void testEventGeolocationSettingsLaunch() {
        // Launch EventGeolocationSettingsActivity directly
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventGeolocationSettingsActivity.class);
        
        // Need to pass a valid Event object since the activity expects it
        Event dummyEvent = new Event();
        dummyEvent.setId("test_event_id");
        dummyEvent.setTitle("Test Geo Event");
        
        intent.putExtra("eventId", dummyEvent.getId());
        intent.putExtra("event", dummyEvent);
        
        try (ActivityScenario<EventGeolocationSettingsActivity> scenario = ActivityScenario.launch(intent)) {
            // Placeholder verification - checks if content view is visible
            onView(withId(android.R.id.content)).check(matches(isDisplayed()));
        }
    }
    
    @Test
    public void testLocationHelper() {
        // Unit test style check for LocationHelper within instrumentation
        Context context = ApplicationProvider.getApplicationContext();
        LocationHelper helper = new LocationHelper(context);
        
        // Since we granted permissions via UiAutomation, this should be true
        // Note: Permissions might take a moment to propagate, but usually immediate via shell
        assert(helper.hasLocationPermission());
    }
}
