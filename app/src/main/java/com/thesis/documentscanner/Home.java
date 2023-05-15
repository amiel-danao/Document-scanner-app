package com.thesis.documentscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.thesis.documentscanner.Views.Home.HomeFragment;
import com.thesis.documentscanner.Views.Notifications.NotificationsFragment;
import com.thesis.documentscanner.Views.PrivateRepo.PrivateRepoFragment;
import com.thesis.documentscanner.Views.QRScannerFragment;
import com.thesis.documentscanner.Views.userprofile.UserProfile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class Home extends AppCompatActivity {
    FirebaseAuth fAuth;
    private PrivateRepoFragment privateFileFragment;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        fAuth = FirebaseAuth.getInstance();

        privateFileFragment = new PrivateRepoFragment();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListner);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    // Fragment switching
    private BottomNavigationView.OnNavigationItemSelectedListener navListner =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()){
                        case R.id.home_activity:
                            selectedFragment = new HomeFragment();
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().hide();
                            }
                            break;
                        case R.id.folder_activity:
                            selectedFragment = privateFileFragment;
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().show();
                            }
                            break;
                        case R.id.qr_scanner_activity:
                            selectedFragment = new QRScannerFragment();
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().hide();
                            }
                            break;

                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                    return true;
                }
            };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_bar, menu);
        MenuItem searchItem = menu.findItem(R.id.searchEdit);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                privateFileFragment.fetchFiles(query); // Call the performSearch method in the fragment
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                privateFileFragment.fetchFiles(newText); // Call the performSearch method in the fragment
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public void logout(){
            fAuth.signOut();
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
          }
    public void userprofile(){

        startActivity(new Intent(getApplicationContext(), UserProfile.class));

    }


    // Toolbar item switch
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.userProfile:
              userprofile();
                break;
            case R.id.searchEdit:
                break;
            case R.id.logout:
                logout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}