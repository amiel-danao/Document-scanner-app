package com.thesis.documentscanner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thesis.documentscanner.Models.Employee;
import com.thesis.documentscanner.Views.Home.HomeFragment;
import com.thesis.documentscanner.Views.Home.AccountsFragment;
import com.thesis.documentscanner.Views.PrivateRepo.PrivateRepoFragment;
import com.thesis.documentscanner.Views.QRScannerFragment;
import com.thesis.documentscanner.Views.userprofile.UserProfile;

public class Home extends AppCompatActivity {
    FirebaseAuth fAuth;
    private PrivateRepoFragment privateFileFragment;
    private Toolbar toolbar;
    private Employee profile;
    private SearchView searchView;
    private MenuItem uploadItem, searchItem;
    private Fragment selectedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        fAuth = FirebaseAuth.getInstance();

        privateFileFragment = new PrivateRepoFragment();

        selectedFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                selectedFragment).commit();


        FirebaseFirestore.getInstance().collection("Users").document(fAuth.getCurrentUser().getUid())
        .get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                profile = task.getResult().toObject(Employee.class);

                BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
                bottomNav.setOnNavigationItemSelectedListener(navListner);

                if(profile.getRole().equals("Staff")){
                    Menu menu = bottomNav.getMenu();
                    MenuItem item = menu.findItem(R.id.accounts_activity);
                    item.setVisible(true);
                }
            }
            else{
                String error = task.getException().getMessage();
                Toast.makeText(getApplicationContext(), "No profile data!\n" + error, Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }

    private View findMenuItemById(Menu menu, int itemId) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            if (menuItem.getItemId() == itemId) {
                return menuItem.getActionView();
            }

            if (menuItem.hasSubMenu()) {
                View subMenuItem = findMenuItemById(menuItem.getSubMenu(), itemId);
                if (subMenuItem != null) {
                    return subMenuItem;
                }
            }
        }

        return null; // MenuItem not found
    }

    private void toggleMenu(boolean show){
        uploadItem.setVisible(show);
        searchItem.setVisible(show);

    }


    // Fragment switching
    private final BottomNavigationView.OnNavigationItemSelectedListener navListner =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    selectedFragment = null;

                    switch (item.getItemId()){
                        case R.id.home_activity:
                            selectedFragment = new HomeFragment();
                            if (getSupportActionBar() != null) {
                                toggleMenu(false);
                            }
                            break;
                        case R.id.folder_activity:
                            selectedFragment = privateFileFragment;
                            if (getSupportActionBar() != null) {
                                toggleMenu(true);
                            }
                            break;
                        case R.id.qr_scanner_activity:
                            selectedFragment = new QRScannerFragment();
                            if (getSupportActionBar() != null) {
                                toggleMenu(false);
                            }
                            break;
                        case R.id.accounts_activity:
                            selectedFragment = new AccountsFragment();
                            if (getSupportActionBar() != null) {
                                toggleMenu(false);
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
        MenuItem searchViewItem = menu.findItem(R.id.searchEdit);
        searchView = (SearchView) searchViewItem.getActionView();
//
//        MenuItem uploadItem = menu.findItem(R.id.uploadMenu);
//        uploadButton = uploadItem.getActionView();



        new Handler().post(new Runnable() {
            @Override
            public void run() {

                uploadItem = menu.findItem(R.id.uploadMenu);
                searchItem = menu.findItem(R.id.searchEdit);
                // SOME OF YOUR TASK AFTER GETTING VIEW REFERENCE
                toggleMenu(selectedFragment.getClass().equals(PrivateRepoFragment.class));
            }
        });

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
            case R.id.uploadMenu:
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