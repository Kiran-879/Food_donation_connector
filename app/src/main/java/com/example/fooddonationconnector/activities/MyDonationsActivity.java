package com.example.fooddonationconnector.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fooddonationconnector.R;
import com.example.fooddonationconnector.adapters.DonationAdapter;
import com.example.fooddonationconnector.databinding.ActivityMyDonationsBinding;
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

public class MyDonationsActivity extends AppCompatActivity implements DonationAdapter.OnDonationClickListener {

    private ActivityMyDonationsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private PreferenceManager preferenceManager;

    private DonationAdapter adapter;
    private final List<Donation> userDonations = new ArrayList<>();
    private final List<Donation> filteredList = new ArrayList<>();
    private ListenerRegistration historyListener;
    private String currentStatusFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyDonationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        preferenceManager = new PreferenceManager(this);

        setupToolbar();
        setupRecyclerView();
        setupFilterChips();
        setupSwipeRefresh();
        loadDonationHistory();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new DonationAdapter(this, filteredList, this);
        binding.rvMyDonations.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMyDonations.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadDonationHistory);
    }

    private void setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentStatusFilter = "ALL";
            } else {
                int id = checkedIds.get(0);
                if (id == R.id.chipFilterAvailable) {
                    currentStatusFilter = Donation.STATUS_AVAILABLE;
                } else if (id == R.id.chipFilterClaimed) {
                    currentStatusFilter = Donation.STATUS_CLAIMED;
                } else if (id == R.id.chipFilterCompleted) {
                    currentStatusFilter = Donation.STATUS_COMPLETED;
                } else {
                    currentStatusFilter = "ALL";
                }
            }
            applyFilter();
        });
    }

    private void loadDonationHistory() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to view history", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = currentUser.getUid();
        String userRole = preferenceManager.getUserRole();
        boolean isNgo = User.ROLE_NGO.equalsIgnoreCase(userRole);

        binding.progressBar.setVisibility(View.VISIBLE);

        if (historyListener != null) {
            historyListener.remove();
        }

        // For Donors: query donations where donorId == uid
        // For NGOs: query donations where receiverId == uid
        String filterField = isNgo ? "receiverId" : "donorId";

        Query query = db.collection("donations")
                .whereEqualTo(filterField, uid)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        historyListener = query.addSnapshotListener((snapshots, e) -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefreshLayout.setRefreshing(false);

            if (e != null) {
                // If composite index is pending, fallback without ordering
                loadHistoryWithoutOrdering(filterField, uid);
                return;
            }

            userDonations.clear();
            if (snapshots != null && !snapshots.isEmpty()) {
                for (QueryDocumentSnapshot doc : snapshots) {
                    Donation item = doc.toObject(Donation.class);
                    item.setDonationId(doc.getId());
                    userDonations.add(item);
                }
            }
            applyFilter();
        });
    }

    private void loadHistoryWithoutOrdering(String filterField, String uid) {
        db.collection("donations")
                .whereEqualTo(filterField, uid)
                .get()
                .addOnSuccessListener(snapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefreshLayout.setRefreshing(false);

                    userDonations.clear();
                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Donation item = doc.toObject(Donation.class);
                            item.setDonationId(doc.getId());
                            userDonations.add(item);
                        }
                    }
                    applyFilter();
                })
                .addOnFailureListener(err -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(this, "Failed to load history: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void applyFilter() {
        filteredList.clear();

        if ("ALL".equalsIgnoreCase(currentStatusFilter)) {
            filteredList.addAll(userDonations);
        } else {
            for (Donation item : userDonations) {
                if (currentStatusFilter.equalsIgnoreCase(item.getStatus())) {
                    filteredList.add(item);
                }
            }
        }

        if (filteredList.isEmpty()) {
            binding.llEmptyState.setVisibility(View.VISIBLE);
            binding.rvMyDonations.setVisibility(View.GONE);
        } else {
            binding.llEmptyState.setVisibility(View.GONE);
            binding.rvMyDonations.setVisibility(View.VISIBLE);
        }

        adapter.updateList(filteredList);
    }

    @Override
    public void onDonationClick(Donation donation) {
        Intent intent = new Intent(MyDonationsActivity.this, DonationDetailActivity.class);
        intent.putExtra("extra_donation", donation);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (historyListener != null) {
            historyListener.remove();
        }
    }
}

