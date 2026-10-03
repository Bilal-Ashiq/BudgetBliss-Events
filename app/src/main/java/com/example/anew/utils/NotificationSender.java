package com.example.anew.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.anew.R;
import com.example.anew.customer.AllEventsActivity;
import com.example.anew.vendor.home.VendorMainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationSender {
    private static final String TAG = "NotificationSender";
    private static final String CHANNEL_ID = "event_planner_channel";
    private static final String CHANNEL_NAME = "Event Planner Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for event planner app";

    // Method to send notification to current user (local notification)
    public static void sendNotification(Context context, String title, String message, String notificationType) {
        Log.d(TAG, "Sending local notification: " + title + " - " + message);
        
        // Create notification channel for Android O and above
        createNotificationChannel(context);

        // Determine which activity to open based on notification type
        Intent intent;
        if ("bid_placed".equals(notificationType)) {
            // Open customer activity for bid notifications
            intent = new Intent(context, AllEventsActivity.class);
        } else if ("bid_accepted".equals(notificationType)) {
            // Open vendor activity for bid acceptance notifications
            intent = new Intent(context, VendorMainActivity.class);
        } else {
            // Default to customer activity
            intent = new Intent(context, AllEventsActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // Use a unique notification ID based on timestamp
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, notificationBuilder.build());
            Log.d(TAG, "Local notification sent with ID: " + notificationId);
        } else {
            Log.e(TAG, "NotificationManager is null");
        }
    }

    // Method to send notification to a specific user by their user ID
    public static void sendNotificationToUser(String targetUserId, String title, String message, String notificationType) {
        Log.d(TAG, "Attempting to send notification to user: " + targetUserId);
        
        // Check if the target user is the current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null && auth.getCurrentUser().getUid().equals(targetUserId)) {
            Log.d(TAG, "Target user is current user, sending local notification immediately");
            // Send immediate local notification for current user
            // We need a context, so we'll store it in Firestore and check later
            storeNotificationInFirestore(targetUserId, title, message, notificationType);
            return;
        }
        
        // Store the notification in Firestore for the target user
        storeNotificationInFirestore(targetUserId, title, message, notificationType);
    }

    // Method to send immediate notification to current user (for testing)
    public static void sendImmediateNotification(Context context, String title, String message, String notificationType) {
        Log.d(TAG, "Sending immediate notification: " + title + " - " + message);
        sendNotification(context, title, message, notificationType);
    }

    private static void storeNotificationInFirestore(String targetUserId, String title, String message, String notificationType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        java.util.Map<String, Object> notification = new java.util.HashMap<>();
        notification.put("userId", targetUserId);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("type", notificationType);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);
        
        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Notification stored in Firestore for user: " + targetUserId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error storing notification for user: " + targetUserId, e);
                });
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        }
    }
} 