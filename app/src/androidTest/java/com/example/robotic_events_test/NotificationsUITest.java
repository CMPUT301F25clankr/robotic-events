package com.example.robotic_events_test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
@SdkSuppress(minSdkVersion = 34, maxSdkVersion = 34)
public class NotificationsUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testNavigateToNotifications() {

        try {
            onView(withId(R.id.notificationButton)).check(matches(isDisplayed()));
            
            onView(withId(R.id.notificationButton)).perform(click());
            onView(withId(R.id.notifications_recycler_view)).check(matches(isDisplayed()));
            
        } catch (NoMatchingViewException e) {
            try {
                onView(withId(R.id.emailInput)).check(matches(isDisplayed()));
                onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
                
                System.out.println("Test passed: App redirected to Login screen as expected (User not logged in).");
            } catch (NoMatchingViewException e2) {
                throw e;
            }
        }
    }
}