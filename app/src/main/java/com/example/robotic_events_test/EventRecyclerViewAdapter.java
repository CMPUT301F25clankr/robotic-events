package com.example.robotic_events_test;

import android.content.Context;
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


        holder.eventImage.setImageResource(event.getImageResId());

        holder.itemView.setOnClickListener(v -> {
            // TODO: open event detail page or show a popup
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