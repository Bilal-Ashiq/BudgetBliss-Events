package com.example.anew.chat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anew.R;
import com.example.anew.customer.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText inputMessage;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;

    private TextView heading;

    private FirebaseFirestore db;
    private String senderId, receiverId, chatId;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        heading = findViewById(R.id.heading);

        senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverId = getIntent().getStringExtra("receiverId");
        if (getIntent().getBooleanExtra("isVendor", false)) {
            path = "vendors";
        } else {
            path = "users";
        }

        String otherUsername = getIntent().getStringExtra("otherUsername");
        setTitle(otherUsername);

        chatId = getChatId(senderId, receiverId);

        db = FirebaseFirestore.getInstance();
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, senderId);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        listenForMessages();

        sendButton.setOnClickListener(v -> {
            String msg = inputMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                sendMessage0(msg);
                inputMessage.setText("");
            }
        });
    }

    private String getChatId(String uid1, String uid2) {
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }

    private void sendMessage(String messageText) {
        db.collection("users").document(senderId).get().addOnSuccessListener(userDoc -> {
            if (userDoc.exists()) {
                String senderUsername = userDoc.getString("username");
                Message message = new Message(senderId, receiverId, messageText, System.currentTimeMillis(), senderUsername);

                // Save message
                db.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .add(message);

                // Update metadata with last message
                Map<String, Object> metadataUpdate = new HashMap<>();
                metadataUpdate.put("lastMessage", messageText);
                metadataUpdate.put("lastTimestamp", System.currentTimeMillis());
                metadataUpdate.put("participants", Arrays.asList(senderId, receiverId));

                db.collection("chats").document(chatId).set(metadataUpdate, SetOptions.merge());
            }
        });
    }


    private void sendMessage0(String messageText) {
        // Fetch the current user's username from Firestore

        db.collection(path).document(senderId).get().addOnSuccessListener(userDoc -> {
            if (userDoc.exists()) {
                String senderUsername = userDoc.getString("userName");

                // Create the message with username included
                Message msg = new Message(senderId, receiverId, messageText, System.currentTimeMillis(), senderUsername);

                // Save the message
                db.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .add(msg)
                        .addOnSuccessListener(documentReference -> {
                            // Save metadata directly on the chat document
                            HashMap<String, Object> metadata = new HashMap<>();
                            metadata.put("participants", Arrays.asList(senderId, receiverId));
                            metadata.put("lastMessage", messageText);
                            metadata.put("lastTimestamp", System.currentTimeMillis());

                            db.collection("chats")
                                    .document(chatId)
                                    .set(metadata, SetOptions.merge());
                        });
            } else {
                Toast.makeText(ChatActivity.this, "sending ... ", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(ChatActivity.this, "Failed to fetch username", Toast.LENGTH_SHORT).show();
        });
    }


    private void listenForMessages() {
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        messageList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            messageList.add(doc.toObject(Message.class));
                        }
                        chatAdapter.notifyDataSetChanged();
                        chatRecyclerView.scrollToPosition(messageList.size() - 1);
                    }
                });
    }

    private void setTitle(String otherUsername) {
        heading.setText(otherUsername);
    }
}
