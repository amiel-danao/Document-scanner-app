package com.thesis.documentscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Intent intent = new Intent(getApplicationContext(), Home.class);
                    FirebaseQueries firebaseQueries= FirebaseQueries.getInstance();
                    firebaseQueries.setUID(user.getUid());
                    startActivity(intent);
                    finish();
                }else {
                    mLoginBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String email = mEmail.getText().toString().trim();
                            String password = mPassword.getText().toString().trim();
                            if (TextUtils.isEmpty(email)) {
                                mEmail.setError("Email is required");
                                return;
                            }
                            if (TextUtils.isEmpty(password)) {
                                mPassword.setError("Password is required");
                                return;
                            }
                            Log.v(TAG,email);

                            fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.v(TAG,"Logged In");
                                        Toast.makeText(MainActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(), Home.class));
                                    } else {
                                        Log.v(TAG,"Not Logged In");
                                        Toast.makeText(MainActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    });

                }
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

