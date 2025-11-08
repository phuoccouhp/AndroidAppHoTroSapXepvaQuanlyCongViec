package com.example.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton; // Import ImageButton
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ChatHistoryActivity extends AppCompatActivity {

    private static final String TAG = "ChatHistoryActivity";

    private RecyclerView recyclerViewChatHistory;
    private ChatHistoryAdapter adapter;
    private List<ChatSession> sessionList = new ArrayList<>();
    private ImageButton btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerViewChatHistory = findViewById(R.id.recyclerViewChatHistory);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewChatHistory.setLayoutManager(layoutManager);
        adapter = new ChatHistoryAdapter(sessionList);
        recyclerViewChatHistory.setAdapter(adapter);

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewChatHistory.getContext(), layoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.list_divider));
        recyclerViewChatHistory.addItemDecoration(dividerItemDecoration);

        FloatingActionButton fabNewChat = findViewById(R.id.fabNewChat);
        fabNewChat.setOnClickListener(v -> createNewChatSession());

        loadChatSessions();
    }

    private void loadChatSessions() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not logged in");
            return;
        }

        db.collection("chat_sessions")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("lastUpdated", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading chat sessions", error);
                        return;
                    }

                    sessionList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        ChatSession session = doc.toObject(ChatSession.class);
                        session.setId(doc.getId());
                        sessionList.add(session);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void createNewChatSession() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        db.collection("chat_sessions")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int newChatNumber = queryDocumentSnapshots.size() + 1;
                    String chatName = "Chat-" + newChatNumber;

                    ChatSession newSession = new ChatSession(chatName, currentUser.getUid());
                    newSession.setLastMessage("New chat started...");

                    db.collection("chat_sessions")
                            .add(newSession)
                            .addOnSuccessListener(documentReference -> {
                                newSession.setId(documentReference.getId());
                                newSession.setLastUpdated(new Date()); 
                                sessionList.add(0, newSession); 
                                adapter.notifyItemInserted(0);
                                recyclerViewChatHistory.scrollToPosition(0);
                                
                                Intent intent = new Intent(ChatHistoryActivity.this, ChatActivity.class);
                                intent.putExtra("CHAT_SESSION_ID", documentReference.getId());
                                intent.putExtra("CHAT_SESSION_NAME", chatName);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Error creating new chat session", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error counting chat sessions", e));
    }
}
