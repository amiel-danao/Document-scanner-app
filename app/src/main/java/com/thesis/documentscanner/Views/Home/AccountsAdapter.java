package com.thesis.documentscanner.Views.Home;

import static com.thesis.documentscanner.common.Constants.USERS_COLLECTION;
import static com.thesis.documentscanner.util.LogUtils.writeLog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thesis.documentscanner.Models.Employee;
import com.thesis.documentscanner.R;

import java.util.List;

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.MyViewHolder> {
    private final FirebaseAuth auth;

    Context context;

    List<Employee> list;

    public AccountsAdapter(Context context, List<Employee> list) {
        this.context = context;
        this.list = list;
        auth = FirebaseAuth.getInstance();
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

        holder.statusSwitch.setChecked(employee.getStatus() == null || employee.getStatus().equals("active"));
        holder.parent_layout.setTag(employee);

        if(employee.getUID().equals(auth.getCurrentUser().getUid())) {
            holder.statusSwitch.setVisibility(View.GONE);
        }
        else {
            holder.statusSwitch.setVisibility(View.VISIBLE);
            holder.statusSwitch.setTag(employee);
            holder.statusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    changeAccountStatus(employee.getUID(), "active", employee.getEmail());
                } else {
                    changeAccountStatus(employee.getUID(), "inactive", employee.getEmail());
                }
            });
        }
    }

    private void changeAccountStatus(String uid, String status, String email) {

        DocumentReference userDocumentRef = FirebaseFirestore.getInstance().collection(USERS_COLLECTION)
                .document(uid);
        userDocumentRef.update("status", status)
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Account status was changed", Toast.LENGTH_LONG).show();
                String logMessage = String.format("Account status changed to %s: %s", status, email);
                writeLog(logMessage, auth.getCurrentUser().getUid());
            } else {
                // User status update failed
                // TODO: Handle the deactivation failure case here
                Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView employeeName, role, email;
        ImageView Avatar;
        CardView parent_layout;
        Switch statusSwitch;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            employeeName = itemView.findViewById(R.id.name);
            role = itemView.findViewById(R.id.txtSender);
            email = itemView.findViewById(R.id.email);
            Avatar = itemView.findViewById(R.id.avatar);
            statusSwitch = itemView.findViewById(R.id.statusSwitch);
            parent_layout = itemView.findViewById(R.id.parent_layout);
        }
    }

}