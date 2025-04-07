package com.example.eventflow;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.adapter.UserAdapter;
import com.example.eventflow.model.UserModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {

    private RecyclerView recyclerUsers;
    private UserAdapter userAdapter;
    private List<UserModel> userList;
    private FirebaseFirestore db;
    private EditText searchUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        db = FirebaseFirestore.getInstance();
        recyclerUsers = findViewById(R.id.recyclerUsers);
        searchUsers = findViewById(R.id.searchUsers);

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsers.setAdapter(userAdapter);

        fetchUsers(); // Load users from Firestore

        searchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void fetchUsers() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d("USER_FETCH", "Document: " + document.getData()); // 🔍 Log raw data

                    UserModel user = document.toObject(UserModel.class);
                    Log.d("USER_FETCH", "Mapped user: " + user.getFullName() + ", " + user.getEmail() + ", " + user.getRole());

                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
                Log.d("USER_FETCH", "Total users: " + userList.size());
            } else {
                Log.e("USER_FETCH", "Failed: " + task.getException().getMessage());
            }
        });
    }

}
