package com.example.robotic_events_test;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class EventTest {

    private Event event;
    private long timestamp;
    private long deadline;

    @Before
    public void setUp() {
        timestamp = System.currentTimeMillis();
        deadline = timestamp + 86400000; // 1 day later
        event = new Event(
                "Test Event", 
                "Test Location", 
                timestamp, 
                100, 
                50.0, 
                "Test Description", 
                "Test Category", 
                "organizer123", 
                "http://image.url/icon",
                "http://image.url/banner",
                deadline
        );
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
        assertEquals("http://image.url/icon", event.getImageUrl());
        assertEquals("http://image.url/banner", event.getBannerUrl());
        assertEquals(deadline, event.getRegistrationDeadline());
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
        event.setBannerUrl("newBanner");
        event.setRegistrationDeadline(123456789L);

        assertEquals("New Title", event.getTitle());
        assertEquals("New Location", event.getLocation());
        assertEquals(200, event.getTotalCapacity());
        assertEquals(75.0, event.getPrice(), 0.001);
        assertEquals("closed", event.getStatus());
        assertEquals("event123", event.getId());
        assertEquals("newBanner", event.getBannerUrl());
        assertEquals(123456789L, event.getRegistrationDeadline());
    }

    @Test
    public void testDefaultConstructor() {
        Event emptyEvent = new Event();
        assertNull(emptyEvent.getTitle());
        assertNull(emptyEvent.getLocation());
        assertNull(emptyEvent.getStatus());
    }
}
