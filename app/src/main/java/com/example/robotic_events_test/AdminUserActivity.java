package com.example.robotic_events_test;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
/** displays the users in admin view*/
public class AdminUserActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private UserListAdapter userListAdapter;
    private List<User> userList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user);

        MaterialToolbar toolbar = findViewById(R.id.adminUserToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();

        fetchUsers();
    }

    private void fetchUsers() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            userList.add(user);
                        }
                        userListAdapter = new UserListAdapter(this, userList);
                        usersRecyclerView.setAdapter(userListAdapter);
                    }
                });
    }
}
