package com.example.fooddonationconnector.models;

import java.io.Serializable;

public class User implements Serializable {
    public static final String ROLE_DONOR = "DONOR";
    public static final String ROLE_NGO = "NGO_RECEIVER";

    private String uid;
    private String name;
    private String email;
    private String phone;
    private String role;
    private long createdAt;

    // Required default empty constructor for Firebase Firestore
    public User() {
    }

    public User(String uid, String name, String email, String phone, String role, long createdAt) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDonor() {
        return ROLE_DONOR.equalsIgnoreCase(role);
    }

    public boolean isNgo() {
        return ROLE_NGO.equalsIgnoreCase(role);
    }
}

