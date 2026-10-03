package com.example.anew.chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anew.R;
import com.example.anew.utils.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatListAdapter adapter;
    private List<ChatMetadata> chatList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        recyclerView = findViewById(R.id.recyclerViewChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatListAdapter(chatList, currentUserId, this, (chat, otherUser) ->  {
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra("receiverId", chat.participants.get(0).equals(currentUserId) ? chat.participants.get(1) : chat.participants.get(0));
            intent.putExtra("chatId", chat.chatId);
            intent.putExtra("otherUserId", otherUser);
            intent.putExtra("otherUsername", chat.otherUsername);
            intent.putExtra("isVendor", false);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchChatList();
    }

    private void fetchChatList() {
        LoadingDialog.show(ChatListActivity.this);
        db.collection("chats")
//                .whereArrayContains("participants", currentUserId)
                .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    LoadingDialog.hide();
                    chatList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ChatMetadata metadata = doc.toObject(ChatMetadata.class);
                        if (metadata != null) {
                            metadata.chatId = doc.getId();
                            chatList.add(metadata);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    LoadingDialog.hide();
                });
    }
}
