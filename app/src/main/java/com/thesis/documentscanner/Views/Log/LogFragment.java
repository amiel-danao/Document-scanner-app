package com.thesis.documentscanner.Views.Log;

import static com.thesis.documentscanner.common.Constants.USERS_COLLECTION;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.thesis.documentscanner.Models.Log;
import com.thesis.documentscanner.R;
import com.thesis.documentscanner.common.RequireLoginFragment;

import java.util.ArrayList;
import java.util.List;

public class LogFragment extends RequireLoginFragment {

    private RecyclerView recyclerView;
    private Context context;
    List<Log> logs = new ArrayList<>();
    private LogRecyclerViewAdapter adapter;

    public LogFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            context = view.getContext();
            recyclerView = (RecyclerView) view;
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set the title for the ActionBar (AppCompatActivity)
        String customTitle = "Logs";
        requireActivity().setTitle(customTitle);
    }

    @Override
    public void onFetchedUser() {
        super.onFetchedUser();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        boolean isAdmin = loggedInUser.getRole().equals("Staff");

        Query query;
        if(isAdmin){
            query = FirebaseFirestore.getInstance().collection("Logs").orderBy("date", Query.Direction.DESCENDING);
        }
        else{
            DocumentReference userRef = FirebaseFirestore.getInstance().collection(USERS_COLLECTION).document(loggedInUser.getUID());
            query = FirebaseFirestore.getInstance().collection("Logs").whereEqualTo("user", userRef).orderBy("date", Query.Direction.DESCENDING);
        }

        adapter = new LogRecyclerViewAdapter(logs, isAdmin);
        recyclerView.setAdapter(adapter);



        query.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                int count = logs.size();
                logs.clear();
                adapter.notifyItemRangeRemoved(0, count);

                List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                for (DocumentSnapshot document : documentSnapshots) {
                    Log log = document.toObject(Log.class);
                    logs.add(log);
                    adapter.notifyItemInserted(logs.size()-1);
                }
            }
            else{
                task.getException().printStackTrace();
                Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}