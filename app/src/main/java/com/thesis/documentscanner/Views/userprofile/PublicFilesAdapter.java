package com.thesis.documentscanner.Views.userprofile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thesis.documentscanner.Models.File;
import com.thesis.documentscanner.R;

import java.util.ArrayList;
public class PublicFilesAdapter extends RecyclerView.Adapter<PublicFilesAdapter.ViewHolder>{
    private static final String TAG = "PrivateRepoAdapter";

    private final ArrayList<File> files;
    private final Context mContext;


    public PublicFilesAdapter(ArrayList<File> files, Context mContext) {
        this.files = files;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public PublicFilesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.private_repo_item, parent, false);
        PublicFilesAdapter.ViewHolder vh = new PublicFilesAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        File file = files.get(viewHolder.getBindingAdapterPosition());
        viewHolder.username.setText(file.getName());

        viewHolder.parentLayout.setOnClickListener(v -> {
            Log.d(TAG, "onClick: clicked on: " + file.getName());
            Toast.makeText(mContext, "File is opening", Toast.LENGTH_SHORT).show();
            Intent implicit = new Intent(Intent.ACTION_VIEW, Uri.parse(file.getFileurl()));
            implicit.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(implicit);
        });
    }


    @Override
    public int getItemCount() {
        return files.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        LinearLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.folder_name);
            parentLayout = itemView.findViewById(R.id.private_repo_item_layout);
        }
    }


}
