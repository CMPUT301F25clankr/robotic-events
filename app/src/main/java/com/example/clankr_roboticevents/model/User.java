package com.example.clankr_roboticevents.model;

/**
 * Immutable core profile for a user of the app.
 *
 * <p>For now this is a lightweight POJO used by the domain layer.
 * Persistence (e.g., Firebase) can map fields 1:1 later.</p>
 */
public class User {

    private final String userId;
    private String displayName;
    private String email;
    private String phone;              // optional
    private boolean notificationsEnabled;
    private Role role;

    /**
     * Creates a new user.
     *
     * @param userId  stable unique id (e.g., UID from auth)
     * @param displayName visible name
     * @param email   contact email
     * @param phone   optional phone number
     * @param role    USER or ORGANIZER
     */
    public User(String userId, String displayName, String email, String phone, Role role) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
        this.role = role == null ? Role.USER : role;
        this.notificationsEnabled = true;
    }

    // --- getters / setters ---
    public String getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public Role getRole() { return role; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setNotificationsEnabled(boolean enabled) { this.notificationsEnabled = enabled; }
    public void setRole(Role role) { this.role = role == null ? Role.USER : role; }
}
