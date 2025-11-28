package com.example.robotic_events_test;

import android.content.Context;
import android.os.CountDownTimer;
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

            // Check for expiration if it's a pending invite
            if (notification.getExpiryTimestamp() > 0) {
                long timeLeft = notification.getExpiryTimestamp() - System.currentTimeMillis();
                if (timeLeft > 0) {
                    startCountDown(holder.expiryText, timeLeft, notification, position);
                    holder.expiryText.setVisibility(View.VISIBLE);
                } else {
                    holder.expiryText.setText("Expired");
                    holder.expiryText.setVisibility(View.VISIBLE);
                    // Disable actions if expired?
                    holder.acceptButton.setEnabled(false);
                    holder.declineButton.setEnabled(false);
                    // Ideally we should also auto-decline on server side or here.
                }
            } else {
                holder.expiryText.setVisibility(View.GONE);
            }

            holder.acceptButton.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Accept Invitation")
                        .setMessage("Are you sure you want to accept this invitation?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Accept Logic: 
                            // 1. Remove from waitlist (treated as "moved to attendees" implicitly)
                            // 2. Update LotteryResult to track as ACCEPTED.
                            String eventId = notification.getEventId();
                            String userId = notification.getReceiverId(); // The current user
                            
                            waitlistController.leaveWaitlist(eventId, userId)
                                    .addOnSuccessListener(aBoolean -> {
                                        // NEW: Call processAccept to update LotteryResult
                                        lotteryController.processAccept(eventId, userId)
                                                .addOnSuccessListener(success -> {
                                                    Toast.makeText(context, "Invitation Accepted", Toast.LENGTH_SHORT).show();
                                                    deleteNotification(notification, position);
                                                });
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
            holder.expiryText.setVisibility(View.GONE);

            holder.dismissButton.setOnClickListener(v -> {
                deleteNotification(notification, position);
            });
        }
    }

    private void startCountDown(TextView textView, long millisInFuture, Notifications notification, int position) {
        // Cancel previous timer if any (requires keeping reference, tricky in RecyclerView)
        // For simplicity, we just start a new one. 
        // Ideally, ViewHolder should manage its timer to avoid leaks on scroll.
        
        new CountDownTimer(millisInFuture, 1000) {
            public void onTick(long millisUntilFinished) {
                // Convert to readable format
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                
                String timeString = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
                textView.setText("Expires in: " + timeString);
            }

            public void onFinish() {
                textView.setText("Expired");
                // Auto-decline logic could be triggered here
            }
        }.start();
    }

    private void deleteNotification(Notifications notification, int position) {
        if (notification.getId() != null) {
            db.collection("notifications").document(notification.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error deleting notification", Toast.LENGTH_SHORT).show();
                    });
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
        TextView expiryText; // NEW
        LinearLayout actionButtonsLayout;
        Button acceptButton;
        Button declineButton;
        Button dismissButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.notification_message);
            timestampText = itemView.findViewById(R.id.notification_timestamp);
            expiryText = itemView.findViewById(R.id.notification_expiry); // NEW
            actionButtonsLayout = itemView.findViewById(R.id.action_buttons_layout);
            acceptButton = itemView.findViewById(R.id.btn_accept);
            declineButton = itemView.findViewById(R.id.btn_decline);
            dismissButton = itemView.findViewById(R.id.btn_dismiss);
        }
    }
}
