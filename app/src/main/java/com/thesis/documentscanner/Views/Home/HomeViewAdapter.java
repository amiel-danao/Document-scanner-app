package com.thesis.documentscanner.Views.Home;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.thesis.documentscanner.Models.Employee;
import com.thesis.documentscanner.R;


import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeViewAdapter extends RecyclerView.Adapter<HomeViewAdapter.MyViewHolder> {

    private static final String TAG = "RecyclerviewAdapter";

    Context context;

    ArrayList<Employee> list;

    public HomeViewAdapter(Context context, ArrayList<Employee> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.home_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        final Employee employee = list.get(position);
        holder.Name.setText(employee.getName());
        holder.Role.setText(employee.getRole());
        Glide.with(context)
                 .asBitmap()
                 .load(employee.getAvatar())
                 .into(holder.Avatar);
        holder.parent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra("image_url", employee.getAvatar());
        intent.putExtra("name_url", employee.getName());
        intent.putExtra("role_url", employee.getRole());
        intent.putExtra("UID", employee.getUID());
        context.startActivity(intent);

                            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView Name, Role;
        CircleImageView Avatar;
        CardView parent_layout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            Name = itemView.findViewById(R.id.name);
            Role = itemView.findViewById(R.id.role);
            Avatar = itemView.findViewById(R.id.avatar);
            parent_layout = itemView.findViewById(R.id.parent_layout);
        }


    }

}