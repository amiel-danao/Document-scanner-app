package com.thesis.documentscanner.Views.PrivateRepo;

import static com.thesis.documentscanner.util.LogUtils.writeLog;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.thesis.documentscanner.Models.File;
import com.thesis.documentscanner.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PrivateRepoAdapter extends RecyclerView.Adapter {
    private static final String TAG = "PrivateRepoAdapter";
    private final SimpleDateFormat formatter;
    private final FirebaseAuth auth;

    private ArrayList<File> files;
    private Context mContext;


    public PrivateRepoAdapter(ArrayList<File> files, Context mContext) {
        this.files = files;
        this.mContext = mContext;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.ENGLISH);
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.private_repo_item, parent, false);
        FileViewHolder vh = new FileViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        File file = files.get(holder.getBindingAdapterPosition());
        FileViewHolder fileViewHolder = ((FileViewHolder) holder);
        fileViewHolder.fileName.setText(file.getName());
        fileViewHolder.fileType.setText("Type: "+ file.getFileType());
        fileViewHolder.visibility.setText(file.getVisibility());
        fileViewHolder.sender.setText("Sender: "+ file.getSender());
        fileViewHolder.status.setText("Status: "+ file.getStatus());

        Date localDateTime = file.getDateUploaded().toDate();

        String formattedDateTime = formatter.format(localDateTime);
        fileViewHolder.dateUpload.setText("Uploaded: " + formattedDateTime);

        Glide.with(mContext).load(file.getQrUrl()).into(fileViewHolder.qrImageView);

        fileViewHolder.qrImageView.setTag(file);
        fileViewHolder.qrImageView.setOnClickListener(enlargeQRClick);

        fileViewHolder.downloadButton.setTag(file);
        fileViewHolder.downloadButton.setOnClickListener(downloadClickListener);
    }

    final View.OnClickListener downloadQRClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            File file = (File)view.getTag();
            Toast.makeText(mContext, "QR is downloading", Toast.LENGTH_SHORT).show();
            DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(file.getQrUrl()));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setTitle("QR Download");
            request.setDescription("Downloading qr file...");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.getName() + ".jpg");

            if (downloadManager != null) {
                downloadManager.enqueue(request);
                String logMessage = String.format("QR downloaded: %s", file.getName());
                writeLog(logMessage, auth.getCurrentUser().getDisplayName(), auth.getCurrentUser().getUid());
            }
        }
    };

    final View.OnClickListener downloadClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            File file = (File)v.getTag();
            Toast.makeText(mContext, "File is opening", Toast.LENGTH_SHORT).show();
            DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(file.getFileurl()));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setTitle("File Download");
            request.setDescription("Downloading file...");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.getName() + "." + file.getFileType());

            if (downloadManager != null) {
                downloadManager.enqueue(request);

                String logMessage = String.format("File downloaded: %s", file.getName());
                writeLog(logMessage, auth.getCurrentUser().getDisplayName(), auth.getCurrentUser().getUid());
            }
        }
    };

    final View.OnClickListener enlargeQRClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            File file = (File)v.getTag();
            // Inflate the custom layout for the AlertDialog
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View dialogView = inflater.inflate(R.layout.dialog_image_enlarged, null);
            ImageView imageViewEnlarged = dialogView.findViewById(R.id.image_enlarged);
            TextView folder_name = dialogView.findViewById(R.id.folder_name);
            ImageView downloadIcon = dialogView.findViewById(R.id.downloadIcon);
            ImageButton downloadQRButton = dialogView.findViewById(R.id.btnDownloadQR);

            View statusLayout = dialogView.findViewById(R.id.statusLayout);
            if(file.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                statusLayout.setVisibility(View.VISIBLE);
                EditText editStatus = dialogView.findViewById(R.id.editStatus);

                ImageButton btnSaveStatus = dialogView.findViewById(R.id.btnSaveStatus);
                btnSaveStatus.setOnClickListener(view -> {
                    if(editStatus.getText().toString().isEmpty()){
                        Toast.makeText(mContext, "Please input a status", Toast.LENGTH_SHORT).show();
                        editStatus.setError("Please input a status");
                        return;
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("status", editStatus.getText().toString());
                    FirebaseFirestore.getInstance().collection("Files").document(file.getDocId())
                        .set(data, SetOptions.merge())
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                Toast.makeText(mContext, "Status was updated successfully", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                });
            }
            else{
                statusLayout.setVisibility(View.GONE);
            }

            folder_name.setText(file.getName());
            downloadIcon.setTag(file);
            downloadIcon.setOnClickListener(downloadClickListener);

            downloadQRButton.setTag(file);
            downloadQRButton.setOnClickListener(downloadQRClickListener);

            // Load the image using Glide into the enlarged ImageView
            Glide.with(mContext)
                    .load(file.getQrUrl())
                    .into(imageViewEnlarged);

            // Create and show the AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setView(dialogView)
                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = builder.create();

// Set a different text color for the positive button
            alertDialog.setOnShowListener(dialog -> {
                Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setTextColor(ContextCompat.getColor(mContext, android.R.color.black));
            });

// Show the AlertDialog
            alertDialog.show();
        }
    };


    @Override
    public int getItemCount() {
        return files.size();
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileName, fileType, visibility, sender, dateUpload, status;
        ImageView qrImageView;
        LinearLayout parentLayout;
        View downloadButton;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.folder_name);
            fileType = itemView.findViewById(R.id.fileType);
            visibility = itemView.findViewById(R.id.visibility);
            sender = itemView.findViewById(R.id.sender);
            dateUpload = itemView.findViewById(R.id.dateUpload);

            parentLayout = itemView.findViewById(R.id.private_repo_item_layout);
            qrImageView = itemView.findViewById(R.id.idIVQrcode);
            downloadButton = itemView.findViewById(R.id.downloadIcon);
            status = itemView.findViewById(R.id.status);

        }
    }


}
