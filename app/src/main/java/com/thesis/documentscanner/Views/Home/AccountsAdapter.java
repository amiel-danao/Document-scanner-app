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
import com.thesis.documentscanner.Models.Employee;
import com.thesis.documentscanner.Models.File;
import com.thesis.documentscanner.R;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.MyViewHolder> {

    private static final String TAG = "RecyclerviewAdapter";
    private final SimpleDateFormat formatter;

    Context context;

    List<Employee> list;

    public AccountsAdapter(Context context, List<Employee> list) {
        this.context = context;
        this.list = list;
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a", Locale.ENGLISH);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.account_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        final Employee employee = list.get(position);
        holder.employeeName.setText(employee.getName());
        holder.role.setText(employee.getRole());
        holder.email.setText(employee.getEmail());

//        Glide.with(context)
//                .asBitmap()
//                .load(file.getQrUrl())
//                .into(holder.Avatar);

        holder.parent_layout.setTag(employee);
    }


    final View.OnClickListener enlargeQRClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Employee employee = (Employee)v.getTag();

        }
    };

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView employeeName, role, email;
        ImageView Avatar;
        CardView parent_layout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            employeeName = itemView.findViewById(R.id.name);
            role = itemView.findViewById(R.id.txtSender);
            email = itemView.findViewById(R.id.email);
            Avatar = itemView.findViewById(R.id.avatar);
            parent_layout = itemView.findViewById(R.id.parent_layout);
        }
    }

}