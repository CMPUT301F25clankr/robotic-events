package com.example.robotic_events_test;
/** image class for display*/
public class ImageItem {
    private String eventId;
    private String imageUrl;

    public ImageItem(String eventId, String imageUrl) {
        this.eventId = eventId;
        this.imageUrl = imageUrl;
    }

    public String getEventId() {
        return eventId;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
