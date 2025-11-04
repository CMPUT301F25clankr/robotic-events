package com.example.robotic_events_test;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventRecyclerViewAdapter adapter;
    private final ArrayList<Event> events = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // must contain RecyclerView with id eventsRecycler

        recyclerView = findViewById(R.id.eventsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        seedFakeData(); // replace with your Firestore load

        adapter = new EventRecyclerViewAdapter(
                this,
                events,
                (event, position, itemView) -> {
                    Intent i = new Intent(this, EventDetailActivity.class);
                    // Pass basic fields so detail screen can render without fetching
                    i.putExtra("id",          event.getId());
                    i.putExtra("title",       event.getTitle());
                    i.putExtra("description", event.getDescription());
                    i.putExtra("dateTime",    event.getDateTime());
                    i.putExtra("location",    event.getLocation());
                    i.putExtra("category",    event.getCategory());
                    i.putExtra("organizerId", event.getOrganizerId());
                    i.putExtra("totalCapacity", event.getTotalCapacity());
                    i.putExtra("status",      event.getStatus());
                    i.putExtra("imageResId",  event.getImageResId());
                    i.putExtra("price",       event.getPrice());
                    startActivity(i);
                }
        );
        recyclerView.setAdapter(adapter);
    }

    private void seedFakeData() {
        long now = System.currentTimeMillis();
        events.clear();

        Event e1 = new Event("1", "Robotics Meetup", now + 3600_000L,
                "CAB 3-10", R.drawable.ic_launcher_foreground, 50, 0.0);
        e1.setDescription("Weekly robotics club meetup.");
        e1.setCategory("Meetup");
        e1.setOrganizerId("orgA");

        Event e2 = new Event("2", "Hardware Night", now + 7200_000L,
                "ETLC E1-001", R.drawable.ic_launcher_foreground, 40, 5.0);
        e2.setDescription("Soldering, sensors, PCB basics.");
        e2.setCategory("Workshop");
        e2.setOrganizerId("orgB");

        Event e3 = new Event("3", "Competition Prep", now + 10800_000L,
                "CSC 2-29", R.drawable.ic_launcher_foreground, 30, 0.0);
        e3.setDescription("Strategy & practice for upcoming comp.");
        e3.setCategory("Practice");
        e3.setOrganizerId("orgC");

        events.add(e1);
        events.add(e2);
        events.add(e3);
    }
}
