package com.example.anew.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.anew.R;
import com.example.anew.customer.models.Event;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter2 extends RecyclerView.Adapter<EventAdapter2.EventViewHolder> {

    private List<Event> eventList;
    private List<Event> fullEventList = new ArrayList<>();
    private Context context;

    OnEventClickListener listener;

    public EventAdapter2(Context context, List<Event> eventList, OnEventClickListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
        fullEventList.addAll(eventList);
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.evnet_item2, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventName.setText(event.getEventName());
        holder.eventDate.setText(event.getEventDate());
        holder.eventLocation.setText(event.getEventLocation());

        Glide.with(context.getApplicationContext())
                .load(event.getEventPicUrl())
                .placeholder(R.drawable.baseline_image_24)
                .into(holder.eventImage);

        holder.itemView.setOnClickListener(v -> {
            listener.onItemClick(event);
        });
    }

    public void filter(String query) {
        if (fullEventList.isEmpty()) {
            fullEventList.addAll(eventList);
        }
        eventList.clear();

        if (query.isEmpty()) {
            eventList.addAll(fullEventList);
        } else {
            List<Event> tempList = new ArrayList<>();
            for (Event item : fullEventList) {
                if (item.eventName.toLowerCase().contains(query.toLowerCase())
                        || item.eventDescription.toLowerCase().contains(query.toLowerCase())) {
                    tempList.add(item);
                }
            }

            eventList.addAll(tempList);
        }

        notifyDataSetChanged();
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
