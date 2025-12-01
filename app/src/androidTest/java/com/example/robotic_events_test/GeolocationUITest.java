package com.example.robotic_events_test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
@SdkSuppress(minSdkVersion = 34, maxSdkVersion = 34)
public class GeolocationUITest {

    @Test
    public void testEnableDisableGeolocation() {
        Event dummyEvent = new Event();
        dummyEvent.setId("test_geo_event_id");
        dummyEvent.setTitle("Test Geo Event");
        dummyEvent.setGeolocationRequired(false);

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventGeolocationSettingsActivity.class);
        intent.putExtra("eventId", dummyEvent.getId());
        intent.putExtra("event", dummyEvent);

        try (ActivityScenario<EventGeolocationSettingsActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.geolocationSwitch)).check(matches(isNotChecked()));
            onView(withId(R.id.geolocationDescriptionText)).check(matches(withText(containsString("DISABLED"))));

            onView(withId(R.id.geolocationSwitch)).perform(click());

            onView(withId(R.id.geolocationSwitch)).check(matches(isChecked()));
            onView(withId(R.id.geolocationDescriptionText)).check(matches(withText(containsString("ENABLED"))));

            onView(withId(R.id.saveGeolocationButton)).perform(click());

        }
    }
}
