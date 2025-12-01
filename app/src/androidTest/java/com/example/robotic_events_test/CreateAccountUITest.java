package com.example.robotic_events_test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
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

import java.util.Random;

@RunWith(AndroidJUnit4.class)
@LargeTest
@SdkSuppress(minSdkVersion = 34, maxSdkVersion = 34)
public class CreateAccountUITest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void testCreateAccountLogoutLogin() throws InterruptedException {
        // 0. CHECK INITIAL STATE: If already logged in, LOGOUT first.
        try {
            // Check for a view unique to Main Screen
            onView(withId(R.id.notificationButton)).check(matches(isDisplayed()));
            
            // If we are here, we are on Main Screen. LOGOUT.
            onView(withId(R.id.profileButton)).perform(click());
            onView(withId(R.id.profileLogoutButton)).perform(click());
            Thread.sleep(3000); // Wait for logout transition
        } catch (NoMatchingViewException e) {
            // Not on Main Screen, assume on Login Screen (or Signup).
            // Proceed normal flow.
        }

        // 1. Generate random user credentials to avoid conflict
        String randomSuffix = String.valueOf(new Random().nextInt(100000));
        String testName = "TestUser" + randomSuffix;
        String testEmail = "test" + randomSuffix + "@example.com";
        String testPassword = "password123";

        // 2. Go to Signup Screen (We should be on Login Screen now)
        onView(withId(R.id.signupButton)).perform(click());

        // 3. Fill Signup Form
        onView(withId(R.id.nameInput)).perform(replaceText(testName), closeSoftKeyboard());
        onView(withId(R.id.signupEmailInput)).perform(replaceText(testEmail), closeSoftKeyboard());
        onView(withId(R.id.signupPasswordInput)).perform(replaceText(testPassword), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordInput)).perform(replaceText(testPassword), closeSoftKeyboard());

        // 4. Submit Signup (Removed scrollTo() as root is not ScrollView)
        onView(withId(R.id.signupConfirmButton)).perform(click());

        // Wait for registration and navigation to Main (approx 6s for safe measure)
        Thread.sleep(6000);

        // 5. Verify we are on Main Screen (Notification Button visible)
        onView(withId(R.id.notificationButton)).check(matches(isDisplayed()));

        // 6. Navigate to Profile to Logout
        onView(withId(R.id.profileButton)).perform(click());

        // 7. Click Logout
        onView(withId(R.id.profileLogoutButton)).perform(click());

        // Wait for logout
        Thread.sleep(2000);

        // 8. Verify we are back on Login Screen
        onView(withId(R.id.emailInput)).check(matches(isDisplayed()));

        // 9. Login with the same user
        onView(withId(R.id.emailInput)).perform(replaceText(testEmail), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(replaceText(testPassword), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        // Wait for login
        Thread.sleep(6000);

        // 10. Verify we are back on Main Screen
        onView(withId(R.id.notificationButton)).check(matches(isDisplayed()));
    }
}
