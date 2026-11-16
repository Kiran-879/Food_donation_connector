package com.example.fooddonationconnector.models;

import java.io.Serializable;

public class Donation implements Serializable {
    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_CLAIMED = "CLAIMED";
    public static final String STATUS_COMPLETED = "COMPLETED";

    private String donationId;
    private String foodType;
    private String quantity;
    private String expiryTime;
    private String pickupAddress;
    private String additionalNotes;
    private String photoData; // Can store Base64 encoded string or image URL
    private String donorId;
    private String donorName;
    private String donorPhone;
    private String receiverId;
    private String receiverName;
    private String status;
    private long createdAt;

    // Required default empty constructor for Firebase Firestore deserialization
    public Donation() {
    }

    public Donation(String donationId, String foodType, String quantity, String expiryTime,
                    String pickupAddress, String additionalNotes, String photoData,
                    String donorId, String donorName, String donorPhone,
                    String receiverId, String receiverName, String status, long createdAt) {
        this.donationId = donationId;
        this.foodType = foodType;
        this.quantity = quantity;
        this.expiryTime = expiryTime;
        this.pickupAddress = pickupAddress;
        this.additionalNotes = additionalNotes;
        this.photoData = photoData;
        this.donorId = donorId;
        this.donorName = donorName;
        this.donorPhone = donorPhone;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getDonationId() {
        return donationId;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(String expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public String getPhotoData() {
        return photoData;
    }

    public void setPhotoData(String photoData) {
        this.photoData = photoData;
    }

    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        this.donorId = donorId;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getDonorPhone() {
        return donorPhone;
    }

    public void setDonorPhone(String donorPhone) {
        this.donorPhone = donorPhone;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAvailable() {
        return STATUS_AVAILABLE.equalsIgnoreCase(status);
    }

    public boolean isClaimed() {
        return STATUS_CLAIMED.equalsIgnoreCase(status);
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equalsIgnoreCase(status);
    }
}

