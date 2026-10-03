package com.example.anew.utils;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.anew.R;
import com.example.anew.customer.ViewEventActivity;
import com.example.anew.customer.models.Event;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private Context context;

    public EventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.evnet_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventName.setText(event.getEventName());
        holder.eventDate.setText(event.getEventDate());
        holder.eventLocation.setText(event.getEventLocation());

        Glide.with(context)
                .load(event.getEventPicUrl())
                .placeholder(R.drawable.baseline_image_24) // optional
                .into(holder.eventImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewEventActivity.class);
            intent.putExtra("selectedEvent", event);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventDate, eventLocation;
        ImageView eventImage;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.textEventName);
            eventDate = itemView.findViewById(R.id.textEventDate);
            eventLocation = itemView.findViewById(R.id.textEventLocation);
            eventImage = itemView.findViewById(R.id.imageEvent);
        }
    }
}
