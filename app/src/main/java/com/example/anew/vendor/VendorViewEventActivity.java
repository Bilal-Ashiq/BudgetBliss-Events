package com.example.anew.vendor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.anew.R;
import com.example.anew.customer.ViewBidActivity;
import com.example.anew.customer.models.Bid;
import com.example.anew.customer.models.Event;
import com.example.anew.utils.DataLoader;
import com.example.anew.utils.LoadingDialog;
import com.example.anew.utils.NotificationHelper;
import com.example.anew.utils.NotificationSender;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VendorViewEventActivity extends AppCompatActivity {

    private Event event;

    private TextView eventName, eventLocation, eventDate, eventBudget, eventDescription;

    private EditText etBid;

    private ImageView eventImage;

    private float rating = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendorviewevent);

        event = (Event) getIntent().getSerializableExtra("event");

        eventName = findViewById(R.id.event_name);
        eventLocation = findViewById(R.id.event_location);
        eventDate = findViewById(R.id.event_date);
        eventBudget = findViewById(R.id.event_budget);
        eventDescription = findViewById(R.id.description);
        eventImage = findViewById(R.id.eventImage);

        etBid = findViewById(R.id.etBid);

        eventName.setText(event.getEventName());
        eventLocation.setText(event.getEventLocation());
        eventDate.setText(event.getEventDate());
        eventBudget.setText("Rs: " + event.getBudget());
        eventDescription.setText(event.getEventDescription());

        getVendorAverageRating(FirebaseAuth.getInstance().getCurrentUser().getUid());

        Glide.with(this)
                .load(event.getEventPicUrl())
                .placeholder(R.drawable.baseline_image_24)
                .into(eventImage);

        Button bidButton = findViewById(R.id.placeBid);
        bidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String bidAmountStr = etBid.getText().toString().trim();

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                LoadingDialog.show(VendorViewEventActivity.this);
                String dataPath = "events"; // Your collection name
                Bid newBid = new Bid(FirebaseAuth.getInstance().getCurrentUser().getUid(), DataLoader.userName, bidAmountStr, rating);

                db.collection(dataPath)
                        .whereEqualTo("eventId", event.eventId)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                DocumentReference eventRef = documentSnapshot.getReference();

                                // Debug: Log the raw document data
                                Log.d("FIRESTORE_DEBUG", "Raw document data: " + documentSnapshot.getData());
                                Log.d("FIRESTORE_DEBUG", "Document ID: " + documentSnapshot.getId());

                                // Step 2: Convert to Event object (optional)
                                Event eventFromFirestore = documentSnapshot.toObject(Event.class);

                                // Debug: Log the Event object data
                                if (eventFromFirestore != null) {
                                    Log.d("FIRESTORE_DEBUG", "Event User ID: " + eventFromFirestore.getUserId());
                                    Log.d("FIRESTORE_DEBUG", "Event Name: " + eventFromFirestore.getEventName());
                                } else {
                                    Log.e("FIRESTORE_DEBUG", "Failed to convert document to Event object");
                                }

                                List<Bid> bids = eventFromFirestore.getBids();
                                if (bids == null) {
                                    bids = new ArrayList<>();
                                }

                                // Step 3: Add the new bid
                                bids.add(newBid);

                                // Step 4: Update the bids in Firestore
                                eventRef.update("bids", bids)
                                        .addOnSuccessListener(aVoid -> {
                                            LoadingDialog.hide();
                                            Toast.makeText(getApplicationContext(), "Bid added successfully", Toast.LENGTH_SHORT).show();
                                            
                                            // Send notification to customer
                                            sendBidNotificationToCustomer(eventFromFirestore);
                                        })
                                        .addOnFailureListener(e -> {
                                            LoadingDialog.hide();
                                            Toast.makeText(getApplicationContext(), "Failed to add bid", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                LoadingDialog.hide();
                                Toast.makeText(getApplicationContext(), "Event not found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            LoadingDialog.hide();
                            Toast.makeText(getApplicationContext(), "Error finding event", Toast.LENGTH_SHORT).show();
                        });

            }
        });
    }

    private void getVendorAverageRating(String vendorId) {
        LoadingDialog.show(VendorViewEventActivity.this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("vendors")
                .document(vendorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    LoadingDialog.hide();
                    if (documentSnapshot.exists()) {
                        List<Long> ratingLongs = (List<Long>) documentSnapshot.get("rating");
                        if (ratingLongs != null && !ratingLongs.isEmpty()) {
                            float sum = 0;
                            for (Long r : ratingLongs) {
                                sum += r;
                            }
                            float average = sum / ratingLongs.size();
                            rating = average;
                        } else {
                           rating = 0.0f;
                        }
                    } else {
                        rating = 0.0f;
                    }
                })
                .addOnFailureListener(e -> {
                    LoadingDialog.hide();
                    Log.e("FIRESTORE", "Error fetching vendor rating", e);
                    rating = 0.0f;
                });
    }

    private void sendBidNotificationToCustomer(Event event) {
        String customerUserId = event.getUserId();
        String vendorName = DataLoader.userName;
        String eventName = event.getEventName();
        String bidAmount = etBid.getText().toString().trim();
        
        // Add debugging logs
        Log.d("NOTIFICATION_DEBUG", "Customer User ID: " + customerUserId);
        Log.d("NOTIFICATION_DEBUG", "Vendor Name: " + vendorName);
        Log.d("NOTIFICATION_DEBUG", "Event Name: " + eventName);
        Log.d("NOTIFICATION_DEBUG", "Bid Amount: " + bidAmount);
        Log.d("NOTIFICATION_DEBUG", "Current User ID: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
        
        // Check if customerUserId is null or empty
        if (customerUserId == null || customerUserId.isEmpty()) {
            Log.e("NOTIFICATION_DEBUG", "Customer User ID is null or empty! Trying to get it from Firestore...");
            
            // Try to get the userId directly from Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("events")
                    .whereEqualTo("eventId", event.getEventId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            String userIdFromFirestore = doc.getString("userId");
                            Log.d("NOTIFICATION_DEBUG", "User ID from Firestore: " + userIdFromFirestore);
                            
                            if (userIdFromFirestore != null && !userIdFromFirestore.isEmpty()) {
                                sendNotificationWithUserId(userIdFromFirestore, vendorName, eventName, bidAmount);
                            } else {
                                Log.e("NOTIFICATION_DEBUG", "User ID is still null from Firestore!");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("NOTIFICATION_DEBUG", "Error getting user ID from Firestore", e);
                    });
            return;
        }
        
        sendNotificationWithUserId(customerUserId, vendorName, eventName, bidAmount);
    }
    
    private void sendNotificationWithUserId(String customerUserId, String vendorName, String eventName, String bidAmount) {
        String title = "New Bid Received!";
        String message = vendorName + " has placed a bid of Rs. " + bidAmount + " for your event: " + eventName;
        
        // Check if the target user is the current user (for testing purposes)
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null && auth.getCurrentUser().getUid().equals(customerUserId)) {
            Log.d("NOTIFICATION_DEBUG", "Target user is current user, sending immediate notification");
            // Send immediate notification for testing
            NotificationSender.sendImmediateNotification(VendorViewEventActivity.this, title, message, "bid_placed");
        }
        
        // Send notification to the customer (not the current vendor)
        NotificationSender.sendNotificationToUser(customerUserId, title, message, "bid_placed");
        
        // Also try to send through FCM (for future use)
        NotificationHelper.sendNotificationToUser(customerUserId, title, message, "bid_placed");
        
        Log.d("NOTIFICATION", "Bid notification sent to customer: " + customerUserId);
    }
}