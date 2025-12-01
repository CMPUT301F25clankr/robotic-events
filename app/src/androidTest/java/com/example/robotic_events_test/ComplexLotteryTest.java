package com.example.robotic_events_test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

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

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
public class ComplexLotteryTest {

    // Static state to persist across separate test methods in the same run
    private static final String RANDOM_SUFFIX = String.valueOf(new Random().nextInt(100000));
    private static final String ORG_EMAIL = "org" + RANDOM_SUFFIX + "@test.com";
    private static final String ORG_PASS = "password";
    private static final String USER1_EMAIL = "user1_" + RANDOM_SUFFIX + "@test.com";
    private static final String USER2_EMAIL = "user2_" + RANDOM_SUFFIX + "@test.com";
    private static final String EVENT_TITLE = "Lottery Event " + RANDOM_SUFFIX;
    private static final int CAPACITY = 2;

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    /**
     * PHASE 1: Organizer Setup
     * - Signup as Organizer
     * - Create Event
     */
    @Test
    public void test1_OrganizerCreatesEvent() throws InterruptedException {
        ensureLoggedOut();

        // 1.1 Signup as Organizer
        signup(ORG_EMAIL, ORG_PASS, true);

        // 1.2 Create Event
        onView(withId(R.id.fab_add_event)).perform(click());
        Thread.sleep(1000);
        
        onView(withId(R.id.eventTitleSetter)).perform(replaceText(EVENT_TITLE), closeSoftKeyboard());
        onView(withId(R.id.eventCapacitySetter)).perform(replaceText(String.valueOf(CAPACITY)), closeSoftKeyboard());
        
        onView(withId(R.id.eventCreationConfirm)).perform(click());
        
        Thread.sleep(5000); // Wait for save
        
        logout();
    }

    /**
     * PHASE 2: Users Join
     * - Signup User 1 & Join
     * - Signup User 2 & Join
     */
    @Test
    public void test2_UsersJoin() throws InterruptedException {
        ensureLoggedOut();

        // User 1
        signup(USER1_EMAIL, ORG_PASS, false);
        joinEvent(EVENT_TITLE);
        logout();

        // User 2
        signup(USER2_EMAIL, ORG_PASS, false);
        joinEvent(EVENT_TITLE);
        logout();
    }

    /**
     * PHASE 3: Organizer Runs Lottery
     * - Login as Organizer
     * - Check Milestones
     * - Run Lottery
     */
    @Test
    public void test3_OrganizerRunsLottery() throws InterruptedException {
        ensureLoggedOut();

        login(ORG_EMAIL, ORG_PASS);

        // Check Organizer Notifications (Milestones)
        onView(withId(R.id.notificationButton)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.notifications_recycler_view)).check(matches(isDisplayed()));
        androidx.test.espresso.Espresso.pressBack(); 

        // Find and Open Event
        searchAndOpenEvent(EVENT_TITLE);
        
        // Run Lottery
        onView(withId(R.id.runLotteryButton)).perform(click());
        Thread.sleep(5000); // Wait for lottery logic

        androidx.test.espresso.Espresso.pressBack(); 
        logout();
    }

    /**
     * PHASE 4: Users Check Results
     * - Login User 1 & Check
     * - Login User 2 & Check
     */
    @Test
    public void test4_UsersCheckResults() throws InterruptedException {
        ensureLoggedOut();

        // Check User 1
        login(USER1_EMAIL, ORG_PASS);
        onView(withId(R.id.notificationButton)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.notifications_recycler_view)).check(matches(isDisplayed()));
        androidx.test.espresso.Espresso.pressBack(); 
        logout();

        // Check User 2
        login(USER2_EMAIL, ORG_PASS);
        onView(withId(R.id.notificationButton)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.notifications_recycler_view)).check(matches(isDisplayed()));
        androidx.test.espresso.Espresso.pressBack(); 
        logout();
    }

    // =================================================================
    // HELPER METHODS
    // =================================================================

    private void ensureLoggedOut() throws InterruptedException {
        try {
            // Check if we are on Main Screen (Notification Button visible)
            onView(withId(R.id.notificationButton)).check(matches(isDisplayed()));
            // If visible, logout
            logout();
        } catch (NoMatchingViewException e) {
            // Not logged in, assume on Login Screen. Good.
        }
    }

    private void signup(String email, String password, boolean isOrganizer) throws InterruptedException {
        // Give time for Login screen to settle if we just logged out
        Thread.sleep(2000); 
        
        onView(withId(R.id.signupButton)).perform(click());
        Thread.sleep(1000); 
        
        onView(withId(R.id.nameInput)).perform(replaceText("Test Name"), closeSoftKeyboard());
        onView(withId(R.id.signupEmailInput)).perform(replaceText(email), closeSoftKeyboard());
        onView(withId(R.id.signupPasswordInput)).perform(replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordInput)).perform(replaceText(password), closeSoftKeyboard());
        
        if (isOrganizer) {
            onView(withId(R.id.organizerRadio)).perform(click());
        }
        
        onView(withId(R.id.signupConfirmButton)).perform(click());
        Thread.sleep(8000); 
    }

    private void login(String email, String password) throws InterruptedException {
        // Give time for Login screen to settle
        Thread.sleep(2000);
        
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
