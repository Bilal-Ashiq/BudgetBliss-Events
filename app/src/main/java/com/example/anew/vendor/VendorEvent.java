package com.example.anew.vendor;
public class VendorEvent {
    private String eventId, vendorId, eventName, eventType, services, description, imageUrl;
    private int budget;

    public VendorEvent() {} // Empty constructor for Firebase

    public VendorEvent(String eventId, String vendorId, String eventName, String eventType, String services, String description, int budget, String imageUrl) {
        this.eventId = eventId;
        this.vendorId = vendorId;
        this.eventName = eventName;
        this.eventType = eventType;
        this.services = services;
        this.description = description;
        this.budget = budget;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
}

