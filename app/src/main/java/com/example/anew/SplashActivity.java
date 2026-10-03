package com.example.anew;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Navigate to the next activity
                Intent intent = new Intent(SplashActivity.this, ChooseActivity.class);
                startActivity(intent);
                finish(); // Finish the current activity
            }
        }, 2000); // Delay in milliseconds
    }
}
