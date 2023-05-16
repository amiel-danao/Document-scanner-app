package com.thesis.documentscanner.Views.Home;


import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.thesis.documentscanner.Models.File;
import com.thesis.documentscanner.R;


import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeViewAdapter extends RecyclerView.Adapter<HomeViewAdapter.MyViewHolder> {

    private static final String TAG = "RecyclerviewAdapter";
    private final SimpleDateFormat formatter;

    Context context;

    List<File> list;

    public HomeViewAdapter(Context context, List<File> list) {
        this.context = context;
        this.list = list;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.ENGLISH);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.home_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        final File file = list.get(position);
        holder.Name.setText(file.getName());
        holder.sender.setText("Sender: "+file.getSender());
        Date localDateTime = file.getDateUploaded().toDate();

// Format the LocalDateTime object to a readable date and time string
        String formattedDateTime = formatter.format(localDateTime);
        holder.timestamp.setText("Uploaded: " + formattedDateTime);
        Glide.with(context)
                 .asBitmap()
                 .load(file.getQrUrl())
                 .into(holder.Avatar);

        holder.parent_layout.setTag(file);
        holder.parent_layout.setOnClickListener(enlargeQRClick);
    }

    final View.OnClickListener downloadClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            File file = (File)v.getTag();
            Toast.makeText(context, "File is opening", Toast.LENGTH_SHORT).show();
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
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
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_image_enlarged, null);
            ImageView imageViewEnlarged = dialogView.findViewById(R.id.image_enlarged);
            TextView folder_name = dialogView.findViewById(R.id.folder_name);
            ImageView downloadIcon = dialogView.findViewById(R.id.downloadIcon);

            folder_name.setText(file.getName());
            downloadIcon.setTag(file);
            downloadIcon.setOnClickListener(downloadClickListener);

            // Load the image using Glide into the enlarged ImageView
            Glide.with(context)
                    .load(file.getQrUrl())
                    .into(imageViewEnlarged);

            // Create and show the AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
                    positiveButton.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                }
            });

// Show the AlertDialog
            alertDialog.show();
        }
    };

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView timestamp;
        TextView Name, sender;
        ImageView Avatar;
        CardView parent_layout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            Name = itemView.findViewById(R.id.name);
            sender = itemView.findViewById(R.id.txtSender);
            timestamp = itemView.findViewById(R.id.timestamp);
            Avatar = itemView.findViewById(R.id.avatar);
            parent_layout = itemView.findViewById(R.id.parent_layout);
        }


    }

}