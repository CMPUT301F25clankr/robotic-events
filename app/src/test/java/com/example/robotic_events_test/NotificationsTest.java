package com.example.robotic_events_test;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class NotificationsTest {

    private Notifications notification;
    private long timestamp;

    @Before
    public void setUp() {
        timestamp = System.currentTimeMillis();
        notification = new Notifications(true, "Test message", "sender123", "receiver456", timestamp, "event789");
    }

    @Test
    public void testConstructorAndGetters() {
        assertTrue(notification.getRespondable());
        assertEquals("Test message", notification.getMessage());
        assertEquals("sender123", notification.getSenderId());
        assertEquals("receiver456", notification.getReceiverId());
        assertEquals(timestamp, notification.getTimestamp());
        assertEquals("event789", notification.getEventId());
    }

    @Test
    public void testSetters() {
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

    @Test
    public void testDefaultConstructor() {
        Notifications emptyNotification = new Notifications();
        assertNull(emptyNotification.getRespondable());
        assertNull(emptyNotification.getMessage());
        assertNull(emptyNotification.getSenderId());
        assertNull(emptyNotification.getReceiverId());
        assertEquals(0L, emptyNotification.getTimestamp());
        assertNull(emptyNotification.getEventId());
    }
}
