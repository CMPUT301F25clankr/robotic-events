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

public class EventRecyclerViewAdapter
        extends RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event, int position, View itemView);
    }

    private final Context context;
    private final ArrayList<Event> events;
    private final OnEventClickListener onClick;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());

    public EventRecyclerViewAdapter(Context context,
                                    ArrayList<Event> events,
                                    OnEventClickListener onClick) {
        this.context = context;
        this.events = events;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.events_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event e = events.get(position);

        holder.eventTitle.setText(nullToEmpty(e.getTitle()));
        holder.eventLocation.setText(nullToEmpty(e.getLocation()));

        String when = e.getDateTime() > 0
                ? sdf.format(new Date(e.getDateTime()))
                : "";
        holder.eventDateTime.setText(when);

        if (e.getImageResId() != 0) {
            holder.eventImage.setImageResource(e.getImageResId());
        } else {
            holder.eventImage.setImageResource(android.R.color.transparent);
        }

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();   // works on all RecyclerView versions
            if (pos != RecyclerView.NO_POSITION && onClick != null) {
                onClick.onEventClick(events.get(pos), pos, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventTitle, eventDateTime, eventLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage    = itemView.findViewById(R.id.eventImage);
            eventTitle    = itemView.findViewById(R.id.eventTitle);
            eventDateTime = itemView.findViewById(R.id.eventDateTime);
            eventLocation = itemView.findViewById(R.id.eventLocation);
        }
    }
}
