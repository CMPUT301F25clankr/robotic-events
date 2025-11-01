package com.example.clankr_roboticevents.domain;

import com.example.clankr_roboticevents.model.Event;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link EventManager}.
 */
public class EventManagerTest {

    @Test
    public void register_attendeeWhenCapacity_available() {
        Event e = new Event("e1", "Title", "desc", "Hall", "2025-11-01T10:00", 2, "org1");
        EventManager m = new EventManager();

        boolean attendee = m.register(e, "u1");
        assertTrue(attendee);
        assertTrue(e.isAttending("u1"));
        assertEquals(1, e.getAttendees().size());
        assertTrue(e.getWaitlist().isEmpty());
    }

    @Test
    public void register_waitlistWhenFull_addsToWaitlist() {
        Event e = new Event("e1", "Title", "desc", "Hall", "2025-11-01T10:00", 1, "org1");
        EventManager m = new EventManager();

        assertTrue(m.register(e, "u1"));          // fills capacity
        assertFalse(m.register(e, "u2"));         // waitlisted
        assertTrue(e.isWaitlisted("u2"));
        assertEquals(1, e.getWaitlist().size());
    }

    @Test
    public void cancel_promotesNextFromWaitlist() {
        Event e = new Event("e1", "Title", "desc", "Hall", "2025-11-01T10:00", 1, "org1");
        EventManager m = new EventManager();

        assertTrue(m.register(e, "u1"));
        assertFalse(m.register(e, "u2"));         // waitlisted

        String promoted = m.cancel(e, "u1");
        assertEquals("u2", promoted);
        assertTrue(e.isAttending("u2"));
        assertTrue(e.getWaitlist().isEmpty());
    }

    @Test
    public void toggle_behaviour() {
        Event e = new Event("e1", "Title", "desc", "Hall", "2025-11-01T10:00", 1, "org1");
        EventManager m = new EventManager();

        assertEquals("ATTENDING", m.toggle(e, "u1"));
        assertEquals("REMOVED", m.toggle(e, "u1"));
        assertEquals("ATTENDING", m.toggle(e, "u1"));
        assertEquals("WAITLISTED", m.toggle(e, "u2"));
    }
}
