package com.example.anew;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.anew.customer.LoginActivity;
import com.example.anew.utils.NotificationHelper;
import com.example.anew.vendor.VendorLoginActivity;

public class ChooseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose);

        // Request notification permission
        NotificationHelper.requestNotificationPermission(this);

        View cst = findViewById(R.id.customer);
         View ven = findViewById(R.id.vendor);

        cst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        ven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChooseActivity.this, VendorLoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
