package com.example.robotic_events_test;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class NotificationsAndroidTest {

    private Notifications notification;
    private long timestamp;

    @Before
    public void setUp() {
        timestamp = System.currentTimeMillis();
        notification = new Notifications(true, "Test message", "sender123", "receiver456", timestamp, "event789");
    }

    @Test
    public void testConstructorAndGetters() {
        // This test runs on the Android device/emulator
        assertTrue(notification.getRespondable());
        assertEquals("Test message", notification.getMessage());
        assertEquals("sender123", notification.getSenderId());
        assertEquals("receiver456", notification.getReceiverId());
        assertEquals(timestamp, notification.getTimestamp());
        assertEquals("event789", notification.getEventId());
    }

    @Test
    public void testSetters() {
        // This test runs on the Android device/emulator
        notification.setRespondable(false);
        notification.setMessage("New message");
        notification.setSenderId("newSender");
        notification.setReceiverId("newReceiver");
        notification.setTimestamp(123456789L);
        notification.setEventId("newEvent");
        notification.setId("docId");

        assertFalse(notification.getRespondable());
        assertEquals("New message", notification.getMessage());
        assertEquals("newSender", notification.getSenderId());
        assertEquals("newReceiver", notification.getReceiverId());
        assertEquals(123456789L, notification.getTimestamp());
        assertEquals("newEvent", notification.getEventId());
        assertEquals("docId", notification.getId());
    }
}
