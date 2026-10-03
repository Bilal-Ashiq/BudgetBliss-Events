package com.example.anew.vendor.home.ui.vendor_events;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.example.anew.ChooseActivity;
import com.example.anew.R;
import com.example.anew.customer.models.Event;
import com.example.anew.databinding.FragmentVendorEventsBinding;
import com.example.anew.utils.EventAdapter2;
import com.example.anew.utils.LoadingDialog;
import com.example.anew.vendor.VendorViewEventActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class VendorEventsFragment extends Fragment {

    private EventAdapter2 eventAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Event> eventList;
    private FragmentVendorEventsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentVendorEventsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerViewEvents.setLayoutManager(new GridLayoutManager(getContext(), 2));

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter2(getActivity(), eventList, event -> {
            startActivity(new Intent(getActivity(), VendorViewEventActivity.class).putExtra("event", event));
        });
        binding.recyclerViewEvents.setAdapter(eventAdapter);
        
        // Set up overflow menu
        binding.overflowMenu.setOnClickListener(v -> {
            showOverflowMenu(v);
        });
        
        loadEvents();
    }

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

    private void loadEvents() {
        LoadingDialog.show(getActivity());

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
                    Toast.makeText(getActivity(), "Failed to load events: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}