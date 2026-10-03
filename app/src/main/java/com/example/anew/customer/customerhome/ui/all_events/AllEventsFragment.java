package com.example.anew.customer.customerhome.ui.all_events;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.anew.R;
import com.example.anew.ChooseActivity;
import com.example.anew.customer.AddEventActivity;
import com.example.anew.customer.ViewEventActivity;
import com.example.anew.customer.models.Event;
import com.example.anew.databinding.FragmentAllEventsBinding;
import com.example.anew.utils.EventAdapter2;
import com.example.anew.utils.LoadingDialog;
import com.example.db.PriorityEventDBHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class AllEventsFragment extends Fragment {

    private EventAdapter2 eventAdapter;

    private ArrayList<Event> eventList = new ArrayList<>();

    private FragmentAllEventsBinding binding;

    private PriorityEventDBHelper dbHelper;
    private FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAllEventsBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        dbHelper = new PriorityEventDBHelper(requireContext());

        binding.searchRecycler.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        eventAdapter = new EventAdapter2(getActivity(), eventList, event -> {
            dbHelper.saveEvent(event.eventName, event.eventLocation, event.eventType);
            Intent intent = new Intent(getActivity(), ViewEventActivity.class);
            intent.putExtra("selectedEvent", event);
            startActivity(intent);
        });

        binding.hint2.setOnClickListener(v -> {
            binding.searchEditText.setText("");
        });

        binding.searchRecycler.setAdapter(eventAdapter);

        binding.searchEditText.addTextChangedListener(new TextWatcher() {
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

        binding.addEvent.setOnClickListener(v -> {
            showOverflowMenu(v);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getEventList();
    }

    private void getEventList() {
        LoadingDialog.show(getActivity());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        PriorityEventDBHelper dbHelper = new PriorityEventDBHelper(requireContext());

        // Step 1: Fetch all priority events from SQLite and store in memory
        HashMap<String, Integer> namePriorityMap = new HashMap<>();
        HashMap<String, Integer> locationPriorityMap = new HashMap<>();
        HashMap<String, Integer> typePriorityMap = new HashMap<>();

        Cursor cursor = dbHelper.getEventsSortedByPriority();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("event_name"));
            String location = cursor.getString(cursor.getColumnIndexOrThrow("event_location"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("event_type"));
            int namePriority = cursor.getInt(cursor.getColumnIndexOrThrow("name_priority"));
            int locationPriority = cursor.getInt(cursor.getColumnIndexOrThrow("location_priority"));
            int typePriority = cursor.getInt(cursor.getColumnIndexOrThrow("type_priority"));

            namePriorityMap.put(name, namePriority);
            locationPriorityMap.put(location, locationPriority);
            typePriorityMap.put(type, typePriority);
        }
        cursor.close();

        // Step 2: Fetch Firestore Events
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            int priorityScore = 0;

                            if (event.getEventName() != null && namePriorityMap.containsKey(event.getEventName())) {
                                priorityScore += namePriorityMap.get(event.getEventName());
                            }
                            if (event.getEventLocation() != null && locationPriorityMap.containsKey(event.getEventLocation())) {
                                priorityScore += locationPriorityMap.get(event.getEventLocation());
                            }
                            if (event.getEventType() != null && typePriorityMap.containsKey(event.getEventType())) {
                                priorityScore += typePriorityMap.get(event.getEventType());
                            }

                            event.setPriorityScore(priorityScore); // Custom field to help sort
                            eventList.add(event);
                        }
                    }

                    // Step 3: Sort the list by the calculated priorityScore
                    Collections.sort(eventList, (e1, e2) -> Integer.compare(e2.getPriorityScore(), e1.getPriorityScore()));

                    // Step 4: Notify adapter
                    eventAdapter.notifyDataSetChanged();
                    LoadingDialog.hide();
                })
                .addOnFailureListener(e -> {
                    LoadingDialog.hide();
                    Toast.makeText(getActivity(), "Failed to load events: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


/*    private void getEventList() {
        LoadingDialog.show(getActivity());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
                    Toast.makeText(getActivity(), "Failed to load events: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }*/

    private void showOverflowMenu(View view) {
        PopupMenu popup = new PopupMenu(getActivity(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.overflow_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_logout) {
                    logoutUser();
                return true;
                }
                return false;
            }
        });

        popup.show();
    }

    private void logoutUser() {
        if (mAuth != null) {
            mAuth.signOut();
            Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            
            // Navigate to ChooseActivity
            Intent intent = new Intent(getActivity(), ChooseActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}