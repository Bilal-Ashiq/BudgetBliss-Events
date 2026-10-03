package com.example.anew.vendor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.anew.R;
import com.example.anew.utils.LoadingDialog;
import com.example.anew.utils.OnAllImagesUploadedListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CreateProfileActivity extends AppCompatActivity {

    EditText etProfileTitle, etServices;

    ImageView profileImage, image1, image2, image3;

    private Button createProfile;

    private Uri selectedImageUri, imageUri1, imageUri2, imageUri3;

    private String profileTitle, services;

    private int selection = 0;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        etProfileTitle = findViewById(R.id.etProfileTitle);
        etServices = findViewById(R.id.etServices);
        profileImage = findViewById(R.id.ivProfile);
        createProfile = findViewById(R.id.createProfile);

        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        image1.setOnClickListener(v -> {
            selection = 1;
            openGallery();
        });

        image2.setOnClickListener(v -> {
            selection = 2;
            openGallery();
        });


        image3.setOnClickListener(v -> {
            selection = 3;
            openGallery();
        });

        profileImage.setOnClickListener(v -> {
            selection = 0;
            openGallery();
        });

        createProfile.setOnClickListener(v -> {
            profileTitle = etProfileTitle.getText().toString();
            services = etServices.getText().toString();

            if (profileTitle.isEmpty() || services.isEmpty()) {
                Toast.makeText(CreateProfileActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {

                if (selectedImageUri == null) {
                    Toast.makeText(CreateProfileActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                } else {
                    uploadImagesToStorage(Arrays.asList(selectedImageUri, imageUri1, imageUri2, imageUri3), urls -> {
                        createProfile(profileTitle, services, urls);
                    });
                }
            }
        });

    }

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    switch (selection) {
                        case 0:
                            selectedImageUri = result.getData().getData();
                            profileImage.setImageURI(selectedImageUri);
                            break;

                            case 1:
                            imageUri1 = result.getData().getData();
                            image1.setImageURI(imageUri1);
                            break;

                            case 2:
                            imageUri2 = result.getData().getData();
                            image2.setImageURI(imageUri2);
                            break;

                            case 3:
                            imageUri3 = result.getData().getData();
                            image3.setImageURI(imageUri3);
                            break;
                    }
                }
            });

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void createProfile(String profileTitle, String services, List<String> imageUrls) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("profileTitle", profileTitle);
        userData.put("services", services);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            userData.put("workImages", imageUrls);
        }

        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        db.collection("vendors").document(uid)
                .update(userData)
                .addOnSuccessListener(aVoid -> {
                    LoadingDialog.hide();
                    Toast.makeText(CreateProfileActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateProfileActivity.this, VendorHomeActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    LoadingDialog.hide();
                    Toast.makeText(CreateProfileActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    public void uploadImageToFirebaseStorage(Uri imageUri, String fileName, OnSuccessListener<String> listener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("event_pics/" + fileName);
        LoadingDialog.show(CreateProfileActivity.this);
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

    public void uploadImagesToStorage(List<Uri> imageUris, OnAllImagesUploadedListener listener) {
        List<String> uploadedUrls = new ArrayList<>();

        if (imageUris == null || imageUris.isEmpty()) {
            listener.onAllUploaded(uploadedUrls);
            return;
        }

        LoadingDialog.show(CreateProfileActivity.this);

        for (int i = 0; i < imageUris.size(); i++) {
            Uri uri = imageUris.get(i);
            String fileName = "image_" + System.currentTimeMillis() + "_" + i + ".jpg";

            uploadImageToFirebaseStorage(uri, fileName, url -> {
                uploadedUrls.add(url);

                if (uploadedUrls.size() == imageUris.size()) {
                    listener.onAllUploaded(uploadedUrls); // Callback when all done
                }
            });
        }
    }

}