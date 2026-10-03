package com.example.anew.vendor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.anew.R;
import com.example.anew.utils.DataLoader;
import com.example.anew.utils.LoadingDialog;
import com.example.anew.utils.NotificationHelper;
import com.example.anew.vendor.home.VendorMainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;

public class VendorLoginActivity extends AppCompatActivity {
    private EditText etUserName, etPassword;
    private Button loginButton;
    private TextView signupRedirect;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendorlogin);

        etUserName = findViewById(R.id.vendorloginusername);
        etPassword = findViewById(R.id.vendorloginpassword);
        loginButton = findViewById(R.id.vendorloginbutton);
        signupRedirect = findViewById(R.id.sign_2);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        signupRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VendorLoginActivity.this, VendorSignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String input = etUserName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (input.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        LoadingDialog.show(VendorLoginActivity.this);
        loginButton.setEnabled(false);

        // Step 1: Get email from Firestore using username or email
        db.collection("vendors")
                .where(Filter.and(
                        Filter.or(
                                Filter.equalTo("userName", input),
                                Filter.equalTo("email", input)
                        ),
                        Filter.equalTo("type", "VENDOR")
                ))
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String email = document.getString("email");

                        // Step 2: Sign in with email and password
                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener(authResult -> {
                                    DataLoader.userName = document.getString("userName");
                                    LoadingDialog.hide();
                                    loginButton.setEnabled(true);
                                    Toast.makeText(VendorLoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    
                                    // Save FCM token after successful login
                                    NotificationHelper.getAndSaveFCMToken(VendorLoginActivity.this, "vendor");
                                    
                                    Intent intent = new Intent(VendorLoginActivity.this, VendorMainActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    LoadingDialog.hide();
                                    loginButton.setEnabled(true);
                                    Toast.makeText(VendorLoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });

                    } else {
                        LoadingDialog.hide();
                        loginButton.setEnabled(true);
                        Toast.makeText(VendorLoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    LoadingDialog.hide();
                    loginButton.setEnabled(true);
                    Toast.makeText(VendorLoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
