package com.example.anew.customer.models;

public class Message {
    private String senderId;
    private String receiverId;
    private String message;
    private long timestamp;

    private String senderUsername;

    public Message() {} // Needed for Firestore

    public Message(String senderId, String receiverId, String message, long timestamp, String senderUsername) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.senderUsername = senderUsername;
    }

    // Getters and setters...

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderId() {
        return senderId;
    }

    private void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    private String getReceiverId() {
        return receiverId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }
}
