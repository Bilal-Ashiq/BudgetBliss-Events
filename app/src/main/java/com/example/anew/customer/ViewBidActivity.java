package com.example.anew.customer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.anew.R;
import com.example.anew.chat.ChatActivity;
import com.example.anew.customer.models.Bid;
import com.example.anew.utils.LoadingDialog;
import com.example.anew.utils.NotificationHelper;
import com.example.anew.utils.NotificationSender;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewBidActivity extends AppCompatActivity {

    TextView etProfileTitle, etServices, offerAmount, ratingCount;

    ImageView profileImage, image1, image2, image3;

    private Button acceptBid, rejectBid, chat;

    private FirebaseFirestore db;

    private Bid selectedBid;

    private String selectedEventId;

    RatingBar ratingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_bid);

        selectedBid = (Bid) getIntent().getSerializableExtra("selectedBid");
        selectedEventId = getIntent().getStringExtra("selectedEventId");

        etProfileTitle = findViewById(R.id.etProfileTitle);
        etServices = findViewById(R.id.etServices);
        profileImage = findViewById(R.id.ivProfile);
        acceptBid = findViewById(R.id.acceptBid);
        rejectBid = findViewById(R.id.rejectBid);
        chat = findViewById(R.id.chat);
        offerAmount = findViewById(R.id.offerAmount);

        ratingCount = findViewById(R.id.rating_count);
        ratingBar = findViewById(R.id.materialRatingBar);

        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);

        db = FirebaseFirestore.getInstance();

        etProfileTitle.setText(selectedBid.getBidderName());
        etServices.setText(selectedBid.getBidAmount());
        offerAmount.setText(selectedBid.getBidAmount());

        getVendorData(selectedBid);

        acceptBid.setOnClickListener(v -> {
            // Handle accept bid button click
            updateVendorRating(selectedBid, (int) ratingBar.getRating());
            
            // Send notification to vendor
            sendBidAcceptanceNotification(selectedBid);
        });

        rejectBid.setOnClickListener(v -> {
            // Handle reject bid button click
            deleteBid(selectedEventId, selectedBid.getBidId(), ViewBidActivity.this);
        });

        chat.setOnClickListener(v -> {
            startActivity(
                    new Intent(ViewBidActivity.this, ChatActivity.class)
                            .putExtra("receiverId", selectedBid.getBidId())
            );
        });

    }

    private void getVendorData(Bid bid) {
        LoadingDialog.show(ViewBidActivity.this);
        db.collection("vendors")
                .document(bid.getBidId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    LoadingDialog.hide();
                    if (documentSnapshot.exists()) {
                        Map<String, Object> vendorData = documentSnapshot.getData();
                        if (vendorData != null) {
                            String name = vendorData.get("userName").toString();
                            String services = vendorData.get("services").toString();
                            List<String> workImages = (List<String>) vendorData.get("workImages");

                            // Update UI with vendor data
                            etProfileTitle.setText(name);
                            etServices.setText(services);

                            Glide.with(this)
                                    .load(workImages.get(0))
                                    .placeholder(R.drawable.baseline_image_24)
                                    .into(profileImage);

                            Glide.with(this)
                                    .load(workImages.get(1))
                                    .placeholder(R.drawable.baseline_image_24)
                                    .into(image1);

                            Glide.with(this)
                                    .load(workImages.get(2))
                                    .placeholder(R.drawable.baseline_image_24)
                                    .into(image2);

                            Glide.with(this)
                                    .load(workImages.get(3))
                                    .placeholder(R.drawable.baseline_image_24)
                                    .into(image3);

                            // Use other fields as needed
                            Log.d("FIRESTORE", "Vendor name: " + name);
                        } else {
                            Log.d("FIRESTORE", "Vendor not found");
                        }
                    }
                }).addOnFailureListener(e -> {
                    LoadingDialog.hide();
                    Log.e("FIRESTORE", "Error fetching vendor", e);
                });
    }

    private void updateVendorRating(Bid bid, int newRating) {
        String vendorId = bid.getBidId();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference vendorRef = db.collection("vendors").document(vendorId);

        LoadingDialog.show(ViewBidActivity.this);
        vendorRef.get().addOnSuccessListener(documentSnapshot -> {
            LoadingDialog.hide();
            if (documentSnapshot.exists()) {
                List<Long> currentRatings = (List<Long>) documentSnapshot.get("rating");

                if (currentRatings == null) {
                    currentRatings = new ArrayList<>();
                }

                currentRatings.add((long) newRating);  // Firestore stores integers as Longs

                vendorRef.update("rating", currentRatings)
                        .addOnSuccessListener(unused -> Log.d("FIRESTORE", "Rating updated successfully"))
                        .addOnFailureListener(e -> Log.e("FIRESTORE", "Failed to update rating", e));

            } else {
                Log.d("FIRESTORE", "Vendor not found");
            }

            finish();
        }).addOnFailureListener(e -> {
            LoadingDialog.hide();
            Toast.makeText(ViewBidActivity.this, "Failed to fetch vendor", Toast.LENGTH_SHORT).show();
        });
    }

    void deleteBid(String eventId, String bidId, Context context) {
        LoadingDialog.show(context);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query where eventId is a field, not the document ID
        db.collection("events")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            DocumentReference eventRef = doc.getReference();
                            List<Map<String, Object>> bids = (List<Map<String, Object>>) doc.get("bids");

                            if (bids != null) {
                                // Filter out the bid with the matching bidId
                                List<Map<String, Object>> updatedBids = new ArrayList<>();
                                for (Map<String, Object> bid : bids) {
                                    if (!bidId.equals(bid.get("bidId"))) {
                                        updatedBids.add(bid);
                                    }
                                }

                                // Update the document
                                eventRef.update("bids", updatedBids)
                                        .addOnSuccessListener(aVoid -> {
                                            LoadingDialog.hide();
                                            Toast.makeText(context, "Bid Rejected", Toast.LENGTH_SHORT).show();
                                            ((Activity) context).finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            LoadingDialog.hide();
                                            Toast.makeText(context, "Failed to remove bid: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            } else {
                                LoadingDialog.hide();
                                Toast.makeText(context, "No bids to update", Toast.LENGTH_SHORT).show();
                            }
                            break; // Assuming eventId is unique, handle only the first match
                        }
                    } else {
                        LoadingDialog.hide();
                        Toast.makeText(context, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    LoadingDialog.hide();
                    Toast.makeText(context, "Error finding event: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


/*    void deleteBid(String eventId, String bidId, Context context) {
        LoadingDialog.show(context);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventId);
        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> bids = (List<Map<String, Object>>) documentSnapshot.get("bids");

                if (bids != null) {
                    // Filter out the bid with the matching bidId
                    List<Map<String, Object>> updatedBids = new ArrayList<>();
                    for (Map<String, Object> bid : bids) {
                        if (!bidId.equals(bid.get("bidId"))) {
                            updatedBids.add(bid);
                        }
                    }

                    // Update the document
                    eventRef.update("bids", updatedBids)
                            .addOnSuccessListener(aVoid -> {
                                LoadingDialog.hide();
                                Toast.makeText(context, "Bid Rejected", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                LoadingDialog.hide();
                                Toast.makeText(context, "Failed to remove bid: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                } else {
                    LoadingDialog.hide();
                }
            } else {
                LoadingDialog.hide();
            }
        });
    }*/

    private void sendBidAcceptanceNotification(Bid bid) {
        String vendorUserId = bid.getBidId();
        String customerName = "Customer"; // You might want to get the actual customer name
        String bidAmount = bid.getBidAmount();
        
        String title = "Bid Accepted!";
        String message = "Your bid of Rs. " + bidAmount + " has been accepted by " + customerName;
        
        // Send notification to the vendor (not the current customer)
        NotificationSender.sendNotificationToUser(vendorUserId, title, message, "bid_accepted");
        
        // Also try to send through FCM (for future use)
        NotificationHelper.sendNotificationToUser(vendorUserId, title, message, "bid_accepted");
        
        Log.d("NOTIFICATION", "Bid acceptance notification sent to vendor: " + vendorUserId);
    }
}