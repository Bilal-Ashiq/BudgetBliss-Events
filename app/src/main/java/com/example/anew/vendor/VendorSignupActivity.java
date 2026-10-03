package com.example.anew.vendor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VendorSignupActivity extends AppCompatActivity {

    private EditText username, cnic, phone, password, email;
    private Button signupButton;
    private TextView loginRedirect;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendorsignup);

        username = findViewById(R.id.vendorname);
        cnic = findViewById(R.id.vendorcnic);
        phone = findViewById(R.id.vendorphone);
        password = findViewById(R.id.vendorsignuppassword);
        signupButton = findViewById(R.id.vendorsignupbutton);
        email = findViewById(R.id.vendorEmail);
        loginRedirect = findViewById(R.id.lo12);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredUsername = username.getText().toString().trim();
                String enteredCnic = cnic.getText().toString().trim();
                String enteredPhone = phone.getText().toString().trim();
                String enteredPassword = password.getText().toString().trim();
                String enteredEmail = email.getText().toString().trim();

                if (enteredUsername.isEmpty()) {
                    username.setError("Username is required");
                } else if (!enteredUsername.matches("^[a-zA-Z ]{3,}$")) {
                    Toast.makeText(VendorSignupActivity.this, "Username must be at least 3 letters and contain only letters and spaces", Toast.LENGTH_SHORT).show();
                }

                // CNIC validation
                else if (enteredCnic.isEmpty()) {
                    cnic.setError("CNIC is required");
                } else if (!isValidCNIC(enteredCnic)) {
                    Toast.makeText(VendorSignupActivity.this, "Invalid CNIC format", Toast.LENGTH_SHORT).show();
                }

                // Phone number validation
                else if (enteredPhone.isEmpty()) {
                    Toast.makeText(VendorSignupActivity.this, "Phone number is required", Toast.LENGTH_SHORT).show();
                } else if (!enteredPhone.matches("^03[0-9]{9}$")) {
                    Toast.makeText(VendorSignupActivity.this, "Invalid phone number format", Toast.LENGTH_SHORT).show();
                }

                // Email validation
                else if (enteredEmail.isEmpty()) {
                    email.setError("Email is required");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches()) {
                    email.setError("Enter a valid email address");
                }

                // Password validation
                else if (enteredPassword.isEmpty()) {
                    password.setError("Password is required");
                } else if (!isPasswordStrong(enteredPassword)) {
                    Toast.makeText(VendorSignupActivity.this, "Password must be 8+ chars, with upper, lower, and digit", Toast.LENGTH_SHORT).show();
                } else if (!isPasswordCharLimitValid(enteredPassword)) {
                    Toast.makeText(VendorSignupActivity.this, "No character should appear more than twice", Toast.LENGTH_SHORT).show();
                } else {
                    LoadingDialog.show(VendorSignupActivity.this);
                    mAuth.createUserWithEmailAndPassword(enteredEmail, enteredPassword)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String uid = mAuth.getCurrentUser().getUid();
                                    saveUserType(uid, UserType.VENDOR.name(), enteredUsername, enteredCnic, enteredPhone, enteredEmail);
                                } else {
                                    LoadingDialog.hide();
                                    Toast.makeText(VendorSignupActivity.this, "Signup failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }
        });
    }

    // Strong password: at least 8 characters, includes upper, lower, digit
    public boolean isPasswordStrong(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*");
    }

    // No character more than twice
    public boolean isPasswordCharLimitValid(String password) {
        Map<Character, Integer> countMap = new HashMap<>();
        for (char c : password.toCharArray()) {
            countMap.put(c, countMap.getOrDefault(c, 0) + 1);
            if (countMap.get(c) > 2) return false;
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

    public boolean isValidCNIC(String cnicStr) {
        // Remove any dashes or spaces from the input
        String cleanCnic = cnicStr.replaceAll("[\\s-]", "");
        
        // Check if it's exactly 13 digits
        if (!cleanCnic.matches("^[0-9]{13}$")) {
            return false;
        }
        
        // Format the CNIC with dashes (optional)
        String formattedCnic = cleanCnic.substring(0, 5) + "-" + 
                             cleanCnic.substring(5, 12) + "-" + 
                             cleanCnic.substring(12);
        
        // Update the EditText with formatted CNIC
        cnic.setText(formattedCnic);
        
        return true;
    }


    private void saveUserType(String uid, String userType, String userName, String cnic, String phone, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("type", userType);
        userData.put("userName", userName);
        userData.put("createdAt", FieldValue.serverTimestamp());
        userData.put("cnic", cnic);
        userData.put("phone", phone);
        userData.put("email", email);
        userData.put("services", new ArrayList<String>());
        userData.put("reviews", new ArrayList<Map<String, Object>>());
        userData.put("workImages", new ArrayList<String>());
        db.collection("vendors").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    LoadingDialog.hide();
                    Toast.makeText(VendorSignupActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VendorSignupActivity.this, CreateProfileActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    LoadingDialog.hide();
                    Log.e("VendorSignupActivity", "Error saving vendor to Firestore: ", e);
                    // Clean up auth user if document creation failed
                    if (mAuth.getCurrentUser() != null) {
                        mAuth.getCurrentUser().delete();
                    }
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && errorMsg.contains("PERMISSION_DENIED")) {
                        Toast.makeText(VendorSignupActivity.this, "Firestore Permission Denied! Please update rules in Firebase Console.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(VendorSignupActivity.this, "Failed to save user profile: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
