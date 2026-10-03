package com.example.anew.customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.anew.R;
import com.example.anew.utils.LoadingDialog;
import com.example.anew.utils.UserType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    private EditText username, email, password;
    private Button signupButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView loginRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        username = findViewById(R.id.text11);
        email = findViewById(R.id.text22);
        password = findViewById(R.id.text33);
        signupButton = findViewById(R.id.signupButton);
        loginRedirect = findViewById(R.id.login_12);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginRedirect.setOnClickListener(v -> {
            finish();
        });

        // Set up button click listener
        signupButton.setOnClickListener(v -> {
            // Get entered data
            String enteredUsername = username.getText().toString().trim();
            String enteredEmail = email.getText().toString().trim();
            String enteredPassword = password.getText().toString().trim();

            // Validate username
            if (enteredUsername.isEmpty()) {
                username.setError("Username is required");
            } else if (!enteredUsername.matches("^[a-zA-Z ]{3,}$")) {
                username.setError("Username must be at least 3 letters and contain only letters and spaces");
            }

            // Validate email
            else if (enteredEmail.isEmpty()) {
                email.setError("Email is required");
            } else if (!Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches()) {
                email.setError("Enter a valid email");
            }

            // Validate password
            else if (enteredPassword.isEmpty()) {
                password.setError("Password is required");
            } else if (!isPasswordStrong(enteredPassword)) {
                Toast.makeText(SignupActivity.this, "Password must be at least 8 characters and include upper, lower, and a digit", Toast.LENGTH_SHORT).show();
            } else if (!isPasswordCharLimitValid(enteredPassword)) {
                Toast.makeText(SignupActivity.this, "No character should appear more than twice", Toast.LENGTH_SHORT).show();
            }
            else {
                // Insert into database
                LoadingDialog.show(this);
                mAuth.createUserWithEmailAndPassword(enteredEmail, enteredPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String uid = mAuth.getCurrentUser().getUid();
                                saveUserType(uid, UserType.CUSTOMER.name(), enteredUsername, enteredEmail);
                            } else {
                                LoadingDialog.hide();
                                Toast.makeText(SignupActivity.this, "Signup failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    // Password must be at least 8 characters, have upper, lower, digit
    public boolean isPasswordStrong(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*");
    }

    // No character should appear more than twice
    public boolean isPasswordCharLimitValid(String password) {
        Map<Character, Integer> map = new HashMap<>();
        for (char c : password.toCharArray()) {
            map.put(c, map.getOrDefault(c, 0) + 1);
            if (map.get(c) > 2) return false;
        }
        return true;
    }

    public boolean isPasswordValid(String password) {
        Map<Character, Integer> charCount = new HashMap<>();

        for (char c : password.toCharArray()) {
            int count = charCount.getOrDefault(c, 0) + 1;
            if (count > 2) {
                return false; // Invalid: character appears more than twice
            }
            charCount.put(c, count);
        }

        return true; // Valid: no character occurs more than twice
    }

    private void saveUserType(String uid, String userType, String userName, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("type", userType);
        userData.put("userName", userName);
        userData.put("email", email);
        userData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    LoadingDialog.hide();
                    Toast.makeText(SignupActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    LoadingDialog.hide();
                    Log.e("SignupActivity", "Error saving user to Firestore: ", e);
                    // Clean up auth user if document creation failed
                    if (mAuth.getCurrentUser() != null) {
                        mAuth.getCurrentUser().delete();
                    }
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && errorMsg.contains("PERMISSION_DENIED")) {
                        Toast.makeText(SignupActivity.this, "Firestore Permission Denied! Please update rules in Firebase Console.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SignupActivity.this, "Failed to save user profile: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
