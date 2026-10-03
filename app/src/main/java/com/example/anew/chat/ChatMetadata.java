package com.example.anew.chat;

import java.util.List;

public class ChatMetadata {
    public List<String> participants;
    public long lastTimestamp;
    public String chatId;
    public String lastMessage;

    // not stored in Firestore
    public String otherUserId;
    public String otherUsername;

    public ChatMetadata() {
        // Required empty public constructor
        // This constructor is required for Firestore deserialization
    }

    public ChatMetadata(List<String> participants, long lastTimestamp, String chatId, String otherUserId, String otherUsername, String lastMessage) {
        // Required empty public constructor
        // This constructor is required for Firestore deserialization
        this.participants = participants;
        this.lastTimestamp = lastTimestamp;
        this.chatId = chatId;
        this.otherUserId = otherUserId;
        this.otherUsername = otherUsername;
        this.lastMessage = lastMessage;
    }
}

