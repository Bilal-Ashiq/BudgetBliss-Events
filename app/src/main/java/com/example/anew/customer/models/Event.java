package com.example.anew.customer.models;

import java.io.Serializable;
import java.util.List;

public class Event implements Serializable {
    public String eventName;
    public String eventLocation;
    public String eventDate;
    public String eventType;
    public String eventServices;
    public String eventDescription;
    public String budget;
    public String eventPicUrl;
    public String eventId;
    public String userId;

    public int priorityScore;

    public List<Bid> bids;

    public Event() {} // Required for Firestore

    public Event(String eventName, String eventLocation, String eventDate, String eventType,
                 String eventServices, String eventDescription, String budget, String eventPicUrl, String eventId, String userId, List<Bid> bids) {
        this.eventName = eventName;
        this.eventLocation = eventLocation;
        this.eventDate = eventDate;
        this.eventType = eventType;
        this.eventServices = eventServices;
        this.eventDescription = eventDescription;
        this.budget = budget;
        this.eventPicUrl = eventPicUrl;
        this.eventId = eventId;
        this.userId = userId;
        this.bids = bids;
    }

    // Getters and setters
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventId(){
        return eventId;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getBudget() {
        return budget;
    }

    public void setEventPicUrl(String eventPicUrl) {
        this.eventPicUrl = eventPicUrl;
    }

    public String getEventPicUrl() {
        return eventPicUrl;
    }

    public void setEventServices(String eventServices) {
        this.eventServices = eventServices;
    }

    public String getEventServices() {
        return eventServices;
    }

    public String getEventName(){
        return eventName;
    }

    public String getEventLocation(){
        return eventLocation;
    }

    public String getEventDate(){
        return eventDate;
    }

    public String getEventType(){
        return eventType;
    }

    public String getEventDescription(){
        return eventDescription;
    }

    public void setEventName(){
        this.eventName = eventName;
    }

    public void setEventLocation(){
        this.eventLocation = eventLocation;
    }

    public void setEventDate(){
        this.eventDate = eventDate;
    }

    public void setEventType(){
        this.eventType = eventType;
    }

    public void setEventDescription(){
        this.eventDescription = eventDescription;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public List<Bid> getBids() {
        return bids;
    }

    public void setBids(List<Bid> bids) {
        this.bids = bids;
    }

    public int getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(int priorityScore) {
        this.priorityScore = priorityScore;
    }


}
