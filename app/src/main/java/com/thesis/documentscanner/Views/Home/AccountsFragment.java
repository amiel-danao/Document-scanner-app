package com.thesis.documentscanner.Views.Home;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.thesis.documentscanner.Models.Employee;
import com.thesis.documentscanner.R;

import java.util.ArrayList;
import java.util.List;

public class AccountsFragment extends Fragment {

    private List<Employee> accounts;
    private AccountsAdapter adapter;
    private RecyclerView recyclerView;
    private View closeForm;
    private View form;
    private FloatingActionButton fab;
    private EditText editName, editEmail, editPassword, editConfirmPassword;
    private Button createAccountButton;
    private FirebaseAuth auth;
    private View loading;
    private RadioGroup radioGroup;
    private View parent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        parent = inflater.inflate(R.layout.fragment_accounts, container, false);
        accounts = new ArrayList<>();
        recyclerView = parent.findViewById(R.id.accountsRecyclerView);
        form = parent.findViewById(R.id.form);
        fab = parent.findViewById(R.id.fab);
        closeForm = parent.findViewById(R.id.closeForm);
        editName = parent.findViewById(R.id.editName);
        editEmail = parent.findViewById(R.id.editEmail);
        editPassword = parent.findViewById(R.id.editTextPassword);
        editConfirmPassword = parent.findViewById(R.id.editTextConfirmPassword);
        loading = parent.findViewById(R.id.loading);
        radioGroup = parent.findViewById(R.id.radioGroup);
        createAccountButton = parent.findViewById(R.id.createAccountButton);

        auth = FirebaseAuth.getInstance();

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFormValid()){
                    registerUserWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString());
                }
                else{
                    Toast.makeText(getContext(), "Please fill up the required fields!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        closeForm.setOnClickListener(view12 -> {
            form.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
            clearForm();
        });

        fab.setOnClickListener(view1 -> {
            form.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
        });

        fetchAccounts();
        return parent;
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
                FirebaseUser user = auth.getCurrentUser();
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
                            Toast.makeText(getContext(), "Account was created successfully:",
                                    Toast.LENGTH_SHORT).show();
                            clearForm();
                            String logMessage = String.format("Account created(%s): %s", finalRole, employee.getEmail());
                            writeLog(logMessage, auth.getCurrentUser().getUid());
                        }
                        else{
                            Toast.makeText(getContext(), "Profile creation:" + task1.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
//                    sendEmailVerification(user);
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
                adapter = new AccountsAdapter(context, accounts);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            }
            else{
                String error = task.getException().getMessage();
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the title for the ActionBar (AppCompatActivity)
        String customTitle = "Accounts";
        requireActivity().setTitle(customTitle);
    }
}