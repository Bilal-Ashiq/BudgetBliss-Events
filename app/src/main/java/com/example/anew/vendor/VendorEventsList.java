package com.example.anew.vendor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anew.R;
import com.example.anew.customer.AllEventsActivity;
import com.example.anew.customer.models.Event;
import com.example.anew.utils.EventAdapter;
import com.example.anew.utils.EventAdapter2;
import com.example.anew.utils.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class VendorEventsList extends AppCompatActivity {

    private EventAdapter2 eventAdapter;
    private FirebaseFirestore db;

    private FirebaseAuth mAuth;
    private List<Event> eventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_events_list);


        RecyclerView recyclerView = findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter2(this, eventList, event -> {
            startActivity(new Intent(VendorEventsList.this, VendorViewEventActivity.class).putExtra("event", event));
        });
        recyclerView.setAdapter(eventAdapter);
        loadEvents();
    }

    private void loadEvents() {

        LoadingDialog.show(VendorEventsList.this);

        Log.d("AllEventsActivity", "Loading events: eventsId: " + mAuth.getCurrentUser().getUid() + "");
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        eventList.add(event);
                    }
                    eventAdapter.notifyDataSetChanged();
                    LoadingDialog.hide();
                })
                .addOnFailureListener(e -> {
                    LoadingDialog.hide();
                    Toast.makeText(this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}