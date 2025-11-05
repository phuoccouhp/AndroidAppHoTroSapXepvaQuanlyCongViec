package com.example.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ConversationListActivity extends AppCompatActivity {

    private static final String TAG = "ConvListActivity";

    private RecyclerView conversationsRecyclerView;
    private ConversationListAdapter adapter;
    private List<Conversation> conversationList;
    private FloatingActionButton fabNewConversation;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";

        initUI();
        loadConversations();
    }

    private void initUI() {
        conversationsRecyclerView = findViewById(R.id.conversationsRecyclerView);
        fabNewConversation = findViewById(R.id.fab_new_conversation);

        conversationList = new ArrayList<>();
        adapter = new ConversationListAdapter(this, conversationList);
        
        conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        conversationsRecyclerView.setAdapter(adapter);

        fabNewConversation.setOnClickListener(v -> {
            Intent intent = new Intent(ConversationListActivity.this, ChatActivity.class);
            // No conversationId is passed, so ChatActivity will know it's a new chat
            startActivity(intent);
        });
    }

    private void loadConversations() {
        if (userId.equals("anonymous")) {
            Log.w(TAG, "No user logged in, cannot load conversations.");
            return;
        }

        db.collection("users").document(userId).collection("conversations")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        conversationList.clear();
                        for (var doc : snapshots.getDocuments()) {
                            Conversation conversation = doc.toObject(Conversation.class);
                            if (conversation != null) {
                                conversation.setId(doc.getId());
                                conversationList.add(conversation);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The listener in loadConversations will handle updates automatically,
        // so a manual refresh might not be needed unless you want to force it.
    }
}
