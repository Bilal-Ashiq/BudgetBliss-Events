package com.example.anew.customer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.anew.BiddingActivity;
import com.example.anew.R;
import com.example.anew.utils.NotificationHelper;
import com.example.anew.utils.NotificationChecker;
import com.example.anew.utils.NotificationSender;

public class CustomerHomeActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        // Save FCM token for customer
        NotificationHelper.getAndSaveFCMToken(this, "customer");
        
        // Debug: Check current user and token status
        NotificationHelper.debugCurrentUser(this);
        
        // Check for new notifications on app start
        NotificationChecker.checkForNewNotifications(this);
        
        // Start real-time notification listener
        NotificationChecker.startNotificationListener(this);
        
        // Send a test notification after 5 seconds for debugging
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                NotificationSender.sendImmediateNotification(CustomerHomeActivity.this, 
                    "Test Notification", 
                    "This is a test notification for customer!", 
                    "test");
                Log.d("TEST", "Test notification sent to customer");
            }
        }, 5000);
        
        View plan = findViewById(R.id.planevent);
        View view = findViewById(R.id.viewevent);
        View findtheoffer = findViewById(R.id.findtheoffer);

        plan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerHomeActivity.this, AddEventActivity.class);
                intent.putExtra("category", "events");
                startActivity(intent);
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerHomeActivity.this, AllEventsActivity.class);
                startActivity(intent);
            }
        });

        findtheoffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerHomeActivity.this, BiddingActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the notification listener when activity is destroyed
        NotificationChecker.stopNotificationListener();
    }
}