package com.example.anew.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 100;

    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // For older versions, permission is granted by default
    }

    public static void getAndSaveFCMToken(Context context, String userType) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "User not authenticated when trying to save FCM token");
            return;
        }
        
        Log.d(TAG, "Getting FCM token for user: " + auth.getCurrentUser().getUid() + " with type: " + userType);
        
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d(TAG, "FCM Token received: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
                    saveTokenToFirestore(token, userType);
                });
    }

    // Debug method to check current user and manually trigger token saving
    public static void debugCurrentUser(Context context) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Log.d(TAG, "Current user ID: " + auth.getCurrentUser().getUid());
            Log.d(TAG, "Current user email: " + auth.getCurrentUser().getEmail());
            
            // Check if token already exists
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("fcm_tokens").document(auth.getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "Token already exists for current user");
                        } else {
                            Log.d(TAG, "No token found for current user, triggering save...");
                            getAndSaveFCMToken(context, "unknown");
                        }
                    });
        } else {
            Log.w(TAG, "No user currently authenticated");
        }
    }

    private static void saveTokenToFirestore(String token, String userType) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "User not authenticated");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Saving FCM token for user: " + userId + " with type: " + userType);
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("token", token);
        tokenData.put("userType", userType);
        tokenData.put("userId", userId);
        tokenData.put("timestamp", System.currentTimeMillis());

        String collectionName = "fcm_tokens";
        Log.d(TAG, "Saving to collection: " + collectionName + " with document ID: " + userId);
        
        db.collection(collectionName).document(userId)
                .set(tokenData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "FCM token saved successfully for user: " + userId);
                    // Verify the token was saved by reading it back
                    verifyTokenSaved(userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving FCM token for user: " + userId, e);
                });
    }

    private static void verifyTokenSaved(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("fcm_tokens").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String savedToken = documentSnapshot.getString("token");
                        String userType = documentSnapshot.getString("userType");
                        Log.d(TAG, "Token verification successful - User: " + userId + 
                              ", Type: " + userType + ", Token: " + (savedToken != null ? savedToken.substring(0, Math.min(20, savedToken.length())) + "..." : "null"));
                    } else {
                        Log.w(TAG, "Token verification failed - Document does not exist for user: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Token verification failed for user: " + userId, e);
                });
    }

    public static void sendNotificationToUser(String targetUserId, String title, String message, String notificationType) {
        Log.d(TAG, "Attempting to send notification to user: " + targetUserId);
        Log.d(TAG, "Title: " + title + ", Message: " + message + ", Type: " + notificationType);
        
        // For now, send local notification directly instead of trying to get FCM token
        // This will work for testing purposes
        sendLocalNotification(title, message, notificationType);
        
        // Keep the FCM token logic for future use when backend is ready
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Get the target user's FCM token
        db.collection("fcm_tokens").document(targetUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fcmToken = documentSnapshot.getString("token");
                        String userType = documentSnapshot.getString("userType");
                        Log.d(TAG, "Found FCM token for user: " + targetUserId + ", Type: " + userType);
                        
                        if (fcmToken != null) {
                            Log.d(TAG, "Token found, sending notification...");
                            sendNotificationToToken(fcmToken, title, message, notificationType);
                        } else {
                            Log.w(TAG, "FCM token is null for user: " + targetUserId);
                        }
                    } else {
                        Log.w(TAG, "No FCM token found for user: " + targetUserId);
                        // Let's check what documents exist in the collection
                        listAllTokens();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting FCM token for user: " + targetUserId, e);
                });
    }

    private static void listAllTokens() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("fcm_tokens")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Total FCM tokens in collection: " + queryDocumentSnapshots.size());
                    for (var doc : queryDocumentSnapshots) {
                        String userId = doc.getString("userId");
                        String userType = doc.getString("userType");
                        String token = doc.getString("token");
                        Log.d(TAG, "Document ID: " + doc.getId() + 
                              ", User ID: " + userId + 
                              ", Type: " + userType + 
                              ", Token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error listing FCM tokens", e);
                });
    }

    private static void sendLocalNotification(String title, String message, String notificationType) {
        // Get the current context from any activity
        // For now, we'll use a static context or pass it through
        Log.d(TAG, "Sending local notification: " + title + " - " + message);
        
        // This will be called from activities, so we can get context from there
        // For now, just log that we want to send a local notification
    }

    private static void sendNotificationToToken(String fcmToken, String title, String message, String notificationType) {
        // For now, just log the notification details
        // In the future, this will send to backend
        Log.d(TAG, "Would send FCM notification to token: " + fcmToken);
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "Type: " + notificationType);
    }
} 