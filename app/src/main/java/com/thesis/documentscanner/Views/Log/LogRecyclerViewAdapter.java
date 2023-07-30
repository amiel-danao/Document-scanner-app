package com.thesis.documentscanner.Views.Log;

import static com.thesis.documentscanner.common.Constants.USERS_COLLECTION;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.thesis.documentscanner.Models.Employee;
import com.thesis.documentscanner.Models.Log;
import com.thesis.documentscanner.databinding.FragmentLogBinding;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LogRecyclerViewAdapter extends RecyclerView.Adapter<LogRecyclerViewAdapter.ViewHolder> {

    private final List<Log> mValues;
    private final boolean isAdmin;
    private final FirebaseFirestore database;
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);

    public LogRecyclerViewAdapter(List<Log> items, boolean isAdmin) {
        mValues = items;
        this.isAdmin = isAdmin;
        database = FirebaseFirestore.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentLogBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mLogDate.setText(dateTimeFormat.format(mValues.get(position).getDate()));
        if(isAdmin) {
            holder.mLogUser.setVisibility(View.VISIBLE);
            String userName = holder.mItem.getUser();
            if(userName == null || userName.isEmpty()) {
                FirebaseFirestore.getInstance().collection(USERS_COLLECTION).document(holder.mItem.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            Employee employee = task.getResult().toObject(Employee.class);
                            holder.mLogUser.setText(employee.getName());
                        }
                    });
            }
            else{
                holder.mLogUser.setText(userName);
            }
        }
        else{
            holder.mLogUser.setVisibility(View.GONE);
        }
        holder.mLogMessage.setText(mValues.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mLogDate;
        public final TextView mLogMessage;
        public final TextView mLogUser;
        public Log mItem;

        public ViewHolder(FragmentLogBinding binding) {
            super(binding.getRoot());
            mLogUser = binding.logUser;
            mLogDate = binding.logDate;
            mLogMessage = binding.logMessage;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mLogMessage.getText() + "'";
        }
    }
}