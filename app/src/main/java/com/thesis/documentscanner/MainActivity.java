package com.thesis.documentscanner;

import static com.thesis.documentscanner.common.Constants.USERS_COLLECTION;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity {

    EditText mEmail,mPassword;
    Button mLoginBtn;
    FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fAuth = FirebaseAuth.getInstance();

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mLoginBtn = findViewById(R.id.loginbutton);



        //Log in
        mAuthStateListener = firebaseAuth -> {
            String email = mEmail.getText().toString().trim();



            FirebaseUser user = firebaseAuth.getCurrentUser();
            if(user != null){
                Query query = FirebaseFirestore.getInstance().collection(USERS_COLLECTION).whereEqualTo("email", email)
                        .whereEqualTo("status", "active");
                query.get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        if (task.getResult().isEmpty() && !email.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Your account is deactivated or deleted", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Intent intent = new Intent(getApplicationContext(), Home.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            }else {
                mLoginBtn.setOnClickListener(v -> {
                    String g_email = mEmail.getText().toString().trim();
                    String password = mPassword.getText().toString().trim();
                    if (TextUtils.isEmpty(g_email)) {
                        mEmail.setError("Email is required");
                        return;
                    }
                    if (TextUtils.isEmpty(password)) {
                        mPassword.setError("Password is required");
                        return;
                    }
                    Log.v(TAG, g_email);
                    Query query = FirebaseFirestore.getInstance().collection(USERS_COLLECTION).whereEqualTo("email", g_email)
                            .whereEqualTo("status", "active");
                    query
                    .get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            if(task.getResult().isEmpty()){
                                Toast.makeText(getApplicationContext(), "Your account is deactivated or deleted", Toast.LENGTH_LONG).show();
                                return;
                            }
                            fAuth.signInWithEmailAndPassword(g_email, password).addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    Log.v(TAG,"Logged In");
                                    Toast.makeText(MainActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), Home.class));
                                } else {
                                    Log.v(TAG,"Not Logged In");
                                    Toast.makeText(MainActivity.this, "Error" + task2.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else{
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                });
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        fAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        fAuth.removeAuthStateListener(mAuthStateListener);
    }

}

