package com.example.fooddonationconnector.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddonationconnector.R;
import com.example.fooddonationconnector.databinding.ItemDonationBinding;
import com.example.fooddonationconnector.models.Donation;
import com.example.fooddonationconnector.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.DonationViewHolder> {

    public interface OnDonationClickListener {
        void onDonationClick(Donation donation);
    }

    private final Context context;
    private List<Donation> donationList;
    private final OnDonationClickListener listener;

    public DonationAdapter(Context context, List<Donation> donationList, OnDonationClickListener listener) {
        this.context = context;
        this.donationList = donationList != null ? donationList : new ArrayList<>();
        this.listener = listener;
    }

    public void updateList(List<Donation> newList) {
        this.donationList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DonationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDonationBinding binding = ItemDonationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DonationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DonationViewHolder holder, int position) {
        Donation donation = donationList.get(position);
        holder.bind(donation);
    }

    @Override
    public int getItemCount() {
        return donationList.size();
    }

    class DonationViewHolder extends RecyclerView.ViewHolder {
        private final ItemDonationBinding binding;

        public DonationViewHolder(ItemDonationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Donation donation) {
            binding.tvItemFoodTitle.setText(donation.getFoodType());
            binding.tvItemQuantity.setText(String.format("Quantity: %s", donation.getQuantity()));
            binding.tvItemExpiry.setText(String.format("Expires: %s", donation.getExpiryTime()));
            binding.tvItemAddress.setText(donation.getPickupAddress());

            String donorText = (donation.getDonorName() != null && !donation.getDonorName().isEmpty())
                    ? "Posted by: " + donation.getDonorName()
                    : "Posted by: Anonymous Donor";
            binding.tvItemDonor.setText(donorText);

            // Configure status chip
            String status = donation.getStatus() != null ? donation.getStatus() : Donation.STATUS_AVAILABLE;
            binding.tvItemStatus.setText(status);

            if (Donation.STATUS_AVAILABLE.equalsIgnoreCase(status)) {
                binding.tvItemStatus.setBackgroundResource(R.drawable.bg_status_available);
                binding.tvItemStatus.setTextColor(ContextCompat.getColor(context, R.color.status_available_text));
            } else if (Donation.STATUS_CLAIMED.equalsIgnoreCase(status)) {
                binding.tvItemStatus.setBackgroundResource(R.drawable.bg_status_claimed);
                binding.tvItemStatus.setTextColor(ContextCompat.getColor(context, R.color.status_claimed_text));
            } else {
                binding.tvItemStatus.setBackgroundResource(R.drawable.bg_status_completed);
                binding.tvItemStatus.setTextColor(ContextCompat.getColor(context, R.color.status_completed_text));
            }

            // Load food photo
            ImageUtils.loadPhoto(context, donation.getPhotoData(), binding.ivItemFoodImage);

            // Handle card click
            binding.cardDonationItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDonationClick(donation);
                }
            });
        }
    }
}

