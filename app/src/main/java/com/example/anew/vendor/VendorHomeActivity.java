package com.example.anew.vendor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.anew.R;

public class VendorHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendorhome);
        View vendorview=findViewById(R.id.vendorviewevent);
        View vendorviewschedule= findViewById(R.id.vendorviewschedule);


        vendorview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VendorHomeActivity.this, VendorEventsList.class);
                startActivity(intent);
            }
        });

        vendorviewschedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VendorHomeActivity.this, VendorViewScheduleActivity.class);
                startActivity(intent);
            }
        });

    }
}