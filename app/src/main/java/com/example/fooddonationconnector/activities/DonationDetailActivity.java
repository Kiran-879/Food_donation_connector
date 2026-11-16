package com.example.fooddonationconnector.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.fooddonationconnector.R;
import com.example.fooddonationconnector.databinding.ActivityDonationDetailBinding;
import com.example.fooddonationconnector.models.Donation;
import com.example.fooddonationconnector.models.User;
import com.example.fooddonationconnector.utils.ImageUtils;
import com.example.fooddonationconnector.utils.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class DonationDetailActivity extends AppCompatActivity {

    private ActivityDonationDetailBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private PreferenceManager preferenceManager;

    private Donation currentDonation;
    private String donationId;
    private ListenerRegistration detailListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDonationDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        preferenceManager = new PreferenceManager(this);

        setupToolbar();

        if (getIntent().hasExtra("extra_donation")) {
            currentDonation = (Donation) getIntent().getSerializableExtra("extra_donation");
            if (currentDonation != null) {
                donationId = currentDonation.getDonationId();
                bindDonationData(currentDonation);
            }
        } else if (getIntent().hasExtra("extra_donation_id")) {
            donationId = getIntent().getStringExtra("extra_donation_id");
        }

        if (donationId != null && !donationId.isEmpty()) {
            listenToDonationUpdates();
        } else {
            Toast.makeText(this, "Donation details not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupButtons();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void listenToDonationUpdates() {
        binding.progressBar.setVisibility(View.VISIBLE);
        DocumentReference docRef = db.collection("donations").document(donationId);

        detailListener = docRef.addSnapshotListener((snapshot, e) -> {
            binding.progressBar.setVisibility(View.GONE);
            if (e != null) {
                Toast.makeText(this, "Error syncing donation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                currentDonation = snapshot.toObject(Donation.class);
                if (currentDonation != null) {
                    currentDonation.setDonationId(snapshot.getId());
                    bindDonationData(currentDonation);
                }
            }
        });
    }

    private void bindDonationData(Donation donation) {
        binding.tvDetailFoodType.setText(donation.getFoodType());
        binding.tvDetailQuantity.setText(String.format("Quantity: %s", donation.getQuantity()));
        binding.tvDetailExpiryTime.setText(String.format("Expires: %s", donation.getExpiryTime()));
        binding.tvDetailAddress.setText(String.format("Pickup Address: %s", donation.getPickupAddress()));

        // Notes
        if (donation.getAdditionalNotes() != null && !donation.getAdditionalNotes().trim().isEmpty()) {
            binding.llDetailNotes.setVisibility(View.VISIBLE);
            binding.tvDetailNotes.setText(donation.getAdditionalNotes());
        } else {
            binding.llDetailNotes.setVisibility(View.GONE);
        }

        // Donor details
        String donorName = (donation.getDonorName() != null && !donation.getDonorName().isEmpty())
                ? donation.getDonorName() : "Anonymous Donor";
        binding.tvDetailDonorName.setText(String.format("Donor: %s", donorName));

        String phone = (donation.getDonorPhone() != null && !donation.getDonorPhone().isEmpty())
                ? donation.getDonorPhone() : "Not provided";
        binding.tvDetailDonorPhone.setText(String.format("Contact: %s", phone));

        // Receiver info if claimed
        if (donation.getReceiverName() != null && !donation.getReceiverName().isEmpty()) {
            binding.tvDetailReceiverInfo.setVisibility(View.VISIBLE);
            binding.tvDetailReceiverInfo.setText(String.format("Claimed by: %s", donation.getReceiverName()));
        } else {
            binding.tvDetailReceiverInfo.setVisibility(View.GONE);
        }

        // Status badge
        String status = donation.getStatus() != null ? donation.getStatus() : Donation.STATUS_AVAILABLE;
        binding.tvDetailStatus.setText(status);

        if (Donation.STATUS_AVAILABLE.equalsIgnoreCase(status)) {
            binding.tvDetailStatus.setBackgroundResource(R.drawable.bg_status_available);
            binding.tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_available_text));
        } else if (Donation.STATUS_CLAIMED.equalsIgnoreCase(status)) {
            binding.tvDetailStatus.setBackgroundResource(R.drawable.bg_status_claimed);
            binding.tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_claimed_text));
        } else {
            binding.tvDetailStatus.setBackgroundResource(R.drawable.bg_status_completed);
            binding.tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.status_completed_text));
        }

        // Photo
        ImageUtils.loadPhoto(this, donation.getPhotoData(), binding.ivDetailFoodPhoto);

        // Adjust Action buttons based on Role and Status
        updateActionButtons(donation);
    }

    private void updateActionButtons(Donation donation) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : preferenceManager.getUserId();
        String userRole = preferenceManager.getUserRole();
        String status = donation.getStatus() != null ? donation.getStatus() : Donation.STATUS_AVAILABLE;

        boolean isDonor = currentUid.equals(donation.getDonorId());
        boolean isClaimant = currentUid.equals(donation.getReceiverId());

        if (Donation.STATUS_AVAILABLE.equalsIgnoreCase(status)) {
            binding.btnActionComplete.setVisibility(View.GONE);

            // NGOs/Receivers can request/accept donation
            if (!isDonor) {
                binding.btnActionRequest.setVisibility(View.VISIBLE);
                binding.btnActionRequest.setText(R.string.btn_request_accept);
                binding.btnActionRequest.setEnabled(true);
            } else {
                // If donor views their own post
                binding.btnActionRequest.setVisibility(View.VISIBLE);
                binding.btnActionRequest.setText("Your Available Donation");
                binding.btnActionRequest.setEnabled(false);
            }
        } else if (Donation.STATUS_CLAIMED.equalsIgnoreCase(status)) {
            binding.btnActionRequest.setVisibility(View.GONE);

            // Either the donor or claimant can mark as completed
            if (isDonor || isClaimant || User.ROLE_NGO.equalsIgnoreCase(userRole)) {
                binding.btnActionComplete.setVisibility(View.VISIBLE);
                binding.btnActionComplete.setEnabled(true);
            } else {
                binding.btnActionComplete.setVisibility(View.GONE);
            }
        } else { // COMPLETED
            binding.btnActionRequest.setVisibility(View.GONE);
            binding.btnActionComplete.setVisibility(View.GONE);
        }
    }

    private void setupButtons() {
        // Request / Accept Button
        binding.btnActionRequest.setOnClickListener(v -> confirmAndRequestDonation());

        // Mark Completed Button
        binding.btnActionComplete.setOnClickListener(v -> confirmAndMarkCompleted());

        // Call Donor Button
        binding.btnCallDonor.setOnClickListener(v -> {
            if (currentDonation != null && currentDonation.getDonorPhone() != null && !currentDonation.getDonorPhone().isEmpty()) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + currentDonation.getDonorPhone()));
                startActivity(dialIntent);
            } else {
                Toast.makeText(this, "No contact phone number provided", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmAndRequestDonation() {
        new AlertDialog.Builder(this)
                .setTitle("Request Donation")
                .setMessage("Are you sure you want to request and claim this food donation?")
                .setPositiveButton("Accept & Claim", (dialog, which) -> executeRequestDonation())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void executeRequestDonation() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : preferenceManager.getUserId();
        String receiverName = preferenceManager.getUserName();

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnActionRequest.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", Donation.STATUS_CLAIMED);
        updates.put("receiverId", currentUid);
        updates.put("receiverName", receiverName);

        db.collection("donations").document(donationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(DonationDetailActivity.this, "Donation accepted and claimed successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnActionRequest.setEnabled(true);
                    Toast.makeText(DonationDetailActivity.this, "Error claiming donation: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void confirmAndMarkCompleted() {
        new AlertDialog.Builder(this)
                .setTitle("Mark as Received / Completed")
                .setMessage("Has this donation been successfully handed over and received?")
                .setPositiveButton("Mark Completed", (dialog, which) -> executeMarkCompleted())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void executeMarkCompleted() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnActionComplete.setEnabled(false);

        db.collection("donations").document(donationId)
                .update("status", Donation.STATUS_COMPLETED)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(DonationDetailActivity.this, "Donation completed! Thank you for reducing food waste.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnActionComplete.setEnabled(true);
                    Toast.makeText(DonationDetailActivity.this, "Error updating status: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (detailListener != null) {
            detailListener.remove();
        }
    }
}

