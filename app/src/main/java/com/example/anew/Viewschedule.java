package com.example.anew;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Viewschedule extends AppCompatActivity {

    private TextView eventNameTextView, eventDateTextView, eventTimeTextView, eventLocationTextView, eventDescriptionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewschedule);

        // Initialize views
        eventNameTextView = findViewById(R.id.view_event_name);
        eventDateTextView = findViewById(R.id.view_event_date);
        eventTimeTextView = findViewById(R.id.view_event_time);
        eventLocationTextView = findViewById(R.id.view_event_location);
        eventDescriptionTextView = findViewById(R.id.view_event_description);
        Button backButton = findViewById(R.id.back_button);

        // Retrieve schedule data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("EventSchedulePrefs", MODE_PRIVATE);
        String eventName = sharedPreferences.getString("event_name", "No Event Name Saved");
        String eventDate = sharedPreferences.getString("event_date", "No Event Date Saved");
        String eventTime = sharedPreferences.getString("event_time", "No Event Time Saved");
        String eventLocation = sharedPreferences.getString("event_location", "No Event Location Saved");
        String eventDescription = sharedPreferences.getString("event_description", "No Event Description Saved");

        // Display the data
        eventNameTextView.setText("Event Name: " + eventName);
        eventDateTextView.setText("Event Date: " + eventDate);
        eventTimeTextView.setText("Event Time: " + eventTime);
        eventLocationTextView.setText("Event Location: " + eventLocation);
        eventDescriptionTextView.setText("Event Description: " + eventDescription);

        // Handle back button click
        backButton.setOnClickListener(v -> {
            Toast.makeText(Viewschedule.this, "Returning to previous screen", Toast.LENGTH_SHORT).show();
            finish(); // Close this activity and return to the previous screen
        });
    }
}
