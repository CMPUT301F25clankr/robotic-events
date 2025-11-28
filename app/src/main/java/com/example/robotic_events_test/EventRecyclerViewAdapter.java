package com.example.robotic_events_test;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Event> events;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
    private final boolean isOrganizer;

    public EventRecyclerViewAdapter(Context context, ArrayList<Event> events, boolean isOrganizer) {
        this.context = context;
        this.events = events;
        this.isOrganizer = isOrganizer;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.events_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);

        holder.eventTitle.setText(event.getTitle());
        holder.eventLocation.setText(event.getLocation());
        holder.eventDateTime.setText(dateFormatter.format(new Date(event.getDateTime())));

        if (isOrganizer) {
            holder.eventRowRoot.setBackgroundColor(Color.parseColor("#E8EAF6"));
            holder.eventTitle.setTextColor(Color.parseColor("#1565C0"));
        } else {
            holder.eventRowRoot.setBackgroundColor(Color.parseColor("#F3E5F5"));
            holder.eventTitle.setTextColor(Color.parseColor("#8E24AA"));
        }

        holder.bind(event, context);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout eventRowRoot;
        TextView eventTitle, eventLocation, eventDateTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventRowRoot = itemView.findViewById(R.id.eventRowRoot);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventLocation = itemView.findViewById(R.id.eventLocation);
            eventDateTime = itemView.findViewById(R.id.eventDateTime);
        }

        public void bind(Event event, Context context) {
            // Set click listener on the eventRowRoot instead of itemView
            eventRowRoot.setOnClickListener(v -> {
                Intent intent = new Intent(context, EventDetailActivity.class);
                intent.putExtra("event", event);
                context.startActivity(intent);
            });
        }


    }
}
