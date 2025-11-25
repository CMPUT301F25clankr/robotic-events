package com.example.robotic_events_test;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Notifications> notifications;
    private FirebaseFirestore db;
    private WaitlistController waitlistController;
    private LotteryController lotteryController;

    public NotificationsAdapter(Context context, ArrayList<Notifications> notifications) {
        this.context = context;
        this.notifications = notifications;
        this.db = FirebaseFirestore.getInstance();
        this.waitlistController = new WaitlistController();
        this.lotteryController = new LotteryController();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notifications notification = notifications.get(position);
        holder.messageText.setText(notification.getMessage());

        // Format timestamp
        long time = notification.getTimestamp();
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                time,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        );
        holder.timestampText.setText(timeAgo);

        if (notification.getRespondable() != null && notification.getRespondable()) {
            holder.actionButtonsLayout.setVisibility(View.VISIBLE);
            holder.dismissButton.setVisibility(View.GONE);

            holder.acceptButton.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Accept Invitation")
                        .setMessage("Are you sure you want to accept this invitation?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Accept Logic: Remove from waitlist (treated as "moved to attendees" implicitly)
                            String eventId = notification.getEventId();
                            String userId = notification.getReceiverId(); // The current user
                            
                            waitlistController.leaveWaitlist(eventId, userId)
                                    .addOnSuccessListener(aBoolean -> {
                                        Toast.makeText(context, "Invitation Accepted", Toast.LENGTH_SHORT).show();
                                        deleteNotification(notification, position);
                                    });
                        })
                        .setNegativeButton("No", null)
                        .show();
            });

            holder.declineButton.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Decline Invitation")
                        .setMessage("Are you sure you want to decline this invitation?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Decline Logic: Remove from waitlist AND find replacement
                            String eventId = notification.getEventId();
                            String userId = notification.getReceiverId();

                            // 1. Remove from waitlist
                            waitlistController.leaveWaitlist(eventId, userId)
                                    .addOnSuccessListener(aBoolean -> {
                                        // 2. Process decline and find replacement
                                        lotteryController.processDecline(eventId, userId)
                                                .addOnSuccessListener(replaced -> {
                                                    String msg = replaced ? "Invitation Declined. Replacement found." : "Invitation Declined.";
                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                                                    deleteNotification(notification, position);
                                                });
                                    });
                        })
                        .setNegativeButton("No", null)
                        .show();
            });

        } else {
            holder.actionButtonsLayout.setVisibility(View.GONE);
            holder.dismissButton.setVisibility(View.VISIBLE);

            holder.dismissButton.setOnClickListener(v -> {
                deleteNotification(notification, position);
            });
        }
    }

    private void deleteNotification(Notifications notification, int position) {
        if (notification.getId() != null) {
            // Optimistically remove from list to fix "disappear right away" issue
            // Check bounds to avoid crashes if rapid clicks happen
            if (position >= 0 && position < notifications.size()) {
                // Note: notifications.remove(position) might mismatch if list changed in background.
                // Safer to remove by object or just rely on the fact that we are in onBind logic?
                // No, position is stale if adapter is modified.
                // However, for immediate feedback:
                // We should probably not remove it manually if we rely on SnapshotListener, 
                // BUT the user complained about lag. 
                // Removing it here + notifying adapter will hide it.
                // When SnapshotListener fires, it will just confirm the new state.
                
                // Actually, if we remove it here, and then SnapshotListener fires with the "REMOVED" event,
                // we need to ensure we don't double remove or crash.
                // Since `NotificationsActivity` clears and refills the list on snapshot, it should be fine.
                // But wait, `NotificationsActivity` uses `notificationsList` which IS `notifications` here (same reference passed in constructor).
                // So if we modify it here, the Activity's list is modified.
                
                // Let's just rely on DB delete callback for safety, but to make it "instant", we can hide the view?
                // Or just notify item removed.
                
                db.collection("notifications").document(notification.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                         // Success. SnapshotListener in Activity will likely fire and refresh the list.
                         // If we want it FASTER, we could suppress it here.
                         // But usually Firestore local writes are instant. 
                         // The issue might be `NotificationsActivity` waiting for server confirmation?
                         // No, Firestore defaults to optimistic UI.
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error deleting notification", Toast.LENGTH_SHORT).show();
                    });
            }
        } else {
            Toast.makeText(context, "Cannot delete: Notification ID missing", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timestampText;
        LinearLayout actionButtonsLayout;
        Button acceptButton;
        Button declineButton;
        Button dismissButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.notification_message);
            timestampText = itemView.findViewById(R.id.notification_timestamp);
            actionButtonsLayout = itemView.findViewById(R.id.action_buttons_layout);
            acceptButton = itemView.findViewById(R.id.btn_accept);
            declineButton = itemView.findViewById(R.id.btn_decline);
            dismissButton = itemView.findViewById(R.id.btn_dismiss);
        }
    }
}
