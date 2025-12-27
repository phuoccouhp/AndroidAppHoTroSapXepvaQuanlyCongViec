package com.example.login_signup.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_signup.R;
import com.example.login_signup.classes.FirebaseRepo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ChatHistoryFragment extends Fragment {

    private static final String TAG = "ChatHistoryFragment";

    private RecyclerView recyclerViewChatHistory;
    private ChatHistoryAdapter adapter;
    private List<ChatSession> sessionList = new ArrayList<>();

    private FirebaseRepo fbRepo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_history, container, false);

        fbRepo = new FirebaseRepo();
        initViews(view);
        loadChatSessions();

        return view;
    }

    void initViews(View view) {
        recyclerViewChatHistory = view.findViewById(R.id.recyclerViewChatHistory);
        FloatingActionButton fabNewChat = view.findViewById(R.id.fabNewChat);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerViewChatHistory.setLayoutManager(layoutManager);

        adapter = new ChatHistoryAdapter(sessionList);
        recyclerViewChatHistory.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewChatHistory.getContext(), layoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.list_divider));
        recyclerViewChatHistory.addItemDecoration(dividerItemDecoration);

        fabNewChat.setOnClickListener(v -> createNewChatSession());
    }

    private void loadChatSessions() {
        fbRepo.listenForChatSessions(new FirebaseRepo.OnChatSessionsListener() {
            @Override
            public void onSessionsLoaded(List<ChatSession> sessions) {
                sessionList.clear();
                sessionList.addAll(sessions);
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading chat sessions", e);
            }
        });
    }

    private void createNewChatSession() {
        fbRepo.createNewChatSession(new FirebaseRepo.OnCreateSessionListener() {
            @Override
            public void onSuccess(ChatSession newSession, String documentId) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("CHAT_SESSION_ID", documentId);
                intent.putExtra("CHAT_SESSION_NAME", newSession.getName());
                startActivity(intent);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error creating new chat session", e);
            }
        });
    }
}