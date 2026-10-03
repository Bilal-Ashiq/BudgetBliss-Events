package com.example.anew.chat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anew.R;
import com.example.anew.utils.OnChatClickListener;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
    private List<ChatMetadata> chatList;
    private String currentUserId;
    private Context context;

    private OnChatClickListener listener;

    public ChatListAdapter(List<ChatMetadata> chatList, String currentUserId, Context context, OnChatClickListener listener) {
        this.chatList = chatList;
        this.currentUserId = currentUserId;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMetadata chat = chatList.get(position);
        String otherUser = chat.participants.stream().filter(id -> !id.equals(currentUserId)).findFirst().orElse("Unknown");

        holder.textViewUser.setText(chat.otherUsername);
        holder.textViewLastMessage.setText(chat.lastMessage);

        holder.itemView.setOnClickListener(v -> {
            listener.onChatClick(chat, otherUser);
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUser, textViewLastMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUser = itemView.findViewById(R.id.textViewUser);
            textViewLastMessage = itemView.findViewById(R.id.textViewLastMessage);
        }
    }
}
