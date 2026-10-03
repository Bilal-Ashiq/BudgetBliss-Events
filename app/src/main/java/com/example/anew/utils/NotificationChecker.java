package com.example.anew.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class NotificationChecker {
    private static final String TAG = "NotificationChecker";
    private static ListenerRegistration notificationListener;

    public static void checkForNewNotifications(Context context) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.d(TAG, "No user logged in, skipping notification check");
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Checking for new notifications for user: " + currentUserId);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " unread notifications");
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String title = document.getString("title");
                        String message = document.getString("message");
                        String type = document.getString("type");
                        
                        Log.d(TAG, "Showing notification: " + title + " - " + message);
                        
                        // Show the notification
                        NotificationSender.sendNotification(context, title, message, type);
                        
                        // Mark as read
                        document.getReference().update("read", true)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification marked as read"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error marking notification as read", e));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for notifications", e);
                });
    }

    // Start real-time listener for new notifications
    public static void startNotificationListener(Context context) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.d(TAG, "No user logged in, skipping notification listener");
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Starting real-time notification listener for user: " + currentUserId);

        // Remove existing listener if any
        if (notificationListener != null) {
            notificationListener.remove();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        notificationListener = db.collection("notifications")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("read", false)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening for notifications", error);
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        Log.d(TAG, "Real-time: Found " + value.size() + " new notifications");
                        
                        for (QueryDocumentSnapshot document : value) {
                            String title = document.getString("title");
                            String message = document.getString("message");
                            String type = document.getString("type");
                            
                            Log.d(TAG, "Real-time: Showing notification: " + title + " - " + message);
                            
                            // Show the notification immediately
                            NotificationSender.sendNotification(context, title, message, type);
                            
                            // Mark as read
                            document.getReference().update("read", true)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Real-time: Notification marked as read"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Real-time: Error marking notification as read", e));
                        }
                    }
                });
    }

    // Stop the real-time listener
    public static void stopNotificationListener() {
        if (notificationListener != null) {
            notificationListener.remove();
            notificationListener = null;
            Log.d(TAG, "Notification listener stopped");
        }
    }
} 