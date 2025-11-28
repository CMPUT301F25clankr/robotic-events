package com.example.robotic_events_test;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;

public class AdminEventActivity extends AppCompatActivity {

    private ArrayList<Event> events = new ArrayList<>();
    private EventRecyclerViewAdapter adapter;
    private ListenerRegistration eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event);

        MaterialToolbar toolbar = findViewById(R.id.adminEventToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.admin_events_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventRecyclerViewAdapter(this, events, false); // Admin sees all events
        recyclerView.setAdapter(adapter);

        loadEventsFromFirestore();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventListener != null) {
            eventListener.remove();
        }
    }

    private void loadEventsFromFirestore() {
        EventModel eventModel = new EventModel();
        eventListener = eventModel.addEventsListener((value, error) -> {
            if (error != null) {
                Log.e("AdminEventActivity", "Listen failed.", error);
                Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                events.clear();
                for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                    events.add(doc.toObject(Event.class));
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}
