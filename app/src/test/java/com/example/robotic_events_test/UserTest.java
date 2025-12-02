package com.example.robotic_events_test;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserTest {

    private User user;

    @Before
    public void setUp() {
        user = new User("uid123", "John Doe", "john@example.com");
    }

    @Test
    public void testConstructorAndGetters() {
        assertEquals("uid123", user.getUid());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertFalse(user.isOrganizer()); //default
        assertTrue(user.isNotificationsEnabled()); //default
        assertFalse(user.isBanned()); //default
    }

    @Test
    public void testSetters() {
        user.setUid("newUid");
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        user.setPhone("1234567890");
        user.setLocation("New York");
        user.setOrganizer(true);
        user.setNotificationsEnabled(false);
        user.setBanned(true);

        assertEquals("newUid", user.getUid());
        assertEquals("Jane Doe", user.getName());
        assertEquals("jane@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhone());
        assertEquals("New York", user.getLocation());
        assertTrue(user.isOrganizer());
        assertFalse(user.isNotificationsEnabled());
        assertTrue(user.isBanned());
    }

    @Test
    public void testDefaultConstructor() {
        User emptyUser = new User();
        assertNull(emptyUser.getUid());
        assertNull(emptyUser.getName());
        assertNull(emptyUser.getEmail());
    }
}
