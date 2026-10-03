package com.example.anew.vendor.home.ui.vendor_profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.anew.R;
import com.example.anew.customer.ViewBidActivity;
import com.example.anew.customer.models.Bid;
import com.example.anew.databinding.FragmentVendorProfileBinding;
import com.example.anew.utils.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class VendorProfileFragment extends Fragment {

    private FragmentVendorProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentVendorProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        getVendorData(auth.getCurrentUser().getUid());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void getVendorData(String vendorId) {
        LoadingDialog.show(getActivity());
        db.collection("vendors")
                .document(vendorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    LoadingDialog.hide();
                    if (documentSnapshot.exists()) {
                        Map<String, Object> vendorData = documentSnapshot.getData();
                        if (vendorData != null) {
                            String name = vendorData.get("userName").toString();
                            String services = vendorData.get("services").toString();
                            String phone = vendorData.get("phone").toString();
                            String email = vendorData.get("email").toString();
                            List<String> workImages = (List<String>) vendorData.get("workImages");

                            // Update UI with vendor data
                            binding.etProfileTitle.setText(name);
                            binding.etServices.setText("Services: "+services);
                            binding.etEmail.setText("Email: "+email);
                            binding.etPhone.setText("Phone: "+phone);

                            Glide.with(this)
                                    .load(workImages.get(0))
                                    .placeholder(R.drawable.baseline_image_24)
                                    .into(binding.ivProfile);

                            Glide.with(this)
                                    .load(workImages.get(1))
                                    .placeholder(R.drawable.baseline_image_24)
                                    .into(binding.image1);

                            Glide.with(this)
                                    .load(workImages.get(2))
                                    .placeholder(R.drawable.baseline_image_24)
                                    .into(binding.image2);

                            Glide.with(this)
                                    .load(workImages.get(3))
                                    .placeholder(R.drawable.baseline_image_24)
                                    .into(binding.image3);

                            // Use other fields as needed
                            Log.d("FIRESTORE", "Vendor name: " + name);
                        } else {
                            Log.d("FIRESTORE", "Vendor not found");
                        }
                    }
                }).addOnFailureListener(e -> {
                    LoadingDialog.hide();
                    Log.e("FIRESTORE", "Error fetching vendor", e);
                });
    }
}
