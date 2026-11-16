package com.example.fooddonationconnector.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.fooddonationconnector.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageUtils {

    /**
     * Resizes a Bitmap and encodes it to a Base64 string for lightweight Firestore storage.
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return "";

        int maxWidth = 640;
        int maxHeight = 480;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;
        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, Math.max(1, finalWidth), Math.max(1, finalHeight), true);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    /**
     * Converts an image Uri from the phone into a Base64 string.
     */
    public static String uriToBase64(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }
            return bitmapToBase64(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Loads a photo into an ImageView: decodes Base64 if present, or uses Glide for URL, or shows fallback.
     */
    public static void loadPhoto(Context context, String photoData, ImageView imageView) {
        if (photoData == null || photoData.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.ic_food_placeholder);
            return;
        }

        if (photoData.startsWith("http://") || photoData.startsWith("https://")) {
            Glide.with(context)
                    .load(photoData)
                    .placeholder(R.drawable.ic_food_placeholder)
                    .error(R.drawable.ic_food_placeholder)
                    .into(imageView);
        } else {
            try {
                byte[] decodedString = Base64.decode(photoData, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedByte != null) {
                    imageView.setImageBitmap(decodedByte);
                } else {
                    imageView.setImageResource(R.drawable.ic_food_placeholder);
                }
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.ic_food_placeholder);
            }
        }
    }
}

