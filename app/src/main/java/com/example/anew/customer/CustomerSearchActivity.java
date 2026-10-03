package com.example.anew.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anew.R;
import com.example.anew.customer.models.Event;
import com.example.anew.utils.EventAdapter2;
import com.example.anew.utils.LoadingDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class CustomerSearchActivity extends AppCompatActivity {

    private EventAdapter2 eventAdapter;

    private EditText searchEditText;

    private TextView seeAll;
    private ArrayList<Event> eventList = new ArrayList<>();

    private ImageView addEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_search);
        searchEditText = findViewById(R.id.searchEditText);
        seeAll = findViewById(R.id.hint2);

        RecyclerView recyclerView = findViewById(R.id.searchRecycler);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        eventAdapter = new EventAdapter2(this, eventList, event -> {
            startActivity(new Intent(CustomerSearchActivity.this, CustomerHomeActivity.class));
        });

        seeAll.setOnClickListener(v -> {
            searchEditText.setText("");
        });

        getEventList();
        recyclerView.setAdapter(eventAdapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                eventAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        addEvent = findViewById(R.id.addEvent);

        addEvent.setOnClickListener(v -> {
            showPopupMenu(v);
        });

    }

    private void getEventList() {
        LoadingDialog.show(CustomerSearchActivity.this);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("popular_events")
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

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(CustomerSearchActivity.this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.popu_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(CustomerSearchActivity.this, AddEventActivity.class).putExtra("category", "popular_events"));
                return true;
            }
        });

        popup.show();
    }
}