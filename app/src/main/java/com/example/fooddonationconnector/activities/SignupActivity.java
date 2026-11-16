package com.example.fooddonationconnector.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fooddonationconnector.R;
import com.example.fooddonationconnector.databinding.ActivitySignupBinding;
import com.example.fooddonationconnector.models.User;
import com.example.fooddonationconnector.utils.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnSignup.setOnClickListener(v -> performSignup());

        binding.tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void performSignup() {
        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

        // Check selected role
        String selectedRole = binding.rbDonor.isChecked() ? User.ROLE_DONOR : User.ROLE_NGO;

        if (TextUtils.isEmpty(fullName)) {
            binding.tilFullName.setError(getString(R.string.err_empty_field));
            binding.etFullName.requestFocus();
            return;
        } else {
            binding.tilFullName.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.err_empty_field));
            binding.etEmail.requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.err_invalid_email));
            binding.etEmail.requestFocus();
            return;
        } else {
            binding.tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(phone)) {
            binding.tilPhone.setError(getString(R.string.err_empty_field));
            binding.etPhone.requestFocus();
            return;
        } else {
            binding.tilPhone.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.err_empty_field));
            binding.etPassword.requestFocus();
            return;
        } else if (password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.err_short_password));
            binding.etPassword.requestFocus();
            return;
        } else {
            binding.tilPassword.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.err_passwords_mismatch));
            binding.etConfirmPassword.requestFocus();
            return;
        } else {
            binding.tilConfirmPassword.setError(null);
        }

        showLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToFirestore(firebaseUser.getUid(), fullName, email, phone, selectedRole);
                        } else {
                            showLoading(false);
                            navigateToHome();
                        }
                    } else {
                        showLoading(false);
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Registration failed.";
                        Toast.makeText(SignupActivity.this, "Sign Up Failed: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid, String name, String email, String phone, String role) {
        User newUser = new User(uid, name, email, phone, role, System.currentTimeMillis());

        db.collection("users").document(uid).set(newUser)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    preferenceManager.saveUserSession(uid, name, email, phone, role);
                    Toast.makeText(SignupActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    preferenceManager.saveUserSession(uid, name, email, phone, role);
                    Toast.makeText(SignupActivity.this, "Profile creation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    navigateToHome();
                });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSignup.setEnabled(!isLoading);
        binding.etFullName.setEnabled(!isLoading);
        binding.etEmail.setEnabled(!isLoading);
        binding.etPhone.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
        binding.etConfirmPassword.setEnabled(!isLoading);
    }

    private void navigateToHome() {
        Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

