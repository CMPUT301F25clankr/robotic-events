package com.example.robotic_events_test;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class EventTest {

    private Event event;
    private long timestamp;

    @Before
    public void setUp() {
        timestamp = System.currentTimeMillis();
        event = new Event("Test Event", "Test Location", timestamp, 100, 50.0, "Test Description", "Test Category", "organizer123", "http://image.url");
    }

    @Test
    public void testConstructorAndGetters() {
        assertEquals("Test Event", event.getTitle());
        assertEquals("Test Location", event.getLocation());
        assertEquals(timestamp, event.getDateTime());
        assertEquals(100, event.getTotalCapacity());
        assertEquals(50.0, event.getPrice(), 0.001);
        assertEquals("Test Description", event.getDescription());
        assertEquals("Test Category", event.getCategory());
        assertEquals("organizer123", event.getOrganizerId());
        assertEquals("http://image.url", event.getImageUrl());
        assertEquals("open", event.getStatus()); // Default status
    }

    @Test
    public void testSetters() {
        event.setTitle("New Title");
        event.setLocation("New Location");
        event.setTotalCapacity(200);
        event.setPrice(75.0);
        event.setStatus("closed");
        event.setId("event123");

        assertEquals("New Title", event.getTitle());
        assertEquals("New Location", event.getLocation());
        assertEquals(200, event.getTotalCapacity());
        assertEquals(75.0, event.getPrice(), 0.001);
        assertEquals("closed", event.getStatus());
        assertEquals("event123", event.getId());
    }

    @Test
    public void testDefaultConstructor() {
        Event emptyEvent = new Event();
        assertNull(emptyEvent.getTitle());
        assertNull(emptyEvent.getLocation());
        assertNull(emptyEvent.getStatus());
    }
}
