package com.thesis.documentscanner.Views.userprofile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.thesis.documentscanner.FirebaseQueries;
import com.thesis.documentscanner.Models.File;
import com.thesis.documentscanner.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class UserProfile extends AppCompatActivity {
    private ArrayList<File> files ;
    private FirebaseDatabase firebaseDatabase;
    private String UID;
    private static final int PICK_FILE = 1;
    private DatabaseReference databaseReference;

    private static String  TAG = "UserProfileActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        UID = FirebaseQueries.getInstance().getUID();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(UID).child("PublicFiles");
        files = new ArrayList<>();

        RecyclerView recyclerView =  findViewById(R.id.user_profile_recyler_view);
        Context context = getApplicationContext();
        final PublicFilesAdapter adapter = new PublicFilesAdapter(files,context);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        //Fetch public file list from firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    File file = dataSnapshot.getValue(File.class);
                    files.add(file);
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }
    //Pick file from device and upload file
    public void FileUpload(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent,PICK_FILE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_FILE){
            if(resultCode == RESULT_OK){
                Uri FileUri = data.getData();
                StorageReference Folder = FirebaseStorage.getInstance().getReference().child("Files");
                final StorageReference FileName = Folder.child("file"+ FileUri.getLastPathSegment());
                FileName.putFile(FileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                     FileName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                         @Override
                         public void onSuccess(Uri uri) {
                             String URL = String.valueOf(uri);
                             String filename =   uri.getLastPathSegment();
                             File file = new File(filename, URL, UID);
                             DatabaseReference db  = FirebaseDatabase.getInstance().getReference().child("Users").child(UID).child("PublicFiles");
                             db.push().setValue(file).addOnCompleteListener(new OnCompleteListener<Void>() {
                                 @Override
                                 public void onComplete(@NonNull Task<Void> task) {
                                     if(task.isSuccessful()){
                                         Toast.makeText(UserProfile.this,"File Uploaded", Toast.LENGTH_SHORT).show();
                                     }
                                     else{
                                         Toast.makeText(UserProfile.this,task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                         Log.d(TAG, task.getException().getMessage());
                                     }
                                 }
                             });
                         }
                     });
                    }
                });
            }
        }
    }



}