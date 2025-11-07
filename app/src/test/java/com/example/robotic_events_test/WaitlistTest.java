package com.example.robotic_events_test;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class WaitlistTest {

    private Waitlist waitlist;

    @Before
    public void setUp() {
        waitlist = new Waitlist("event123", "userABC", 1700000000000L);
    }

    @Test
    public void testConstructorAndGetters() {
        assertEquals("event123", waitlist.getEventId());
        assertEquals("userABC", waitlist.getUserId());
        assertEquals(1700000000000L, waitlist.getTimestamp());
        assertEquals("waiting", waitlist.getState());
    }

    @Test
    public void testSetters() {
        waitlist.setEventId("newEvent");
        waitlist.setUserId("newUser");
        waitlist.setTimestamp(12345L);
        waitlist.setState("joined");

        assertEquals("newEvent", waitlist.getEventId());
        assertEquals("newUser", waitlist.getUserId());
        assertEquals(12345L, waitlist.getTimestamp());
        assertEquals("joined", waitlist.getState());
    }

    @Test
    public void testDefaultStateIsWaiting() {
        Waitlist newEntry = new Waitlist("e1", "u1", System.currentTimeMillis());
        assertEquals("waiting", newEntry.getState());
    }
}

