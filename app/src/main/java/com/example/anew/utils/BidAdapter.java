package com.example.anew.utils;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anew.R;
import com.example.anew.customer.ViewBidActivity;
import com.example.anew.customer.models.Bid;

import java.util.List;

public class BidAdapter extends RecyclerView.Adapter<BidAdapter.EventViewHolder> {

    private List<Bid> eventList;
    private Context context;

    private OnBidClickListener listener;

    public BidAdapter(Context context, List<Bid> eventList, OnBidClickListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bid_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Bid event = eventList.get(position);
        holder.eventName.setText(event.getBidderName());
        holder.eventDate.setText("Rs: " + event.getBidAmount());
        holder.vendorRating.setText("(" + event.getUserRating() + ")");
        holder.ratingBar.setRating(event.getUserRating());

        holder.itemView.setOnClickListener(v -> {
            listener.onBidClick(event);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventDate, vendorRating;
        RatingBar ratingBar;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.bidder_name);
            eventDate = itemView.findViewById(R.id.offered_budget);
            ratingBar = itemView.findViewById(R.id.materialRatingBar);
            vendorRating = itemView.findViewById(R.id.ratingValue);
        }
    }
}
