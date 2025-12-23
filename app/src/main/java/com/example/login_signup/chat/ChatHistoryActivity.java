package com.example.login_signup.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton; // Import ImageButton
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_signup.R;
import com.example.login_signup.classes.FirebaseRepo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatHistoryActivity extends AppCompatActivity {

    private static final String TAG = "ChatHistoryActivity";

    private RecyclerView recyclerViewChatHistory;
    private ChatHistoryAdapter adapter;
    private List<ChatSession> sessionList = new ArrayList<>();
    private ImageButton btnBack;

    private FirebaseRepo fbRepo;

    void initViews(){
        recyclerViewChatHistory = findViewById(R.id.recyclerViewChatHistory);
        btnBack = findViewById(R.id.btn_back);
        FloatingActionButton fabNewChat = findViewById(R.id.fabNewChat);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewChatHistory.setLayoutManager(layoutManager);
        adapter = new ChatHistoryAdapter(sessionList);
        recyclerViewChatHistory.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewChatHistory.getContext(), layoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.list_divider));
        recyclerViewChatHistory.addItemDecoration(dividerItemDecoration);

        btnBack.setOnClickListener(v -> finish());
        fabNewChat.setOnClickListener(v -> createNewChatSession());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        fbRepo = new FirebaseRepo();

        initViews();
        loadChatSessions();
    }

    private void loadChatSessions() {
        fbRepo.listenForChatSessions(new FirebaseRepo.OnChatSessionsListener() {
            @Override
            public void onSessionsLoaded(List<ChatSession> sessions) {
                sessionList.clear();
                sessionList.addAll(sessions);
                adapter.notifyDataSetChanged();
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
                Intent intent = new Intent(ChatHistoryActivity.this, ChatActivity.class);
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
