package com.example.anew.customer.customerhome.ui.chat_list;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.anew.chat.ChatActivity;
import com.example.anew.chat.ChatListAdapter;
import com.example.anew.chat.ChatMetadata;
import com.example.anew.databinding.FragmentChatListCustomerBinding;
import com.example.anew.utils.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class CustomerChatListFragment extends Fragment {

    private FragmentChatListCustomerBinding binding;
    private ChatListAdapter adapter;
    private List<ChatMetadata> chatList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentChatListCustomerBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        binding.recyclerViewChats.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ChatListAdapter(chatList, currentUserId, getActivity(), (chat, otherUser) -> {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("receiverId", chat.participants.get(0).equals(currentUserId) ? chat.participants.get(1) : chat.participants.get(0));
            intent.putExtra("chatId", chat.chatId);
            intent.putExtra("otherUserId", otherUser);
            intent.putExtra("otherUsername", chat.otherUsername);
            intent.putExtra("isVendor", false);
            getActivity().startActivity(intent);
        });
        binding.recyclerViewChats.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchChatList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void fetchChatList() {
        LoadingDialog.show(getActivity());
        db.collection("chats")
                .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    LoadingDialog.hide();
                    chatList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ChatMetadata metadata = doc.toObject(ChatMetadata.class);

                        if (metadata != null && metadata.participants != null && metadata.participants.contains(currentUserId)) {
                            metadata.chatId = doc.getId();

                            // Identify the other user
                            for (String uid : metadata.participants) {
                                if (!uid.equals(currentUserId)) {
                                    metadata.otherUserId = uid;

                                    // Fetch the username of the other user
                                    db.collection("vendors").document(uid).get()
                                            .addOnSuccessListener(userDoc -> {
                                                if (userDoc.exists()) {
                                                    metadata.otherUsername = userDoc.getString("userName");
                                                } else {
                                                    metadata.otherUsername = "Unknown";
                                                }

                                                // Add to list and update adapter
                                                chatList.add(metadata);
                                                adapter.notifyDataSetChanged();
                                            })
                                            .addOnFailureListener(e -> {
                                                metadata.otherUsername = "Unknown";
                                                chatList.add(metadata);
                                                adapter.notifyDataSetChanged();
                                            });

                                    break;
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> LoadingDialog.hide());
    }

}