package com.thesis.documentscanner.Views.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thesis.documentscanner.Models.Employee;
import com.thesis.documentscanner.R;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    RecyclerView recyclerView;
//    DatabaseReference database;
    HomeViewAdapter homeViewAdapter;
    ArrayList<Employee> list;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.home_recyler_view);
//        database = FirebaseDatabase.getInstance().getReference("UserInfo");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        list = new ArrayList<>();
        homeViewAdapter = new HomeViewAdapter(getActivity(),list);
        recyclerView.setAdapter(homeViewAdapter);

        //Fetch assigned public folder list from firebase
//        database.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
//
//                  Employee employee = dataSnapshot.getValue(Employee.class);
//                    list.add(employee);
//
//                }
//                homeViewAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        return view;
    }

}
