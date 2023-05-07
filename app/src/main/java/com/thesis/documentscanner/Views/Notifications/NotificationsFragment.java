package com.thesis.documentscanner.Views.Notifications;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thesis.documentscanner.R;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {

    private ArrayList<String> mNotificationNames = new ArrayList<>();
    private ArrayList<String> mNotificationDetails = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        initBitmaps();
        setLayout(view);
        return view;
    }

    public void initBitmaps() {
        mNotificationNames.add("Welcome");
        mNotificationDetails.add("Welcome to Plan International");
        mNotificationNames.add("New account");
        mNotificationDetails.add("This is your new account. Your password: 1234. Change it ass soon as possible");
        mNotificationNames.add("Password change");
        mNotificationDetails.add("Your request for changing password is accepted");
        mNotificationNames.add("Hasibul Hassan");
        mNotificationDetails.add("Kire mama ki khobor?");

    }

    public void setLayout(View view) {

        RecyclerView recyclerView = view.findViewById(R.id.notification_recycler_view);

        Context context = getActivity();
        NotificationViewAdapter adapter = new NotificationViewAdapter(mNotificationNames, mNotificationDetails, context);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

    }

}
