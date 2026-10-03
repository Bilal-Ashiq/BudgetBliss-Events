package com.example.anew.customer.customerhome.ui.add_new_event;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.anew.R;
import com.example.anew.customer.AddEventActivity;
import com.example.anew.customer.models.Event;
import com.example.anew.databinding.FragmentAddNewEventBinding;
import com.example.anew.utils.EventAdapter2;
import com.example.anew.utils.LoadingDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddNewEventFragment extends Fragment implements OnMapReadyCallback {

    private FragmentAddNewEventBinding binding;
    private Uri selectedImageUri;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng selectedLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private FirebaseAuth mAuth;
    private String dataPath = "events";

    private ActivityResultLauncher<Intent> galleryLauncher;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAddNewEventBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up location search
        binding.etLocationSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    searchLocation(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Set up location selection button
        binding.btnSelectLocation.setOnClickListener(v -> {
            if (selectedLocation != null) {
                getAddressFromLatLng(selectedLocation);
            } else {
                Toast.makeText(getActivity(), "Please select a location on the map", Toast.LENGTH_SHORT).show();
            }
        });

// InputFilter that blocks everything except a–z and A–Z
        InputFilter letterOnlyFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    char currentChar = source.charAt(i);
                    if (!Character.isLetter(currentChar) && !Character.isSpaceChar(currentChar)) {
                        return ""; // Block input
                    }
                }
                return null; // Accept valid letters
            }
        };

        InputFilter letterAndIntegerFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    char currentChar = source.charAt(i);
                    if (!Character.isLetter(currentChar) && !Character.isSpaceChar(currentChar) && !Character.isDigit(currentChar)) {
                        return ""; // Block input
                    }
                }
                return null; // Accept valid letters
            }
        };

        InputFilter letterAndCommaFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    char currentChar = source.charAt(i);
                    if (!Character.isLetter(currentChar) && !Character.isSpaceChar(currentChar) && !String.valueOf(currentChar).equals(",")) {
                        return ""; // Block input
                    }
                }
                return null; // Accept valid letters
            }
        };

        binding.etDescription.setFilters(new InputFilter[]{letterAndIntegerFilter});

        mAuth = FirebaseAuth.getInstance();

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        binding.selectedImage.setImageURI(selectedImageUri);
                    }
                }
        );

        // List of event names
        String[] eventNames = {
            "Wedding",
            "Birthday Party",
            "Corporate Event",
            "Conference",
            "Concert",
            "Festival",
            "Exhibition",
            "Product Launch",
            "Workshop",
            "Seminar",
            "Award Ceremony",
            "Charity Event",
            "Fashion Show",
            "Networking Event",
            "Cultural Event",
            "Engagement Party",
            "Sports Event",
            "Farewell Party"
        };

        // Set up the event name adapter
        ArrayAdapter<String> eventNameAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                eventNames
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.parseColor("#ffffff")); // Change color here
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE); // Text color
                textView.setBackgroundColor(Color.parseColor("#6A6AFF")); // Dark background
                return view;
            }
        };

        eventNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerEventName.setAdapter(eventNameAdapter);

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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                eventTypes
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.parseColor("#ffffff")); // Change color here
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE); // Text color
                textView.setBackgroundColor(Color.parseColor("#6A6AFF")); // Dark background
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerEventType.setAdapter(adapter);

        // List of services
        String[] services = {
                "Catering",
                "Audio-Visual Setup",
                "Decoration",
                "Entertainment",
                "Photography",
                "Videography",
                "Transportation",
                "Security",
                "Stage Design",
                "Lighting",
                "Invitation Handling"
        };

        // Set up the services adapter
        ArrayAdapter<String> servicesAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                services
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.parseColor("#ffffff")); // Change color here
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE); // Text color
                textView.setBackgroundColor(Color.parseColor("#6A6AFF")); // Dark background
                return view;
            }
        };

        servicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerServices.setAdapter(servicesAdapter);

        // Set click listener for the select photo button
        binding.btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // Disable editing of the date EditText
        binding.date.setFocusable(false);

        // Handle calendar button click
        binding.calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Handle date EditText click
        binding.date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Handle save button click
        binding.btnSaveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.spinnerEventName.getSelectedItem().toString().trim();
                String date = binding.date.getText().toString().trim();
                String location = binding.tvSelectedLocation.getText().toString().trim();
                String providedServices = binding.spinnerServices.getSelectedItem().toString().trim();
                String eventDescription = binding.etDescription.getText().toString().trim();
                String eventBudget = binding.etBudget.getText().toString().trim();
                String selectedType = binding.spinnerEventType.getSelectedItem().toString();

                // Validate input fields
                if (TextUtils.isEmpty(name)
                        || TextUtils.isEmpty(date)
                        || TextUtils.isEmpty(location)
                        || TextUtils.isEmpty(providedServices)
                        || TextUtils.isEmpty(eventDescription)
                        || TextUtils.isEmpty(eventBudget)
                ) {
                    Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else if (selectedImageUri == null) {
                    Toast.makeText(getActivity(), "Please select an image", Toast.LENGTH_SHORT).show();
                } else if(Integer.parseInt(eventBudget) < 1000) {
                    Toast.makeText(getActivity(), "Budget should be greater than 1000", Toast.LENGTH_SHORT).show();
                } else {
                    uploadImageToFirebaseStorage(selectedImageUri, Calendar.getInstance().getTimeInMillis()+".jpg", imageUrl -> {
                        createEvent(name, location, date, selectedType, providedServices, eventDescription, eventBudget, imageUrl);
                    });
                }
            }
        });

    }

    private void searchLocation(String query) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng location = new LatLng(address.getLatitude(), address.getLongitude());
                
                // Move camera to searched location
                if (mMap != null) {
                    mMap.clear();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                    mMap.addMarker(new MarkerOptions().position(location).title("Searched Location"));
                    selectedLocation = location;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // Check for location permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            requestLocationPermission();
        }

        // Set up map click listener
        mMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
        });

        // Set up map UI settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                            // Set initial marker at current location
                            selectedLocation = currentLocation;
                            mMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("Current Location"));
                            // Get and display the address
                            getAddressFromLatLng(currentLocation);
                        } else {
                            Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressText = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressText.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        addressText.append(", ");
                    }
                }
                binding.tvSelectedLocation.setText(addressText.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error getting address", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    try {
                        mMap.setMyLocationEnabled(true);
                        // Clear any existing markers
                        mMap.clear();
                        // Get and set current location
                        getCurrentLocation();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error enabling location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Location permission is required to show your current location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDatePicker() {
        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (view, selectedYear, selectedMonth, selectedDay) -> {
            // Set selected date in EditText
            String formattedDate = selectedYear + "-" + String.format("%02d", (selectedMonth + 1)) + "-" + String.format("%02d", selectedDay);
            binding.date.setText(formattedDate);
        }, year, month, day);

        // Disable past dates
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }

    public void uploadImageToFirebaseStorage(Uri imageUri, String fileName, OnSuccessListener<String> listener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("event_pics/" + fileName);
        LoadingDialog.show(getActivity());
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        listener.onSuccess(uri.toString()); // Get image URL
                    });
                })
                .addOnFailureListener(e -> {
                    LoadingDialog.hide();
                    Toast.makeText(getActivity(), "Image upload failed", Toast.LENGTH_SHORT).show();
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

                    binding.spinnerEventName.setSelection(0);
                    binding.tvSelectedLocation.setText("");
                    binding.etLocationSearch.setText("");
                    binding.date.setText("");
                    binding.spinnerEventType.setSelection(0);
                    binding.spinnerServices.setSelection(0);
                    binding.etDescription.setText("");
                    binding.etBudget.setText("");
                    selectedImageUri = null;
                    selectedLocation = null;
                    if (mMap != null) {
                        mMap.clear();
                    }

                    BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.nav_view2);
                    bottomNavigationView.setSelectedItemId(R.id.navigation_all_events);
                    Toast.makeText(getActivity(), "Event created successfully", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> {
                    LoadingDialog.hide();
                    Toast.makeText(getActivity(), "Failed to create event", Toast.LENGTH_SHORT).show();
                });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}