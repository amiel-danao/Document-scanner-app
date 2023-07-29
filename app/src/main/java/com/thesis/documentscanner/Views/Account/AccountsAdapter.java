package com.thesis.documentscanner.Views.Account;

import static com.thesis.documentscanner.common.Constants.USERS_COLLECTION;
import static com.thesis.documentscanner.util.LogUtils.writeLog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.thesis.documentscanner.Models.Employee;
import com.thesis.documentscanner.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.MyViewHolder> {
    private final FirebaseAuth auth;
    private final AccountsFragment fragment;
    Context context;
    List<Employee> list;
    private final FirebaseFunctions mFunctions;
    private AlertDialog alertDialog;
    public AccountsAdapter(Context context, List<Employee> list, AccountsFragment fragment) {
        this.context = context;
        this.list = list;
        this.fragment = fragment;
        auth = FirebaseAuth.getInstance();
// ...
        mFunctions = FirebaseFunctions.getInstance();
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

        holder.parent_layout.setOnClickListener(v -> {
            Employee employee1 = (Employee) v.getTag();
            fragment.showEditAccount(employee1);
        });

        if(employee.getUID().equals(auth.getCurrentUser().getUid())) {
            holder.statusSwitch.setVisibility(View.INVISIBLE);
            holder.deleteAccount.setVisibility(View.INVISIBLE);
        }
        else {
            holder.deleteAccount.setTag(employee);
            holder.deleteAccount.setOnClickListener(v -> {
                Employee employee1 = (Employee) v.getTag();
                showConfirmDeleteAccount(employee1);
            });
            holder.deleteAccount.setVisibility(View.VISIBLE);

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

    private void showConfirmDeleteAccount(Employee employee) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = fragment.requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        builder.setView(dialogView);
        builder.setTitle("Confirm Account Deletion");
        builder.setMessage("Are you sure you want to delete this account? This action cannot be undone.");

        Button btnPositive = dialogView.findViewById(R.id.btn_positive);
        btnPositive.setText("Yes");
        btnPositive.setOnClickListener(v -> {
            deleteUser(employee, alertDialog);
        });

        Button btnNegative = dialogView.findViewById(R.id.btn_negative);
        btnNegative.setText("No");
        btnNegative.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    private Task<String> deleteUser(Employee employee, DialogInterface dialog) {
        // Create the arguments to the callable function.
        String uid = employee.getUID();
        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);

        return mFunctions
                .getHttpsCallable("deleteUser")
                .call(data)
                .continueWith(task -> {
                    // This continuation runs on either success or failure, but if the task
                    // has failed then getResult() will throw an Exception which will be
                    // propagated down.
                    int index = list.indexOf(employee);
                    list.remove(index);
                    notifyItemRemoved(index);
                    dialog.dismiss();
                    String result = (String) task.getResult().getData();

                    FirebaseFirestore.getInstance().collection(USERS_COLLECTION).document(uid).delete().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(context, "Account was deleted successfully", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                    return result;

                });
    }

    private void changeAccountStatus(String uid, String status, String email) {

        DocumentReference userDocumentRef = FirebaseFirestore.getInstance().collection(USERS_COLLECTION)
                .document(uid);
        userDocumentRef.update("status", status)
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Account status was changed", Toast.LENGTH_LONG).show();
                String logMessage = String.format("Account status changed to %s: %s", status, email);
                writeLog(logMessage, auth.getCurrentUser().getDisplayName(), auth.getCurrentUser().getUid());
            } else {
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
        ImageButton deleteAccount;
        Switch statusSwitch;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            employeeName = itemView.findViewById(R.id.name);
            role = itemView.findViewById(R.id.txtSender);
            email = itemView.findViewById(R.id.email);
            Avatar = itemView.findViewById(R.id.avatar);
            statusSwitch = itemView.findViewById(R.id.statusSwitch);
            parent_layout = itemView.findViewById(R.id.parent_layout);
            deleteAccount = itemView.findViewById(R.id.deleteAccount);
        }
    }

}