package com.example.anew.customer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.anew.R;
import com.example.anew.customer.models.Bid;
import com.example.anew.customer.models.Event;
import com.example.anew.utils.BidAdapter;
import com.example.anew.utils.EventAdapter;
import com.example.anew.utils.LoadingDialog;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewEventActivity extends AppCompatActivity {

    private Event event;

    private TextView eventName, eventLocation, eventDate, eventBudget;

    private ImageView eventImage;
    private ArrayList<Bid> bidsList;
    private BidAdapter bidAdapter;

    private RecyclerView recyclerView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
    }

    @Override
    protected void onResume() {
        super.onResume();

        event = (Event) getIntent().getSerializableExtra("selectedEvent");

        eventName = findViewById(R.id.heading);
        eventLocation = findViewById(R.id.event_location);
        eventDate = findViewById(R.id.event_date);
        eventBudget = findViewById(R.id.event_budget);

        recyclerView = findViewById(R.id.recyclerViewBids);

        eventImage = findViewById(R.id.eventImage);

        eventName.setText(event.getEventName());
        eventLocation.setText(event.getEventLocation());
        eventDate.setText(event.getEventDate());
        eventBudget.setText("Rs: " + event.getBudget());

        Glide.with(this)
                .load(event.getEventPicUrl())
                .placeholder(R.drawable.baseline_image_24)
                .into(eventImage);

        bidsList = new ArrayList<>();

        bidAdapter = new BidAdapter(this, bidsList, bid -> {
            Intent intent = new Intent(ViewEventActivity.this, ViewBidActivity.class);
            intent.putExtra("selectedEventId", event.getEventId());
            intent.putExtra("selectedBid", bid);
            startActivity(intent);
            finish();
        });

        recyclerView.setAdapter(bidAdapter);

        if (event.bids != null) {
            LoadingDialog.show(ViewEventActivity.this);

            List<Bid> tempBidsList = new ArrayList<>();
            AtomicInteger completed = new AtomicInteger(0);

            for (Bid bid : event.bids) {
                getVendorRating(bid, ratings -> {
                    if (!ratings.isEmpty()) {
                        float avg = 0.0f;
                        for (float r : ratings) {
                            avg += r;
                        }
                        avg = avg / ratings.size();
                        tempBidsList.add(new Bid(bid.getBidId(), bid.getBidderName(), bid.getBidAmount(), avg));
                    }

                    // Check if all async calls are done
                    if (completed.incrementAndGet() == event.bids.size()) {
                        // Sort in descending order of rating
                        tempBidsList.sort((b1, b2) -> Float.compare(b2.getUserRating(), b1.getUserRating()));

                        // Update main list and UI
                        bidsList.clear();
                        bidsList.addAll(tempBidsList);
                        bidAdapter.notifyDataSetChanged();

                        LoadingDialog.hide();
                    }
                });
            }
        }

    }

    private void getVendorRating(Bid bid, VendorRatingCallback callback) {
        String vendorId = bid.getBidId();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference vendorRef = db.collection("vendors").document(vendorId);

        vendorRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<Long> currentRatings = (List<Long>) documentSnapshot.get("rating");
                if (currentRatings == null) {
                    currentRatings = new ArrayList<>();
                }
                callback.onRatingFetched(currentRatings);
            } else {
                Log.d("FIRESTORE", "Vendor not found");
                callback.onRatingFetched(new ArrayList<>()); // Return empty list
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(ViewEventActivity.this, "Failed to fetch vendor", Toast.LENGTH_SHORT).show();
            callback.onRatingFetched(new ArrayList<>()); // Return empty list on failure
        });
    }

}
