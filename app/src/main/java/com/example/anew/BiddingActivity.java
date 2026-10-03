package com.example.anew;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class BiddingActivity extends AppCompatActivity implements OnMapReadyCallback {
    static final int MY_PERMISSIONS_REQUEST_LOCATION = 23;
    private GoogleMap mMap;

    private EditText findAddress;
    private TextView yourOffer, offerAmount;
    private Button btnMinus, btnPlus, btnFindVendor, btnSearchAddress;
    private ImageView imageView;

    private int offerAmountValue = 50000; // Initial offer amount

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bidding);

        // Initialize views
        findAddress = findViewById(R.id.address);
        yourOffer = findViewById(R.id.yourOffer);
        offerAmount = findViewById(R.id.offerAmount);
        btnMinus = findViewById(R.id.Minus);
        btnPlus = findViewById(R.id.plus);
        btnFindVendor = findViewById(R.id.findvendor);
        btnSearchAddress = findViewById(R.id.butt);

        // Set initial offer amount
        updateOfferAmount();
        checkPermission();

        // Button click listeners for offer adjustments
        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseOfferAmount();
            }
        });

        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseOfferAmount();
            }
        });

        btnFindVendor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findVendor();
            }
        });

        // Search address and display it on the map
        btnSearchAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = findAddress.getText().toString().trim();
                if (!address.isEmpty()) {
                    findAddressOnMap(address);
                } else {
                    Toast.makeText(BiddingActivity.this, "Please enter an address!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(this);
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set an initial location
        LatLng initialLocation = new LatLng(33.6844, 73.0479); // Islamabad
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 12));
    }

    private void findAddressOnMap(String address) {
        if (mMap == null) {
            Toast.makeText(this, "Map is not ready yet!", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(address, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address location = addressList.get(0);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.clear(); // Clear existing markers
                mMap.addMarker(new MarkerOptions().position(latLng).title(address));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                Toast.makeText(this, "Location found: " + address, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Address not found!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error finding address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateOfferAmount() {
        offerAmount.setText(String.format("PKR %d", offerAmountValue));
    }

    private void decreaseOfferAmount() {
        if (offerAmountValue > 2000) {
            offerAmountValue -= 1000; // Decrease by 1000
            updateOfferAmount();
        } else {
            Toast.makeText(this, "Minimum offer reached!", Toast.LENGTH_SHORT).show();
        }
    }

    private void increaseOfferAmount() {
        offerAmountValue += 1000; // Increase by 1000
        updateOfferAmount();
    }

    private void findVendor() {
        Toast.makeText(this, "Finding the nearest vendor for PKR " + offerAmountValue, Toast.LENGTH_SHORT).show();
        // Add logic to find vendor (e.g., network call or other action)
    }
}
