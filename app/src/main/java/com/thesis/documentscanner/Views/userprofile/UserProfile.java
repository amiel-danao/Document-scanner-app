package com.thesis.documentscanner.Views.userprofile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.thesis.documentscanner.FirebaseStorageHelper;
import com.thesis.documentscanner.MainActivity;
import com.thesis.documentscanner.Models.Employee;
import com.thesis.documentscanner.Models.File;
import com.thesis.documentscanner.QRGenerator;
import com.thesis.documentscanner.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class UserProfile extends AppCompatActivity {
    private String UID;
    private FirebaseAuth auth;

    private static final String  TAG = "UserProfileActivity";
    private View loading;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Intent> appSettingsLauncher;
    private final String[] allowedFileExtensions = {"text/csv", "text/comma-separated-values", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint", "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "image/x-ms-bmp", "image/jpeg", "image/png",
            "text/plain", "text/rtf", "application/pdf"
    };
    private EditText fileNameEdit;
    private AlertDialog.Builder dialog;
    private Employee profile;
    private TextView dateEdit;
    private SimpleDateFormat simpleDateTimeFormat;
    private SwitchCompat visibilitySwitch;
    private TextView fileTypeEdit;
    private EditText editStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        UID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() == null){
            startActivity(new Intent(this, MainActivity.class));
        }

        simpleDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        FloatingActionButton fab = findViewById(R.id.fab);
        View form = findViewById(R.id.form);
        loading = findViewById(R.id.loading);
        fileNameEdit = findViewById(R.id.fileNameEdit);
        fileTypeEdit = findViewById(R.id.fileTypeEdit);
        dateEdit = findViewById(R.id.dateEdit);
        visibilitySwitch = findViewById(R.id.visibilitySwitch);
        editStatus = findViewById(R.id.editStatus);

        setupFilePicker();
        setupPermissionLauncher();

        dialog = new AlertDialog.Builder(this);
        String currentDateTimeString = simpleDateTimeFormat.format(new Date());
        dateEdit.setText(currentDateTimeString);

        fab.setOnClickListener(view -> {
            if(isFormValid()) {
                loading.setVisibility(View.VISIBLE);
                requestStoragePermission();
            }
            else{
                dialog.setTitle("Invalid").setMessage("Invalid form data, please fill up required fields!")
                        .show();
            }
        });

        visibilitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    visibilitySwitch.setText("Visibility: Public");
                }
                else{
                    visibilitySwitch.setText("Visibility: Private");
                }
            }
        });

        //get profile
        FirebaseFirestore.getInstance().collection("Users").document(auth.getCurrentUser().getUid())
        .get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                profile = task.getResult().toObject(Employee.class);
            }
            else{
                String error = task.getException().getMessage();
                Toast.makeText(getApplicationContext(), "No profile data!\n" + error, Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        appSettingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Check if the user has returned from app settings
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        // Permission granted, proceed with file picking
                        if (filePickerLauncher != null) {
                            pickFile();
                        }
                    } else {
                        // Permission still denied, handle accordingly
                        // You may display a message or take appropriate action
                        showCancelledMessage();
                    }
                }
            });
    }

    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    // Permission granted, proceed with file picking
                    if (filePickerLauncher != null) {
                        pickFile();
                    }
                } else {
                    // Permission denied, handle accordingly
                    // You may display a message or take appropriate action
                    showPermissionDeniedDialog();
                }
            });
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, proceed with file picking
            pickFile();
        } else {
            // Permission not granted, request it
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Denied")
            .setMessage("This permission is required to access files. Please grant the permission in the app settings.")
            .setPositiveButton("Go to Settings", (dialog, which) -> {
                // Open app settings
                openAppSettings();
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                // Permission denied, handle accordingly
                showCancelledMessage();
            })
            .setCancelable(false)
            .show();
    }

    private void showCancelledMessage(){
        // You may display a message or take appropriate action
        AlertDialog.Builder cancelledDialog = new AlertDialog.Builder(this);
        cancelledDialog.setTitle("Permission Denied").setMessage("In order to use the upload functionality you need to allow storage permission \n You can do it in the app settings")
        .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        appSettingsLauncher.launch(intent);
    }

    private boolean isFormValid(){
        if(fileNameEdit.getText().toString().trim().isEmpty()){
            fileNameEdit.setError("This field is required");
            return false;
        }

        if(editStatus.getText().toString().isEmpty()){
            editStatus.setError("This field is required");
            return false;
        }
        return true;
    }

    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {

                            Uri fileUri = data.getData();

                            String mimeType = getContentResolver().getType(fileUri);

                            boolean isValidFileType = false;
                            for (String allowedMimeType : allowedFileExtensions) {
                                if (mimeType != null && mimeType.equals(allowedMimeType)) {
                                    isValidFileType = true;
                                    break;
                                }
                            }

                            if (isValidFileType) {
                                // File is valid, proceed with handling it
                                // Handle the selected file URI here
                                String fileName = fileNameEdit.getText().toString();
                                String fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);

                                fileTypeEdit.setText(fileExtension);

                                String folder = "qr/" + auth.getCurrentUser().getUid();
                                StorageReference folderRef = FirebaseStorage.getInstance().getReference().child(folder);
                                final StorageReference fileRef = folderRef.child(fileName + "." + fileExtension);
                                fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {

                                    Task<Uri> downloadUrlTask = fileRef.getDownloadUrl();

                                    downloadUrlTask.addOnFailureListener(e -> loading.setVisibility(View.GONE));

                                    downloadUrlTask.addOnSuccessListener(uriTask -> {
                                        String URL = String.valueOf(uriTask);
                                        Date parsedDate = new Date();

                                        try {
                                            // Parse the date-time string
                                            parsedDate = simpleDateTimeFormat.parse(dateEdit.getText().toString());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                            return;
                                        }

                                        final Timestamp timestamp = new Timestamp(parsedDate);

                                        String visibility = visibilitySwitch.isChecked()? "public" : "private";


                                        Bitmap qrBitmap = QRGenerator.generateQRCode(URL);

                                        FirebaseStorageHelper.uploadBitmapToFirebaseStorage(qrBitmap, fileName, folder, storageTask -> {
                                            if(storageTask.isSuccessful()){
                                                Uri downloadUri = storageTask.getResult();
                                                String qrUrl = downloadUri.toString();
                                                DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Files").document();
                                                // Use the image URL as needed (e.g., save it to a database)
                                                File file = new File(documentReference.getId(), URL, qrUrl, fileName, fileExtension, visibility, profile.getName(), timestamp, UID);
                                                file.setStatus(editStatus.getText().toString());


                                                documentReference.set(file, SetOptions.merge()).addOnCompleteListener(dbTask -> {
                                                    loading.setVisibility(View.GONE);
                                                    if (dbTask.isSuccessful()) {
                                                        Toast.makeText(UserProfile.this, "File Uploaded", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    } else {
                                                        Toast.makeText(UserProfile.this, dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                        Log.d(TAG, dbTask.getException().getMessage());
                                                    }
                                                });
                                            }
                                            else{
                                                loading.setVisibility(View.GONE);
                                            }
                                        });
                                    });
                                });
                            } else {
                                // File is not a valid type, display a message to the user
                                loading.setVisibility(View.GONE);
                                dialog.setTitle("Invalid").setMessage("Invalid file type selected!")
                                        .show();
                            }
                        }
                    }
                    else if (result.getResultCode() == RESULT_CANCELED){
                        loading.setVisibility(View.GONE);
                    }
                });
    }

    private void pickFile() {
        if(filePickerLauncher == null){
            return;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        filePickerLauncher.launch(intent);
    }


}