package com.example.eventflow.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventflow.R;
import com.example.eventflow.model.TicketModel;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<TicketModel> ticketList;

    public TicketAdapter(List<TicketModel> ticketList) {
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        TicketModel ticket = ticketList.get(position);
        holder.textEventName.setText(ticket.getEventName());
        holder.textEventDate.setText(ticket.getDate() + " - " + ticket.getTime());
        holder.textEventLocation.setText(ticket.getLocation());
        holder.textTicketId.setText("Ticket ID: " + ticket.getTicketId());

        // ✅ Debug log
        Log.d("TICKET_ADAPTER", "Loading ticket: " + ticket.getTicketId() + ", QR: " + ticket.getQrUrl());

        // ✅ Glide with placeholder and error handling
        Glide.with(holder.itemView.getContext())
                .load(ticket.getQrUrl())
                .placeholder(R.drawable.qr_placeholder) // You can use a simple QR placeholder image
                .error(R.drawable.qr_error)             // You can create a "not found" image or icon
                .into(holder.imageQR);
    }


    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView textEventName, textEventDate, textEventLocation, textTicketId;
        ImageView imageQR;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            textEventName = itemView.findViewById(R.id.textTicketEvent);
            textEventDate = itemView.findViewById(R.id.textTicketDate);
            textEventLocation = itemView.findViewById(R.id.textTicketLocation);
            textTicketId = itemView.findViewById(R.id.textTicketId);
            imageQR = itemView.findViewById(R.id.imageQR);
        }
    }
}
