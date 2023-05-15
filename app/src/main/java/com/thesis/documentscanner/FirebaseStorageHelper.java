package com.thesis.documentscanner;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.ByteArrayOutputStream;

public class FirebaseStorageHelper {

    public static void uploadBitmapToFirebaseStorage(Bitmap bitmap, String fileName, String folder, final OnCompleteListener<Uri> onCompleteListener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a reference to the image file
        StorageReference imageRef = storageRef.child(folder + "/" + fileName + ".jpg");

        // Compress the bitmap into a ByteArrayOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Upload the image to Firebase Storage
        imageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL of the uploaded image
                    imageRef.getDownloadUrl().addOnCompleteListener(onCompleteListener);
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occurred during the upload
                    e.printStackTrace();
                });
    }
}
