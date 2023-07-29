package com.thesis.documentscanner.Views.Account;

import static com.thesis.documentscanner.common.Constants.USERS_COLLECTION;
import static com.thesis.documentscanner.util.LogUtils.writeLog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.functions.FirebaseFunctions;
import com.thesis.documentscanner.Models.Employee;
import com.thesis.documentscanner.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountsFragment extends Fragment {

    private List<Employee> accounts;
    private AccountsAdapter adapter;
    private RecyclerView recyclerView;
    private View closeForm;
    private View form;
    private FloatingActionButton fab;
    private EditText editName, editEmail, editPassword, editConfirmPassword;
    private TextView editTitle;
    private Button createAccountButton;
    private FirebaseAuth auth;
    private View loading;
    private RadioGroup radioGroup;
    private View parent;
    private Employee employeeToEdit;
    private Employee profile;
    private FirebaseFunctions mFunctions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        parent = inflater.inflate(R.layout.fragment_accounts, container, false);
        accounts = new ArrayList<>();
        recyclerView = parent.findViewById(R.id.accountsRecyclerView);
        form = parent.findViewById(R.id.form);
        fab = parent.findViewById(R.id.fab);
        closeForm = parent.findViewById(R.id.closeForm);
        editTitle = parent.findViewById(R.id.ediTitle);
        editName = parent.findViewById(R.id.editName);
        editEmail = parent.findViewById(R.id.editEmail);
        editPassword = parent.findViewById(R.id.editTextPassword);
        editConfirmPassword = parent.findViewById(R.id.editTextConfirmPassword);
        loading = parent.findViewById(R.id.loading);
        radioGroup = parent.findViewById(R.id.radioGroup);
        createAccountButton = parent.findViewById(R.id.createAccountButton);

        auth = FirebaseAuth.getInstance();
        mFunctions = FirebaseFunctions.getInstance();

        FirebaseFirestore.getInstance().collection(USERS_COLLECTION).document(auth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    profile = task.getResult().toObject(Employee.class);
                }
            }
        });

        createAccountButton.setOnClickListener(view -> {
            if(isFormValid()){
                if(employeeToEdit == null) {
                    registerUserWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString());
                }
                else{
                    updateAccount(employeeToEdit);
                }
            }
            else{
                Toast.makeText(getContext(), "Please fill up the required fields!", Toast.LENGTH_SHORT).show();
            }
        });

        closeForm.setOnClickListener(view12 -> {
            employeeToEdit = null;
            form.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
            clearForm();
        });

        fab.setOnClickListener(view1 -> {
            showEditAccount(null);
            fab.setVisibility(View.GONE);
        });

        fetchAccounts();
        return parent;
    }

    private void updateAccount(Employee employee) {
        String editedUserName = editName.getText().toString();
        boolean userNameChanged = !employee.getName().equals(editedUserName);

        employee.setName(editedUserName);
        int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
        String role = "User";

        if (selectedRadioButtonId != -1) {
            RadioButton selectedRadioButton = parent.findViewById(selectedRadioButtonId);
            role = selectedRadioButton.getText().toString();
        }
        String finalRole = role;
        FirebaseFirestore.getInstance().collection(USERS_COLLECTION).document(employee.getUID())
        .set(employee, SetOptions.merge()).addOnCompleteListener(task1 -> {
            if(task1.isSuccessful()){
                if(userNameChanged){
                    String uid = employee.getUID();
                    Map<String, Object> data = new HashMap<>();
                    data.put("uid", uid);
                    data.put("name", editedUserName);

                    mFunctions
                            .getHttpsCallable("updateUserName")
                            .call(data)
                            .continueWith(task -> {
                                String result = (String) task.getResult().getData();
                                Toast.makeText(getContext(), "Account was updated successfully",
                                        Toast.LENGTH_SHORT).show();
                                return result;
                            });
                }
                else{
                    Toast.makeText(getContext(), "Account was updated successfully:",
                            Toast.LENGTH_SHORT).show();
                }

                clearForm();
                String logMessage = String.format("Account updated(%s): %s", finalRole, employee.getEmail());
                writeLog(logMessage, auth.getCurrentUser().getDisplayName(), auth.getCurrentUser().getUid());
                form.setVisibility(View.GONE);
            }
            else{
                Toast.makeText(getContext(), "Profile creation:" + task1.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isFormValid() {
        String email = editEmail.getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (email.isEmpty() || !email.matches(emailPattern)) {
            editEmail.setError("Invalid Email!");
            return false;
        }

        String name = editName.getText().toString();
        if(name.trim().isEmpty()){
            editName.setError("Invalid Name!");
            return false;
        }

        if(employeeToEdit != null)
            return true;

        String password = editPassword.getText().toString();
        String confirmPassword = editConfirmPassword.getText().toString();

        if(!password.equals(confirmPassword)){
            editConfirmPassword.setError("Password doesn't match!");
            return false;
        }

        if (password.length() < 6) {
            editPassword.setError("Password should be at least 6 characters!");
            return false;
        }

        return true;
    }

    private void clearForm(){
        editName.setText("");
        editEmail.setText("");
        editPassword.setText("");
        editConfirmPassword.setText("");
    }

    private void registerUserWithEmailAndPassword(String email, String password) {
        loading.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Registration success

                FirebaseUser user = task.getResult().getUser();
                int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                String role = "User";

                if (selectedRadioButtonId != -1) {
                    RadioButton selectedRadioButton = parent.findViewById(selectedRadioButtonId);
                    role = selectedRadioButton.getText().toString();
                }

                Employee employee = new Employee(editName.getText().toString(), user.getUid(), user.getEmail(), "", role, "active");
                String finalRole = role;
                FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                    .set(employee, SetOptions.merge()).addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()){

                            String uid = employee.getUID();
                            Map<String, Object> data = new HashMap<>();
                            data.put("uid", uid);
                            data.put("name", editName.getText().toString());
                            mFunctions
                                .getHttpsCallable("updateUserName")
                                .call(data)
                                .continueWith(task2 -> {
                                    String result = (String) task2.getResult().getData();

                                    return result;
                                });
                            Toast.makeText(getContext(), "Account was created successfully:",
                                    Toast.LENGTH_SHORT).show();
                            clearForm();
                            String logMessage = String.format("Account created(%s): %s", finalRole, employee.getEmail());
                            form.setVisibility(View.GONE);
                            writeLog(logMessage, auth.getCurrentUser().getDisplayName(), auth.getCurrentUser().getUid());
                        }
                        else{
                            Toast.makeText(getContext(), "Profile creation:" + task1.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
            } else {
                // Registration failed
                Toast.makeText(getContext(), "Registration failed:" + task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
            }

            loading.setVisibility(View.GONE);
        });
    }

    private void fetchAccounts() {
        if(adapter != null){
            adapter.notifyItemRangeRemoved(0, accounts.size());
        }
        accounts.clear();

        FirebaseFirestore.getInstance().collection("Users")
        .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    accounts.add(document.toObject(Employee.class));
                }

                Context context = getActivity();
                adapter = new AccountsAdapter(context, accounts, this);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            }
            else{
                String error = task.getException().getMessage();
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void showEditAccount(Employee employee){
        employeeToEdit = employee;
        form.setVisibility(View.VISIBLE);
        if(employee == null){
            editTitle.setText("New Account");
            editEmail.setText("");
            editEmail.setEnabled(true);
            editName.setText("");
            editPassword.setVisibility(View.VISIBLE);
            editConfirmPassword.setVisibility(View.VISIBLE);
            createAccountButton.setText("Create Account");
        }
        else{
            editTitle.setText("Edit Account");
            editEmail.setText(employee.getEmail());
            editEmail.setEnabled(false);
            editName.setText(employee.getName());
            editPassword.setVisibility(View.GONE);
            editConfirmPassword.setVisibility(View.GONE);
            createAccountButton.setText("Update account");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the title for the ActionBar (AppCompatActivity)
        String customTitle = "Accounts";
        requireActivity().setTitle(customTitle);
    }
}