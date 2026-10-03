package com.example.anew.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.anew.R;
import com.example.anew.customer.customerhome.CustomerHomeActivity;
import com.example.anew.utils.LoadingDialog;
import com.example.anew.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etUserName, etPassword;
    private Button loginButton;
    private TextView signupRedirect;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        etUserName = findViewById(R.id.text_1);
        etPassword = findViewById(R.id.text_2);
        loginButton = findViewById(R.id.loginButton);
        signupRedirect = findViewById(R.id.sign_1);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Handle login button click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Redirect to the signup page
        signupRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
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

        LoadingDialog.show(LoginActivity.this);
        loginButton.setEnabled(false);

        // Step 1: Get user data from Firestore using username or email
        db.collection("users")
                .where(Filter.and(
                        Filter.or(
                                Filter.equalTo("userName", input),
                                Filter.equalTo("email", input)
                        ),
                        Filter.equalTo("type", "CUSTOMER")
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
                                    LoadingDialog.hide();
                                    loginButton.setEnabled(true);
                                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    
                                    // Save FCM token after successful login
                                    NotificationHelper.getAndSaveFCMToken(LoginActivity.this, "customer");
                                    
                                    Intent intent = new Intent(LoginActivity.this, CustomerHomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    LoadingDialog.hide();
                                    loginButton.setEnabled(true);
                                    Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });

                    } else {
                        LoadingDialog.hide();
                        loginButton.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    LoadingDialog.hide();
                    loginButton.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
