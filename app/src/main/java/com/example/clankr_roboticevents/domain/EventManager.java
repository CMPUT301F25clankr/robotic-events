package com.example.clankr_roboticevents.domain;

import com.example.clankr_roboticevents.model.Event;

/**
 * Stateless service for core event operations used by UI/ViewModels.
 *
 * <p>This layer encapsulates business rules and keeps Activities/Fragments
 * thin. Replace or extend with repository calls later (e.g., Firestore).</p>
 */
public class EventManager {

    /**
     * Registers a user for an event. If full, adds to waitlist.
     *
     * @param event  target event (non-null)
     * @param userId user identifier
     * @return true if user became an attendee; false if placed on waitlist
     */
    public boolean register(Event event, String userId) {
        if (event == null || userId == null || userId.isEmpty()) return false;
        if (event.addAttendee(userId)) return true;
        event.addToWaitlist(userId);
        return false;
    }

    /**
     * Cancels a user's attendance or waitlist spot, then promotes the next
     * waitlisted user if a seat opens.
     *
     * @param event  event whose lists are modified
     * @param userId user to remove
     * @return the userId of a promoted waitlisted user, or null if none
     */
    public String cancel(Event event, String userId) {
        if (event == null || userId == null || userId.isEmpty()) return null;
        boolean wasAttending = event.isAttending(userId);
        event.removeUser(userId);

        // Only promote if a seat opened from an attendee leaving
        if (wasAttending) {
            return event.promoteNextFromWaitlist();
        }
        return null;
    }

    /**
     * Toggles a user's participation: if attending → cancel; if waitlisted → remove;
     * if not present → register (attend or waitlist).
     *
     * @return "ATTENDING", "WAITLISTED", or "REMOVED" describing the final state.
     */
    public String toggle(Event event, String userId) {
        if (event == null || userId == null || userId.isEmpty()) return "REMOVED";
        if (event.isAttending(userId) || event.isWaitlisted(userId)) {
            cancel(event, userId);
            return "REMOVED";
        }
        return register(event, userId) ? "ATTENDING" : "WAITLISTED";
    }
}
