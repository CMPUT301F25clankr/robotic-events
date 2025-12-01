package com.example.robotic_events_test;

import org.junit.Test;
import static org.junit.Assert.*;

public class WaitlistEntryTest {

    @Test
    public void testConstructorAndGetters() {
        WaitlistEntry entry = new WaitlistEntry("event123", "user456", 10.0, 20.0, "LocationName");
        
        assertEquals("event123", entry.getEventId());
        assertEquals("user456", entry.getUserId());
        assertEquals(10.0, entry.getLatitude(), 0.001);
        assertEquals(20.0, entry.getLongitude(), 0.001);
        assertEquals("LocationName", entry.getLocationName());
        
        // Timestamp should be close to now
        long now = System.currentTimeMillis();
        assertTrue(entry.getTimestamp() <= now);
        assertTrue(entry.getTimestamp() > now - 1000); // Created within last second
    }

    @Test
    public void testSimpleConstructor() {
        WaitlistEntry entry = new WaitlistEntry("event123", "user456");
        
        assertEquals("event123", entry.getEventId());
        assertEquals("user456", entry.getUserId());
        assertEquals(0.0, entry.getLatitude(), 0.001);
        assertEquals(0.0, entry.getLongitude(), 0.001);
        assertNull(entry.getLocationName());
    }

    @Test
    public void testSetters() {
        WaitlistEntry entry = new WaitlistEntry();
        entry.setEventId("e1");
        entry.setUserId("u1");
        entry.setLatitude(1.1);
        entry.setLongitude(2.2);
        entry.setLocationName("Loc");
        entry.setTimestamp(12345L);
        entry.setId("docId");

        assertEquals("e1", entry.getEventId());
        assertEquals("u1", entry.getUserId());
        assertEquals(1.1, entry.getLatitude(), 0.001);
        assertEquals(2.2, entry.getLongitude(), 0.001);
        assertEquals("Loc", entry.getLocationName());
        assertEquals(12345L, entry.getTimestamp());
        assertEquals("docId", entry.getId());
    }
}
