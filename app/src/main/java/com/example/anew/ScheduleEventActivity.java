package com.example.anew;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class ScheduleEventActivity extends AppCompatActivity {

    private EditText eventNameEditText, eventDateEditText, eventTimeEditText, eventLocationEditText, eventDescriptionEditText;
    private ImageButton calendarButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_event);

        // Initialize views
        eventNameEditText = findViewById(R.id.event_name);
        eventDateEditText = findViewById(R.id.date);
        calendarButton = findViewById(R.id.calendarButton);
        eventTimeEditText = findViewById(R.id.event_time);
        eventLocationEditText = findViewById(R.id.event_location);
        eventDescriptionEditText = findViewById(R.id.event_description);
        Button saveEventButton = findViewById(R.id.save_event_button);

        // Disable editing of the date EditText
        eventDateEditText.setFocusable(false);

        // Handle calendar button click
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Handle date EditText click
        eventDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Set click listener for the save button
        saveEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventName = eventNameEditText.getText().toString().trim();
                String eventDate = eventDateEditText.getText().toString().trim();
                String eventTime = eventTimeEditText.getText().toString().trim();
                String eventLocation = eventLocationEditText.getText().toString().trim();
                String eventDescription = eventDescriptionEditText.getText().toString().trim();

                // Validate input
                if (TextUtils.isEmpty(eventName) || TextUtils.isEmpty(eventDate) ||
                        TextUtils.isEmpty(eventTime) || TextUtils.isEmpty(eventLocation)) {
                    Toast.makeText(ScheduleEventActivity.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Save event details to SharedPreferences
                    saveEventToPreferences(eventName, eventDate, eventTime, eventLocation, eventDescription);

                    // Navigate to ViewScheduleActivity
                    Intent intent = new Intent(ScheduleEventActivity.this, Viewschedule.class);
                    startActivity(intent);

                    // Close the current activity
                    finish();
                }
            }
        });
    }

    // Method to save event details to SharedPreferences
    private void saveEventToPreferences(String name, String date, String time, String location, String description) {
        SharedPreferences sharedPreferences = getSharedPreferences("EventSchedulePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("event_name", name);
        editor.putString("event_date", date);
        editor.putString("event_time", time);
        editor.putString("event_location", location);
        editor.putString("event_description", description);
        editor.apply();
    }

    // Method to show DatePickerDialog
    private void showDatePicker() {
        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            // Set selected date in EditText
            String formattedDate = selectedYear + "-" + String.format("%02d", (selectedMonth + 1)) + "-" + String.format("%02d", selectedDay);
            eventDateEditText.setText(formattedDate);
        }, year, month, day);

        datePickerDialog.show();
    }
}
