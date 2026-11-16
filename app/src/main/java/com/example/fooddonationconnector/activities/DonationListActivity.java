package com.example.fooddonationconnector.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fooddonationconnector.adapters.DonationAdapter;
import com.example.fooddonationconnector.databinding.ActivityDonationListBinding;
import com.example.fooddonationconnector.models.Donation;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DonationListActivity extends AppCompatActivity implements DonationAdapter.OnDonationClickListener {

    private ActivityDonationListBinding binding;
    private FirebaseFirestore db;
    private DonationAdapter adapter;
    private final List<Donation> allAvailableDonations = new ArrayList<>();
    private final List<Donation> filteredList = new ArrayList<>();
    private ListenerRegistration donationsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDonationListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        setupToolbar();
        setupRecyclerView();
        setupSearchFilter();
        setupSwipeRefresh();
        loadAvailableDonations();
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
        binding.rvDonations.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDonations.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadAvailableDonations);
    }

    private void setupSearchFilter() {
        binding.etSearchDonations.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void loadAvailableDonations() {
        binding.progressBar.setVisibility(View.VISIBLE);

        if (donationsListener != null) {
            donationsListener.remove();
        }

        donationsListener = db.collection("donations")
                .whereEqualTo("status", Donation.STATUS_AVAILABLE)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefreshLayout.setRefreshing(false);

                    if (e != null) {
                        // Fallback without ordering in case index is pending
                        loadWithoutOrdering();
                        return;
                    }

                    allAvailableDonations.clear();
                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Donation item = doc.toObject(Donation.class);
                            item.setDonationId(doc.getId());
                            allAvailableDonations.add(item);
                        }
                    }

                    filterList(binding.etSearchDonations.getText().toString());
                });
    }

    private void loadWithoutOrdering() {
        db.collection("donations")
                .whereEqualTo("status", Donation.STATUS_AVAILABLE)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefreshLayout.setRefreshing(false);

                    allAvailableDonations.clear();
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Donation item = doc.toObject(Donation.class);
                            item.setDonationId(doc.getId());
                            allAvailableDonations.add(item);
                        }
                    }
                    filterList(binding.etSearchDonations.getText().toString());
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(this, "Error loading donations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void filterList(String query) {
        filteredList.clear();
        String lowerCaseQuery = query != null ? query.toLowerCase().trim() : "";

        if (lowerCaseQuery.isEmpty()) {
            filteredList.addAll(allAvailableDonations);
        } else {
            for (Donation donation : allAvailableDonations) {
                boolean matchesFood = donation.getFoodType() != null &&
                        donation.getFoodType().toLowerCase().contains(lowerCaseQuery);
                boolean matchesAddress = donation.getPickupAddress() != null &&
                        donation.getPickupAddress().toLowerCase().contains(lowerCaseQuery);

                if (matchesFood || matchesAddress) {
                    filteredList.add(donation);
                }
            }
        }

        if (filteredList.isEmpty()) {
            binding.llEmptyState.setVisibility(View.VISIBLE);
            binding.rvDonations.setVisibility(View.GONE);
        } else {
            binding.llEmptyState.setVisibility(View.GONE);
            binding.rvDonations.setVisibility(View.VISIBLE);
        }

        adapter.updateList(filteredList);
    }

    @Override
    public void onDonationClick(Donation donation) {
        Intent intent = new Intent(DonationListActivity.this, DonationDetailActivity.class);
        intent.putExtra("extra_donation", donation);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (donationsListener != null) {
            donationsListener.remove();
        }
    }
}

