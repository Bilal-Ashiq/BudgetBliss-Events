package com.example.anew.vendor.home;

import android.os.Bundle;
import android.util.Log;

import com.example.anew.R;
import com.example.anew.utils.NotificationHelper;
import com.example.anew.utils.NotificationSender;
import com.example.anew.utils.NotificationChecker;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.anew.databinding.ActivityVendorMainBinding;

public class VendorMainActivity extends AppCompatActivity {

    private ActivityVendorMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVendorMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Save FCM token for vendor
        NotificationHelper.getAndSaveFCMToken(this, "vendor");
        
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
                NotificationSender.sendImmediateNotification(VendorMainActivity.this, 
                    "Vendor Test Notification", 
                    "This is a test notification for vendor!", 
                    "test");
                Log.d("TEST", "Test notification sent to vendor");
            }
        }, 5000);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_vendor_main);
        NavigationUI.setupWithNavController(binding.navView, navController);
        
        // Add test notification button (you can add this to your layout)
        // For now, we'll send a test notification after a delay
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Send a test notification after 3 seconds
                NotificationSender.sendNotification(VendorMainActivity.this, 
                    "Vendor Test Notification", 
                    "This is a test notification for vendors!", 
                    "test");
            }
        }, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the notification listener when activity is destroyed
        NotificationChecker.stopNotificationListener();
    }
}