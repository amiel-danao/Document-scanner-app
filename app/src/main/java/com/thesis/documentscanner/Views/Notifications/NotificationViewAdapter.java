package com.thesis.documentscanner.Views.Notifications;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thesis.documentscanner.R;

import java.util.ArrayList;


public class NotificationViewAdapter extends RecyclerView.Adapter<NotificationViewAdapter.ViewHolder>{

    private static final String TAG = "NotificationViewAdapter";

    private ArrayList<String> mNotificationNames;
    private ArrayList<String> mNotificationDetails;
    private Context mContext;

    public NotificationViewAdapter(ArrayList<String> mNotificationNames, ArrayList<String> mNotificationDetails, Context mContext) {
        this.mNotificationNames = mNotificationNames;
        this.mNotificationDetails = mNotificationDetails;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {

        viewHolder.NotificationName.setText(mNotificationNames.get(position));
        viewHolder.NotificationDetails.setText(mNotificationDetails.get(position));

        viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked on: " + mNotificationNames.get(position));
                Toast.makeText(mContext, mNotificationNames.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNotificationNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView NotificationName;
        TextView NotificationDetails;
        RelativeLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            NotificationName = itemView.findViewById(R.id.NotificationName);
            NotificationDetails = itemView.findViewById(R.id.NotificationDetails);
            parentLayout = itemView.findViewById(R.id.notification_item_layout);
        }
    }


}
