package com.example.anew.customer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.anew.R;
import com.example.anew.customer.models.Event;
import com.example.anew.utils.LoadingDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

public class AddEventActivity extends AppCompatActivity {

    private EditText Eventname, Eventlocation, Eventdate, EventServices, EventDescription, EventBudget;
    private ImageButton calendarButton;
    private Uri selectedImageUri;

    private Spinner eventTypeSpinner;

    private ImageView selectedImage;

    private FirebaseAuth mAuth;
    private String dataPath = "events";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addevent);

        // Initialize views
        Eventname = findViewById(R.id.etEventName);
        Eventdate = findViewById(R.id.date);
        Eventlocation = findViewById(R.id.etEventLocation);
        calendarButton = findViewById(R.id.calendarButton);
        Button save = findViewById(R.id.btnSaveEvent);
        Button selectPhoto = findViewById(R.id.btnUploadImage);

        if (getIntent().getStringExtra("category") != null) {
            dataPath = getIntent().getStringExtra("category");
        }

        EventServices = findViewById(R.id.etServices);
        EventDescription = findViewById(R.id.etDescription);
        EventBudget = findViewById(R.id.etBudget);

        eventTypeSpinner = findViewById(R.id.spinnerEventType);

        selectedImage = findViewById(R.id.selectedImage);

        mAuth = FirebaseAuth.getInstance();

        // List of event types
        String[] eventTypes = {
                "Birthday",
                "Wedding",
                "Engagement",
                "Anniversary",
                "Graduation",
                "Retirement Party"
        };

        // Set up the adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                eventTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventTypeSpinner.setAdapter(adapter);

        // Set click listener for the select photo button
        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // Disable editing of the date EditText
        Eventdate.setFocusable(false);

        // Handle calendar button click
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Handle date EditText click
        Eventdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Handle save button click
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = Eventname.getText().toString().trim();
                String date = Eventdate.getText().toString().trim();
                String location = Eventlocation.getText().toString().trim();
                String providedServices = EventServices.getText().toString().trim();
                String eventDescription = EventDescription.getText().toString().trim();
                String eventBudget = EventBudget.getText().toString().trim();
                String selectedType = eventTypeSpinner.getSelectedItem().toString();

                // Validate input fields
                if (TextUtils.isEmpty(name)
                                || TextUtils.isEmpty(date)
                                || TextUtils.isEmpty(location)
                                || TextUtils.isEmpty(providedServices)
                                || TextUtils.isEmpty(eventDescription)
                                || TextUtils.isEmpty(eventBudget)
                ) {
                    Toast.makeText(AddEventActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else if (selectedImageUri == null) {
                    Toast.makeText(AddEventActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                } else {
                    uploadImageToFirebaseStorage(selectedImageUri, Calendar.getInstance().getTimeInMillis()+".jpg", imageUrl -> {
                        createEvent(name, location, date, selectedType, providedServices, eventDescription, eventBudget, imageUrl);
                    });
                }
            }
        });
    }

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
            Eventdate.setText(formattedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    public void uploadImageToFirebaseStorage(Uri imageUri, String fileName, OnSuccessListener<String> listener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("event_pics/" + fileName);
        LoadingDialog.show(AddEventActivity.this);
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        listener.onSuccess(uri.toString()); // Get image URL
                    });
                })
                .addOnFailureListener(e -> {
                    LoadingDialog.hide();
                    Toast.makeText(getApplicationContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                });
    }


    public void createEvent(String eventName, String eventLocation, String eventDate, String eventType,
                            String eventServices, String eventDescription, String budget, String eventPicUrl) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Event event = new Event(eventName, eventLocation, eventDate, eventType, eventServices,
                eventDescription, budget, eventPicUrl, String.valueOf(Calendar.getInstance().getTimeInMillis()), mAuth.getCurrentUser().getUid(), null);

        db.collection(dataPath)
                .add(event)
                .addOnSuccessListener(documentReference -> {
                    LoadingDialog.hide();
                    Toast.makeText(getApplicationContext(), "Event created successfully", Toast.LENGTH_SHORT).show();
                    finish();

                })
                .addOnFailureListener(e -> {
                    LoadingDialog.hide();
                    Toast.makeText(getApplicationContext(), "Failed to create event", Toast.LENGTH_SHORT).show();
                });
    }

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    selectedImage.setImageURI(selectedImageUri);
                }
            });

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }
}
