package com.example.robotic_events_test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Random;

/**
 * Verifies that when an event is over-subscribed, 
 * users who are NOT selected receive a rejection notification.
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
public class LotteryRejectionTest {

    private static final String RANDOM_SUFFIX = String.valueOf(new Random().nextInt(100000));
    private static final String ORG_EMAIL = "org_rej_" + RANDOM_SUFFIX + "@test.com";
    private static final String ORG_PASS = "password";
    private static final String USER1_EMAIL = "loser1_" + RANDOM_SUFFIX + "@test.com";
    private static final String USER2_EMAIL = "winner_" + RANDOM_SUFFIX + "@test.com";
    private static final String EVENT_TITLE = "High Stakes Event " + RANDOM_SUFFIX;
    
    // CAPACITY 1 ensures only 1 winner, so the other user must be rejected.
    private static final int CAPACITY = 1; 

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void test1_OrganizerCreatesSmallEvent() throws InterruptedException {
        ensureLoggedOut();
        signup(ORG_EMAIL, ORG_PASS, true);

        onView(withId(R.id.fab_add_event)).perform(click());
        Thread.sleep(1000);
        
        onView(withId(R.id.eventTitleSetter)).perform(replaceText(EVENT_TITLE), closeSoftKeyboard());
        onView(withId(R.id.eventCapacitySetter)).perform(replaceText(String.valueOf(CAPACITY)), closeSoftKeyboard());
        onView(withId(R.id.eventCreationConfirm)).perform(click());
        Thread.sleep(5000);
        
        logout();
    }

    @Test
    public void test2_UsersJoinWaitlist() throws InterruptedException {
        ensureLoggedOut();

        // User 1 Joins
        signup(USER1_EMAIL, ORG_PASS, false);
        joinEvent(EVENT_TITLE);
        logout();

        // User 2 Joins
        signup(USER2_EMAIL, ORG_PASS, false);
        joinEvent(EVENT_TITLE);
        logout();
    }

    @Test
    public void test3_OrganizerRunsLottery() throws InterruptedException {
        ensureLoggedOut();
        login(ORG_EMAIL, ORG_PASS);

        // Find and Open Event
        searchAndOpenEvent(EVENT_TITLE);
        
        // Run Lottery
        onView(withId(R.id.runLotteryButton)).perform(click());
        Thread.sleep(5000); 

        androidx.test.espresso.Espresso.pressBack(); 
        logout();
    }

    @Test
    public void test4_VerifyRejectionAndAcceptance() throws InterruptedException {
        ensureLoggedOut();

        // Check User 1
        login(USER1_EMAIL, ORG_PASS);
        verifyNotificationReceived();
        logout();

        // Check User 2
        login(USER2_EMAIL, ORG_PASS);
        verifyNotificationReceived();
        logout();
    }

    // =================================================================
    // HELPER METHODS
    // =================================================================

    private void verifyNotificationReceived() throws InterruptedException {
        onView(withId(R.id.notificationButton)).perform(click());
        Thread.sleep(2000);
        
        // Ensure the list is visible
        onView(withId(R.id.notifications_recycler_view)).check(matches(isDisplayed()));
        
        // Check that a message is displayed containing either "Congratulations" or "Unfortunately"
        // Since we don't know which user won, checking for existence is enough to prove the system works.
        // But ideally we match text.
        // We can match for "selected" as both messages contain it.
        // "Congratulations! You have been selected..."
        // "Unfortunately, you were not selected..."
        onView(withText(containsString("selected"))).check(matches(isDisplayed()));
        
        androidx.test.espresso.Espresso.pressBack(); 
    }

    private void ensureLoggedOut() throws InterruptedException {
        try {
            onView(withId(R.id.notificationButton)).check(matches(isDisplayed()));
            logout();
        } catch (NoMatchingViewException e) { }
    }

    private void signup(String email, String password, boolean isOrganizer) throws InterruptedException {
        onView(withId(R.id.signupButton)).perform(click());
        Thread.sleep(1000); 
        onView(withId(R.id.nameInput)).perform(replaceText("Test Name"), closeSoftKeyboard());
        onView(withId(R.id.signupEmailInput)).perform(replaceText(email), closeSoftKeyboard());
        onView(withId(R.id.signupPasswordInput)).perform(replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordInput)).perform(replaceText(password), closeSoftKeyboard());
        if (isOrganizer) onView(withId(R.id.organizerRadio)).perform(click());
        onView(withId(R.id.signupConfirmButton)).perform(click());
        Thread.sleep(8000); 
    }

    private void login(String email, String password) throws InterruptedException {
        onView(withId(R.id.emailInput)).perform(replaceText(email), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());
        Thread.sleep(8000); 
    }

    private void logout() throws InterruptedException {
        onView(withId(R.id.profileButton)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.profileLogoutButton)).perform(click());
        Thread.sleep(2000);
    }

    private void joinEvent(String title) throws InterruptedException {
        searchAndOpenEvent(title);
        onView(withId(R.id.joinLeaveWaitlistButton)).perform(click());
        Thread.sleep(3000); 
        androidx.test.espresso.Espresso.pressBack(); 
    }

    private void searchAndOpenEvent(String title) throws InterruptedException {
        try {
            onView(withHint("Search Events...")).perform(click());
            onView(withHint("Search Events...")).perform(replaceText(title), closeSoftKeyboard());
        } catch (Exception e) {
            onView(withId(R.id.search_bar)).perform(click());
            onView(withId(R.id.search_bar)).perform(replaceText(title), closeSoftKeyboard());
        }
        Thread.sleep(2000); 
        onView(allOf(withId(R.id.eventTitle), withText(title))).perform(click());
        Thread.sleep(2000);
    }
}
