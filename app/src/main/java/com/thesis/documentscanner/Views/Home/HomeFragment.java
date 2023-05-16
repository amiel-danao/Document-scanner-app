package com.thesis.documentscanner.Views.Home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.thesis.documentscanner.MainActivity;
import com.thesis.documentscanner.Models.Employee;
import com.thesis.documentscanner.Models.File;
import com.thesis.documentscanner.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class HomeFragment extends Fragment {
    private TextView txtWelcome;
    private Employee profile;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private List<File> recentFiles;
    private HomeViewAdapter adapter;
    private RecyclerView recyclerView;
    private TextView recent_files_text_view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        txtWelcome = view.findViewById(R.id.txtWelcome);
        recyclerView = view.findViewById(R.id.recentFilesRecyclerView);
        recent_files_text_view = view.findViewById(R.id.recent_files_text_view);
        recentFiles = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() == null){
            startActivity(new Intent(getActivity(), MainActivity.class));
        }

        FirebaseFirestore.getInstance().collection("Users").document(auth.getCurrentUser().getUid())
        .get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                profile = task.getResult().toObject(Employee.class);
                txtWelcome.setText("Welcome back!\n" + profile.getName());

                getLastTenRecords();
            }
            else{
                String error = task.getException().getMessage();
                Toast.makeText(getContext(), "No profile data!\n" + error, Toast.LENGTH_LONG).show();
                startActivity(new Intent(getContext(), MainActivity.class));
            }
        });



        return view;
    }

    public void getLastTenRecords() {
        if(adapter != null){
            adapter.notifyItemRangeRemoved(0, recentFiles.size());
        }
        recentFiles.clear();
        // Get Firestore instance
        firestore = FirebaseFirestore.getInstance();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endOfDay = calendar.getTime();


        // Create a query to filter documents by timestamp
        Query query = firestore.collection("Files")
                .where(
                    Filter.or(
                            Filter.equalTo("visibility", "public"),
                            Filter.equalTo("sender", profile.getName())
                    )
                )
                .limit(10);

        if (profile == null || profile.getRole().equals("Staff")){
            recent_files_text_view.setText("Recent Files Today");
            query = FirebaseFirestore.getInstance().collection("Files")
                    .whereGreaterThanOrEqualTo("dateUploaded", new Timestamp(startOfDay))
                    .whereLessThanOrEqualTo("dateUploaded", new Timestamp(endOfDay))
                    .orderBy("dateUploaded", Query.Direction.DESCENDING)
                    .limit(10);
        }

        // Execute the query
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();

                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    // Handle the query results (querySnapshot) here
                    // ...
                    File file = document.toObject(File.class);
                    recentFiles.add(file);
                }

                Context context = getActivity();
                adapter = new HomeViewAdapter(context, recentFiles);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                // Handle any errors
                Exception exception = task.getException();
                // ...
            }
        });
    }

    private long getStartOfDayMillis(Date date, int timeZoneOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis() - timeZoneOffset;
    }

}
