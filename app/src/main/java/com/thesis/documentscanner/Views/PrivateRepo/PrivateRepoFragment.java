package com.thesis.documentscanner.Views.PrivateRepo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class PrivateRepoFragment extends Fragment {

    private ArrayList<File> files ;

    private Employee profile;
    private PrivateRepoAdapter adapter;
    private RecyclerView recyclerView;
    private View parent;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if(profile != null) {
//            fetchFiles();
//        }
//        else{
//            initialize();
//        }
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        parent = inflater.inflate(R.layout.fragment_private_repo, container, false);
        initialize();


        return parent;
    }

    private void initialize(){
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        files = new ArrayList<>();
        recyclerView =  parent.findViewById(R.id.private_repo_recyler_view);
        swipeRefreshLayout = parent.findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Perform the refresh action
                // For example, you can fetch updated data from a data source
                // and update the RecyclerView
                fetchFiles("");
            }
        });

        FirebaseFirestore.getInstance().collection("Users").document(fAuth.getCurrentUser().getUid())
        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    profile = task.getResult().toObject(Employee.class);
                    fetchFiles("");
                }
                else{
                    String error = task.getException().getMessage();
                    Toast.makeText(getContext(), "No profile data!\n" + error, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getContext(), MainActivity.class));
                }
            }
        });
    }

    public void fetchFiles(String queryText){
        if(adapter != null){
            adapter.notifyItemRangeRemoved(0, files.size());
        }
        files.clear();

//        Query query = FirebaseFirestore.getInstance().collection("Files").whereEqualTo("visibility", "private");

        Query query = FirebaseFirestore.getInstance().collection("Files").where(Filter.or(
                Filter.equalTo("visibility", "public"),
                Filter.equalTo("sender", profile.getName())
        ));

        if (profile == null || profile.getRole().equals("Staff")){
            query = FirebaseFirestore.getInstance().collection("Files");
        }

        if(!queryText.isEmpty()) {
                query = query.whereGreaterThanOrEqualTo("name", queryText)
                    .whereLessThanOrEqualTo("name", queryText + "\uf8ff");
        }

        Task<QuerySnapshot> taskQuery = query.get();
        taskQuery.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {

                        // Iterate through the documents
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            // Process each document here
                            // Example: String documentId = document.getId();
                            File file = document.toObject(File.class);
                            if(!files.contains(file)) {
                                files.add(file);
                            }
                        }

                        Context context = getActivity();
                        adapter = new PrivateRepoAdapter(files, context);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(context));

                        swipeRefreshLayout.setRefreshing(false);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed fetching documents!\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the title for the ActionBar (AppCompatActivity)
        String customTitle = "Search a file";
        requireActivity().setTitle(customTitle);
    }
}
