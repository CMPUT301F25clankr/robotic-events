package com.example.robotic_events_test;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<Event> events = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.events_container);
        EventRecyclerViewAdapter adapter = new EventRecyclerViewAdapter(this, events);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        defaultEvents(); // call AFTER setting adapter
        adapter.notifyDataSetChanged();
    }

    private void defaultEvents() {

        events.add(new Event("1", "Kids Swimming Lessons",
                1732150800000L,
                "Westside Community Pool",
                R.drawable.swimming, 20, 0.0));

        events.add(new Event("2", "Community Cooking Workshop",
                1732237200000L,
                "Downtown Community Kitchen",
                R.drawable.cooking, 12, 10.0));

        events.add(new Event("3", "Yoga in the Park",
                1732323600000L,
                "Central Park Stage",
                R.drawable.yoga, 30, 5.0));

        events.add(new Event("4", "Retro Stock Car Racing",
                1732410000000L,
                "City Raceway",
                R.drawable.race, 25, 15.0));

        events.add(new Event("5", "Recreational Soccer League",
                1732496400000L,
                "Maplewood Sports Field",
                R.drawable.soccer, 22, 0.0));

    }

}