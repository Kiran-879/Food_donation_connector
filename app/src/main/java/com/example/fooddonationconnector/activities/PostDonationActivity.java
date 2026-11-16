package com.example.fooddonationconnector.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fooddonationconnector.R;
import com.example.fooddonationconnector.databinding.ActivityPostDonationBinding;
import com.example.fooddonationconnector.models.Donation;
import com.example.fooddonationconnector.utils.ImageUtils;
import com.example.fooddonationconnector.utils.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PostDonationActivity extends AppCompatActivity {

    private ActivityPostDonationBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private PreferenceManager preferenceManager;

    private final Calendar selectedCalendar = Calendar.getInstance();
    private String encodedPhotoData = "";

    // Image Picker launcher (Gallery)
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        binding.ivFoodPhoto.setImageBitmap(bitmap);
                        encodedPhotoData = ImageUtils.bitmapToBase64(bitmap);
                        binding.btnSelectPhoto.setText(R.string.change_photo);
                    } catch (IOException e) {
                        Toast.makeText(this, "Failed to load selected image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDonationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        preferenceManager = new PreferenceManager(this);

        setupToolbar();
        setupDateTimePickers();
        setupPhotoPicker();
        setupSubmitButton();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDateTimePickers() {
        binding.etExpiryTime.setOnClickListener(v -> showDatePicker());
        binding.tilExpiryTime.setEndIconOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        int year = selectedCalendar.get(Calendar.YEAR);
        int month = selectedCalendar.get(Calendar.MONTH);
        int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedCalendar.set(Calendar.YEAR, selectedYear);
                    selectedCalendar.set(Calendar.MONTH, selectedMonth);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                    showTimePicker();
                },
                year, month, day
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        int hour = selectedCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = selectedCalendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    selectedCalendar.set(Calendar.MINUTE, selectedMinute);

                    SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                    binding.etExpiryTime.setText(format.format(selectedCalendar.getTime()));
                    binding.tilExpiryTime.setError(null);
                },
                hour, minute, false
        );
        timePickerDialog.show();
    }

    private void setupPhotoPicker() {
        binding.btnSelectPhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    private void setupSubmitButton() {
        binding.btnSubmitDonation.setOnClickListener(v -> submitDonation());
    }

    private void submitDonation() {
        String foodType = binding.etFoodType.getText() != null ? binding.etFoodType.getText().toString().trim() : "";
        String quantity = binding.etQuantity.getText() != null ? binding.etQuantity.getText().toString().trim() : "";
        String expiryTime = binding.etExpiryTime.getText() != null ? binding.etExpiryTime.getText().toString().trim() : "";
        String pickupAddress = binding.etPickupAddress.getText() != null ? binding.etPickupAddress.getText().toString().trim() : "";
        String additionalNotes = binding.etAdditionalNotes.getText() != null ? binding.etAdditionalNotes.getText().toString().trim() : "";

        if (TextUtils.isEmpty(foodType)) {
            binding.tilFoodType.setError(getString(R.string.err_empty_field));
            binding.etFoodType.requestFocus();
            return;
        } else {
            binding.tilFoodType.setError(null);
        }

        if (TextUtils.isEmpty(quantity)) {
            binding.tilQuantity.setError(getString(R.string.err_empty_field));
            binding.etQuantity.requestFocus();
            return;
        } else {
            binding.tilQuantity.setError(null);
        }

        if (TextUtils.isEmpty(expiryTime)) {
            binding.tilExpiryTime.setError(getString(R.string.err_select_expiry));
            binding.etExpiryTime.requestFocus();
            return;
        } else {
            binding.tilExpiryTime.setError(null);
        }

        if (TextUtils.isEmpty(pickupAddress)) {
            binding.tilPickupAddress.setError(getString(R.string.err_empty_field));
            binding.etPickupAddress.requestFocus();
            return;
        } else {
            binding.tilPickupAddress.setError(null);
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : preferenceManager.getUserId();
        String donorName = preferenceManager.getUserName();
        String donorPhone = preferenceManager.getUserPhone();

        showLoading(true);

        String newDocId = db.collection("donations").document().getId();

        Donation newDonation = new Donation(
                newDocId,
                foodType,
                quantity,
                expiryTime,
                pickupAddress,
                additionalNotes,
                encodedPhotoData,
                currentUid,
                donorName,
                donorPhone,
                "",
                "",
                Donation.STATUS_AVAILABLE,
                System.currentTimeMillis()
        );

        db.collection("donations").document(newDocId).set(newDonation)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(PostDonationActivity.this, "Food donation posted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(PostDonationActivity.this, "Failed to post donation: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSubmitDonation.setEnabled(!isLoading);
        binding.btnSelectPhoto.setEnabled(!isLoading);
        binding.etFoodType.setEnabled(!isLoading);
        binding.etQuantity.setEnabled(!isLoading);
        binding.etExpiryTime.setEnabled(!isLoading);
        binding.etPickupAddress.setEnabled(!isLoading);
        binding.etAdditionalNotes.setEnabled(!isLoading);
    }
}

