package com.example.fooddonationconnector.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fooddonationconnector.R;
import com.example.fooddonationconnector.adapters.DonationAdapter;
import com.example.fooddonationconnector.databinding.ActivityHomeBinding;
import com.example.fooddonationconnector.models.Donation;
import com.example.fooddonationconnector.models.User;
import com.example.fooddonationconnector.utils.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements DonationAdapter.OnDonationClickListener {

    private ActivityHomeBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;
    private DonationAdapter adapter;
    private final List<Donation> recentDonationList = new ArrayList<>();
    private ListenerRegistration donationsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);

        initUserHeader();
        setupRecyclerView();
        setupActionCards();
        loadRecentDonations();
    }

    private void initUserHeader() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            signOutAndNavigateToLogin();
            return;
        }

        String displayName = preferenceManager.getUserName();
        String role = preferenceManager.getUserRole();

        binding.tvUserName.setText(displayName);
        String roleLabel = User.ROLE_NGO.equalsIgnoreCase(role) ? "NGO / Receiver" : "Food Donor";
        binding.tvUserRole.setText(String.format("Role: %s", roleLabel));

        // If user is NGO/Receiver, tailor Post Donation card styling or show helper info
        if (User.ROLE_NGO.equalsIgnoreCase(role)) {
            binding.cardPostDonation.setStrokeColor(getColor(R.color.divider));
        }

        // Fetch fresh profile from Firestore in background
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        if (user != null) {
                            preferenceManager.saveUserSession(
                                    user.getUid(),
                                    user.getName(),
                                    user.getEmail(),
                                    user.getPhone(),
                                    user.getRole()
                            );
                            binding.tvUserName.setText(user.getName());
                            String updatedRole = user.isNgo() ? "NGO / Receiver" : "Food Donor";
                            binding.tvUserRole.setText(String.format("Role: %s", updatedRole));
                        }
                    }
                });
    }

    private void setupRecyclerView() {
        adapter = new DonationAdapter(this, recentDonationList, this);
        binding.rvRecentDonations.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecentDonations.setAdapter(adapter);
    }

    private void setupActionCards() {
        // Post Donation
        binding.cardPostDonation.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PostDonationActivity.class);
            startActivity(intent);
        });

        // Browse Donations
        binding.cardBrowseDonations.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DonationListActivity.class);
            startActivity(intent);
        });

        // My Activity / History
        binding.cardMyDonations.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MyDonationsActivity.class);
            startActivity(intent);
        });

        // View All recent
        binding.tvViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DonationListActivity.class);
            startActivity(intent);
        });

        // Sign Out
        binding.ivLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void loadRecentDonations() {
        binding.pbRecentDonations.setVisibility(View.VISIBLE);

        donationsListener = db.collection("donations")
                .whereEqualTo("status", Donation.STATUS_AVAILABLE)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener((snapshots, e) -> {
                    binding.pbRecentDonations.setVisibility(View.GONE);
                    if (e != null) {
                        // In case compound index is pending, fallback to simple query
                        loadDonationsWithoutOrdering();
                        return;
                    }

                    recentDonationList.clear();
                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Donation donation = doc.toObject(Donation.class);
                            donation.setDonationId(doc.getId());
                            recentDonationList.add(donation);
                        }
                        binding.tvEmptyRecent.setVisibility(View.GONE);
                        binding.rvRecentDonations.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvEmptyRecent.setVisibility(View.VISIBLE);
                        binding.rvRecentDonations.setVisibility(View.GONE);
                    }
                    adapter.updateList(recentDonationList);
                });
    }

    private void loadDonationsWithoutOrdering() {
        db.collection("donations")
                .whereEqualTo("status", Donation.STATUS_AVAILABLE)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.pbRecentDonations.setVisibility(View.GONE);
                    recentDonationList.clear();
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Donation donation = doc.toObject(Donation.class);
                            donation.setDonationId(doc.getId());
                            recentDonationList.add(donation);
                        }
                        binding.tvEmptyRecent.setVisibility(View.GONE);
                        binding.rvRecentDonations.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvEmptyRecent.setVisibility(View.VISIBLE);
                        binding.rvRecentDonations.setVisibility(View.GONE);
                    }
                    adapter.updateList(recentDonationList);
                })
                .addOnFailureListener(err -> {
                    binding.pbRecentDonations.setVisibility(View.GONE);
                    binding.tvEmptyRecent.setVisibility(View.VISIBLE);
                });
    }

    @Override
    public void onDonationClick(Donation donation) {
        Intent intent = new Intent(HomeActivity.this, DonationDetailActivity.class);
        intent.putExtra("extra_donation", donation);
        startActivity(intent);
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (dialog, which) -> signOutAndNavigateToLogin())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void signOutAndNavigateToLogin() {
        mAuth.signOut();
        preferenceManager.clearSession();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (donationsListener != null) {
            donationsListener.remove();
        }
    }
}

