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
        try {
            onView(withId(R.id.notificationButton)).check(matches(isDisplayed()));
            
            onView(withId(R.id.profileButton)).perform(click());
            onView(withId(R.id.profileLogoutButton)).perform(click());
            Thread.sleep(3000);
        } catch (NoMatchingViewException e) {
        }

        String randomSuffix = String.valueOf(new Random().nextInt(100000));
        String testName = "TestUser" + randomSuffix;
        String testEmail = "test" + randomSuffix + "@example.com";
        String testPassword = "password123";

        onView(withId(R.id.signupButton)).perform(click());

        onView(withId(R.id.nameInput)).perform(replaceText(testName), closeSoftKeyboard());
        onView(withId(R.id.signupEmailInput)).perform(replaceText(testEmail), closeSoftKeyboard());
        onView(withId(R.id.signupPasswordInput)).perform(replaceText(testPassword), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordInput)).perform(replaceText(testPassword), closeSoftKeyboard());

        onView(withId(R.id.signupConfirmButton)).perform(click());

        Thread.sleep(6000);

        onView(withId(R.id.notificationButton)).check(matches(isDisplayed()));

        onView(withId(R.id.profileButton)).perform(click());

        onView(withId(R.id.profileLogoutButton)).perform(click());

        Thread.sleep(2000);

        onView(withId(R.id.emailInput)).check(matches(isDisplayed()));

        onView(withId(R.id.emailInput)).perform(replaceText(testEmail), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(replaceText(testPassword), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        Thread.sleep(6000);

        onView(withId(R.id.notificationButton)).check(matches(isDisplayed()));
    }
}
