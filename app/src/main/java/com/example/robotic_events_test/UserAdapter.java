package com.example.robotic_events_test;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users;

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userNameText;
        TextView userEmailText;

        public UserViewHolder(View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.userName);
            userEmailText = itemView.findViewById(R.id.userEmail);
        }
    }

    public UserAdapter(List<User> users) {
        this.users = users;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.userNameText.setText(user.getName() != null ? user.getName() : "Unknown");
        holder.userEmailText.setText(user.getEmail() != null ? user.getEmail() : "");
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }
}
