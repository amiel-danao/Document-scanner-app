package com.thesis.documentscanner.common;

import static com.thesis.documentscanner.common.Constants.USERS_COLLECTION;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thesis.documentscanner.Models.Employee;

public abstract class RequireLoginFragment extends Fragment {
    protected Employee loggedInUser;
    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() == null){
            return;
        }

        fetchUserData();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchUserData();
    }

    private void fetchUserData(){
        FirebaseFirestore.getInstance().collection(USERS_COLLECTION).document(firebaseAuth.getCurrentUser().getUid()).get()
            .addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    loggedInUser = task.getResult().toObject(Employee.class);
                    onFetchedUser();
                }
            });
    }

    public void onFetchedUser(){
    }
}
