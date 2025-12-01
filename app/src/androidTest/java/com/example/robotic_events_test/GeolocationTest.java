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
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "pm grant " + InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName()
                        + " android.permission.ACCESS_FINE_LOCATION");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "pm grant " + InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName()
                        + " android.permission.ACCESS_COARSE_LOCATION");
    }

    @Test
    public void testEventGeolocationSettingsLaunch() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventGeolocationSettingsActivity.class);
        
        Event dummyEvent = new Event();
        dummyEvent.setId("test_event_id");
        dummyEvent.setTitle("Test Geo Event");
        
        intent.putExtra("eventId", dummyEvent.getId());
        intent.putExtra("event", dummyEvent);
        
        try (ActivityScenario<EventGeolocationSettingsActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(android.R.id.content)).check(matches(isDisplayed()));
        }
    }
    
    @Test
    public void testLocationHelper() {
        Context context = ApplicationProvider.getApplicationContext();
        LocationHelper helper = new LocationHelper(context);

        assert(helper.hasLocationPermission());
    }
}
