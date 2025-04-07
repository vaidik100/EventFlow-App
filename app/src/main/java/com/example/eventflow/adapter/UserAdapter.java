package com.example.eventflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventflow.R;
import com.example.eventflow.model.UserModel;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<UserModel> userList;
    private List<UserModel> filteredList;

    public UserAdapter(List<UserModel> userList) {
        this.userList = userList;
        this.filteredList = new ArrayList<>(userList);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = filteredList.get(position);
        holder.userName.setText(user.getFullName());
        holder.userEmail.setText(user.getEmail());
        holder.userRole.setText(user.getRole());

        holder.blockButton.setOnClickListener(v -> {
            filteredList.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(userList);
        } else {
            for (UserModel user : userList) {
                if (user.getFullName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userEmail, userRole;
        Button blockButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.textUserName);
            userEmail = itemView.findViewById(R.id.textUserEmail);
            userRole = itemView.findViewById(R.id.textUserRole);
            blockButton = itemView.findViewById(R.id.btnBlock);
        }
    }
}
