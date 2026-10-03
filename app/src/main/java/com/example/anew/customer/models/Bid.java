package com.example.anew.customer.models;

import java.io.Serializable;

public class Bid implements Serializable {

    private String bidId;
    private String bidderName;
    private String bidAmount;

    private float userRating;

    public Bid() {}

    public Bid(String bidId, String bidderName, String bidAmount, float userRating) {
        this.bidId = bidId;
        this.bidderName = bidderName;
        this.bidAmount = bidAmount;
        this.userRating = userRating;
    }


    public String getBidderName() {
        return bidderName;
    }

    public String getBidAmount() {
        return bidAmount;
    }

    public void setBidderName(String bidderName) {
        this.bidderName = bidderName;
    }

    public void setBidAmount(String bidAmount) {
        this.bidAmount = bidAmount;
    }

    public String getBidId() {
        return bidId;
    }

    public void setBidId(String bidId) {
        this.bidId = bidId;
    }

    public float getUserRating() {
        return userRating;
    }

    public void setUserRating(float userRating) {
        this.userRating = userRating;
    }
}
