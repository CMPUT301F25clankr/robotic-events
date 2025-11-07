package com.example.robotic_events_test;

/**
 * Represents a user in the Robotic Events application.
 * <p>
 * This class contains information about a user, including their name, email, contact details, and roles (such as whether they are an organizer).
 * It is used for user authentication, profile management, and Firebase data storage/retrieval.
 * </p>
 */
public class User {

    /** Identifier for the user*/
    private String uid;

    /** Full name of the user. */
    private String name;

    /** Email address associated with the user account. */
    private String email;

    /** User's phone number */
    private String phone;

    /** User's location */
    private String location;

    /** Indicates whether the user has organizer privileges. */
    private boolean isOrganizer;

    /** Indicates whether the user has notifications enabled. */
    private boolean notificationsEnabled;

    /** Indicates whether the user account is banned or suspended. */
    private boolean banned;


    public User() {}

    /**
     * Constructs a new User with basic required information.
     *
     * @param uid   Unique identifier for the user (from Firebase Authentication)
     * @param name  User's full name
     * @param email User's email address
     */
    public User(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.notificationsEnabled = true;
        this.isOrganizer = false;
        this.banned = false;
    }

    /**
     * Returns the unique user ID.
     *
     * @return the user's UID
     */
    public String getUid() { return uid; }

    /**
     * Sets the unique user ID.
     *
     * @param uid the user's UID
     */
    public void setUid(String uid) { this.uid = uid; }

    /**
     * Returns the user's name.
     *
     * @return the user's name
     */
    public String getName() { return name; }

    /**
     * Sets the user's name.
     *
     * @param name the user's name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the user's email address.
     *
     * @return the user's email
     */
    public String getEmail() { return email; }

    /**
     * Sets the user's email address.
     *
     * @param email the user's email
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns the user's phone number.
     *
     * @return the user's phone number
     */
    public String getPhone() { return phone; }

    /**
     * Sets the user's phone number.
     *
     * @param phone the user's phone number
     */
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * Returns the user's location.
     *
     * @return the user's location
     */
    public String getLocation() { return location; }

    /**
     * Sets the user's location.
     *
     * @param location the user's location
     */
    public void setLocation(String location) { this.location = location; }

    /**
     * Returns whether the user is an organizer.
     *
     * @return true if the user is an organizer, false otherwise
     */
    public boolean isOrganizer() { return isOrganizer; }

    /**
     * Sets whether the user is an organizer.
     *
     * @param organizer true if the user should have organizer privileges
     */
    public void setOrganizer(boolean organizer) { isOrganizer = organizer; }

    /**
     * Returns whether the user has notifications enabled.
     *
     * @return true if notifications are enabled, false otherwise
     */
    public boolean isNotificationsEnabled() { return notificationsEnabled; }

    /**
     * Sets whether the user has notifications enabled.
     *
     * @param notificationsEnabled true to enable notifications, false to disable
     */
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    /**
     * Returns whether the user is banned.
     *
     * @return true if the user is banned, false otherwise
     */
    public boolean isBanned() { return banned; }

    /**
     * Sets whether the user is banned.
     *
     * @param banned true to mark the user as banned, false otherwise
     */
    public void setBanned(boolean banned) { this.banned = banned; }
}
