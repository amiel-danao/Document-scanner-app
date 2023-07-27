package com.thesis.documentscanner.Views.userprofile;

import static com.thesis.documentscanner.common.Constants.USERS_COLLECTION;
import static com.thesis.documentscanner.util.AddImageInExcel.attachImageToExcel;
import static com.thesis.documentscanner.util.AddImageToDocx.addImageToDocx;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.thesis.documentscanner.FirebaseStorageHelper;
import com.thesis.documentscanner.MainActivity;
import com.thesis.documentscanner.Models.Employee;
import com.thesis.documentscanner.Models.File;
import com.thesis.documentscanner.QRGenerator;
import com.thesis.documentscanner.R;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;


public class UserProfile extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 69;
    private String UID;
    private FirebaseAuth auth;

    private static final String  TAG = "UserProfileActivity";
    private View loading;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private ActivityResultLauncher<String[]> permissionsLauncher;
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
    AlertDialog alertDialog;
    private ArrayList<String> permissionsList;
    private String[] permissionsStr;

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

        dialog = new AlertDialog.Builder(this);
        String currentDateTimeString = simpleDateTimeFormat.format(new Date());
        dateEdit.setText(currentDateTimeString);

        visibilitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
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

        permissionsStr = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE};

        permissionsList = new ArrayList<>();
        permissionsList.addAll(Arrays.asList(permissionsStr));

        fab.setOnClickListener(view -> {
            if(isFormValid()) {
                loading.setVisibility(View.VISIBLE);
                pickFile();
            }
            else{
                dialog.setTitle("Invalid").setMessage("Invalid form data, please fill up required fields!")
                        .show();
            }
        });
    }

    private void showPermissionDialog() {

        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.custom_dialog, null);
            builder.setView(dialogView);

            Button btnPositive = dialogView.findViewById(R.id.btn_positive);
            btnPositive.setText("Settings");
            btnPositive.setOnClickListener(v -> {
                alertDialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            });

            builder.setTitle("Permission required")
                    .setMessage("Some permissions are need to be allowed to use this app without any problems.");
            alertDialog = builder.create();
        }

        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }
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
                                try(Cursor returnCursor =
                                        getContentResolver().query(fileUri, null, null, null, null)) {
                                    int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                                    returnCursor.moveToFirst();
                                    int fileSize = (int) returnCursor.getLong(sizeIndex);

                                    if (fileSize <= 0) {
                                        Toast.makeText(this, "Empty file is not allowed!", Toast.LENGTH_LONG).show();
                                        loading.setVisibility(View.GONE);
                                        return;
                                    }
                                }
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
                                        Date parsedDate;

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

                                        String tempFileName = String.format("temp.%s", fileExtension); // Replace this with the desired file name.
                                        java.io.File internalFile = new java.io.File(getFilesDir(), tempFileName);

                                        try {
                                            internalFile.createNewFile();

                                            fileRef.getFile(internalFile).addOnSuccessListener(taskSnapshot1 -> {
                                                assert qrBitmap != null;
                                                // File download success
                                                java.io.File modifiedFile = null;
                                                if(fileExtension.equals("xls") || fileExtension.equals("xlsx"))
                                                    modifiedFile = attachImageToExcel(getApplicationContext(), new java.io.File(internalFile.getPath()), qrBitmap);
                                                else if (fileExtension.equals("doc") || fileExtension.equals("docx")) {
                                                    modifiedFile = addImageToDocx(getApplicationContext(), internalFile, qrBitmap);
                                                }

                                                if(modifiedFile != null) {
                                                    Uri modifiedUri = Uri.fromFile(modifiedFile);

                                                    StorageReference existingStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(URL);

                                                    existingStorageReference.putFile(modifiedUri).addOnSuccessListener(taskSnapshot2 -> {
                                                                Toast.makeText(UserProfile.this, "QR was placed successfully", Toast.LENGTH_SHORT).show();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(UserProfile.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                            });
                                                }
                                            }).addOnFailureListener(e -> {
                                                // File download failed
                                                Toast.makeText(UserProfile.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                            });
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                        }

                                        assert(qrBitmap != null);

                                        FirebaseStorageHelper.uploadBitmapToFirebaseStorage(qrBitmap, fileName, folder, storageTask -> {
                                            if (storageTask.isSuccessful()) {
                                                Uri downloadUri = storageTask.getResult();
                                                String qrUrl = downloadUri.toString();
                                                DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Files").document();

                                                // Create a WriteBatch instance
                                                WriteBatch batch = FirebaseFirestore.getInstance().batch();
                                                // Use the image URL as needed (e.g., save it to a database)
                                                File file = new File(documentReference.getId(), URL, qrUrl, fileName, fileExtension, visibility, profile.getName(), timestamp, UID);
                                                file.setStatus(editStatus.getText().toString());

                                                DocumentReference currentUserRef = FirebaseFirestore.getInstance().collection(USERS_COLLECTION).document(auth.getCurrentUser().getUid());
                                                String logMessage = String.format("File uploaded: %s.%s", fileName, fileExtension);
                                                com.thesis.documentscanner.Models.Log log = new com.thesis.documentscanner.Models.Log(new Date(), currentUserRef, logMessage);

                                                DocumentReference newLogReference = FirebaseFirestore.getInstance().collection("Logs").document();
                                                // Add the document to the batch
                                                batch.set(newLogReference, log, SetOptions.merge());
                                                batch.set(documentReference, file, SetOptions.merge());

                                                // Commit the batch to execute all the writes together
                                                batch.commit().addOnCompleteListener(batchTask -> {
                                                    loading.setVisibility(View.GONE);
                                                    if (batchTask.isSuccessful()) {
                                                        Toast.makeText(UserProfile.this, "File Uploaded", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    } else {
                                                        Toast.makeText(UserProfile.this, batchTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                        Log.d(TAG, batchTask.getException().getMessage());
                                                    }
                                                });
                                            } else {
                                                loading.setVisibility(View.GONE);
                                            }
                                        });

//                                        FirebaseStorageHelper.uploadBitmapToFirebaseStorage(qrBitmap, fileName, folder, storageTask -> {
//                                            if(storageTask.isSuccessful()){
//                                                Uri downloadUri = storageTask.getResult();
//                                                String qrUrl = downloadUri.toString();
//                                                DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Files").document();
//                                                // Use the image URL as needed (e.g., save it to a database)
//                                                File file = new File(documentReference.getId(), URL, qrUrl, fileName, fileExtension, visibility, profile.getName(), timestamp, UID);
//                                                file.setStatus(editStatus.getText().toString());
//
//
//                                                documentReference.set(file, SetOptions.merge()).addOnCompleteListener(dbTask -> {
//                                                    loading.setVisibility(View.GONE);
//                                                    if (dbTask.isSuccessful()) {
//                                                        Toast.makeText(UserProfile.this, "File Uploaded", Toast.LENGTH_SHORT).show();
//                                                        finish();
//                                                    } else {
//                                                        Toast.makeText(UserProfile.this, dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
//                                                        Log.d(TAG, dbTask.getException().getMessage());
//                                                    }
//                                                });
//                                            }
//                                            else{
//                                                loading.setVisibility(View.GONE);
//                                            }
//                                        });
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