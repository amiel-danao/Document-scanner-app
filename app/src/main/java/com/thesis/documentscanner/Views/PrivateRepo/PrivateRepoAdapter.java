package com.thesis.documentscanner.Views.PrivateRepo;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.thesis.documentscanner.Models.File;
import com.thesis.documentscanner.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.DownloadManager;

public class PrivateRepoAdapter extends RecyclerView.Adapter {
    private static final String TAG = "PrivateRepoAdapter";
    private final SimpleDateFormat formatter;

    private ArrayList<File> files;
    private Context mContext;


    public PrivateRepoAdapter(ArrayList<File> files, Context mContext) {
        this.files = files;
        this.mContext = mContext;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.ENGLISH);
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

        Date localDateTime = file.getDateUploaded().toDate();

        String formattedDateTime = formatter.format(localDateTime);
        fileViewHolder.dateUpload.setText("Uploaded: " + formattedDateTime);

        Glide.with(mContext).load(file.getQrUrl()).into(fileViewHolder.qrImageView);

        fileViewHolder.qrImageView.setTag(file);
        fileViewHolder.qrImageView.setOnClickListener(enlargeQRClick);

        fileViewHolder.downloadButton.setTag(file);
        fileViewHolder.downloadButton.setOnClickListener(downloadClickListener);
    }

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

            folder_name.setText(file.getName());
            downloadIcon.setTag(file);
            downloadIcon.setOnClickListener(downloadClickListener);

            // Load the image using Glide into the enlarged ImageView
            Glide.with(mContext)
                    .load(file.getQrUrl())
                    .into(imageViewEnlarged);

            // Create and show the AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setView(dialogView)
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alertDialog = builder.create();

// Set a different text color for the positive button
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    positiveButton.setTextColor(ContextCompat.getColor(mContext, android.R.color.black));
                }
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
        TextView fileName, fileType, visibility, sender, dateUpload;
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
        }
    }


}
