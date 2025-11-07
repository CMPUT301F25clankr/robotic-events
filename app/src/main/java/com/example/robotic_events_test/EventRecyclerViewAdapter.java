package com.example.robotic_events_test;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Event> events;

    public EventRecyclerViewAdapter(Context context, ArrayList<Event> events) {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.events_row, parent, false); // your card layout XML
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);

        holder.eventTitle.setText(event.getTitle());
        holder.eventLocation.setText(event.getLocation());

        // Convert timestamp to readable date later — placeholder for now:
        holder.eventDateTime.setText(formatDateTime(event.getDateTime()));


        // holder.eventImage.setImageResource(R.drawable.yoga);

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            Event clickedEvent = events.get(currentPosition);
            Intent intent = new Intent(v.getContext(), EventDetailActivity.class);

            intent.putExtra("id", clickedEvent.getId());
            intent.putExtra("title", clickedEvent.getTitle());
            intent.putExtra("description", clickedEvent.getDescription());
            intent.putExtra("dateTime", clickedEvent.getDateTime());
            intent.putExtra("location", clickedEvent.getLocation());
            intent.putExtra("category", clickedEvent.getCategory());
            intent.putExtra("organizerId", clickedEvent.getOrganizerId());
            intent.putExtra("totalCapacity", clickedEvent.getTotalCapacity());
            intent.putExtra("status", clickedEvent.getStatus());
            intent.putExtra("imageUrl", clickedEvent.getImageUrl());
            intent.putExtra("price", clickedEvent.getPrice());

            v.getContext().startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    private String formatDateTime(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());
        return sdf.format(date);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView eventImage;
        TextView eventTitle;
        TextView eventDateTime;
        TextView eventLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            eventImage = itemView.findViewById(R.id.eventImage);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDateTime = itemView.findViewById(R.id.eventDateTime);
            eventLocation = itemView.findViewById(R.id.eventLocation); // using distance TextView for now
        }
    }
}