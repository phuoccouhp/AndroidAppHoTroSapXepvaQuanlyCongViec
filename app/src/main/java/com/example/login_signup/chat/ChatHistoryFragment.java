package com.example.login_signup.chat;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_signup.R;
import com.example.login_signup.classes.FirebaseRepo;
import com.example.login_signup.taskHistory.TaskHistoryActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ChatHistoryFragment extends Fragment {

    private static final String TAG = "ChatHistoryFragment";

    private RecyclerView recyclerViewChatHistory;
    private ChatHistoryAdapter adapter;
    private List<ChatSession> sessionList = new ArrayList<>();
    private ImageButton btnHistory;

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

        adapter = new ChatHistoryAdapter(sessionList, (itemview, session) -> {
            showContextMenu(itemview, session);
        });
        recyclerViewChatHistory.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewChatHistory.getContext(), layoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.list_divider));
        recyclerViewChatHistory.addItemDecoration(dividerItemDecoration);

        btnHistory = view.findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(v ->{
            Intent intent = new Intent(getContext(), TaskHistoryActivity.class);
            startActivity(intent);
        });

        fabNewChat.setOnClickListener(v -> createNewChatSession());
    }

    private void showContextMenu(View view, ChatSession session) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        // Thêm các mục menu bằng code (hoặc dùng menu resource xml)
        popup.getMenu().add(0, 1, 0, "Rename");
        popup.getMenu().add(0, 2, 1, "Delete");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: // Đổi tên
                    showRenameDialog(session);
                    return true;
                case 2: // Xóa
                    showDeleteConfirmDialog(session);
                    return true;
                default:
                    return false;
            }
        });
        popup.show();
    }

    private void showRenameDialog(ChatSession session) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Rename the chat");

        final EditText input = new EditText(getContext());
        input.setText(session.getName());

        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                fbRepo.renameChatSession(session.getId(), newName, (message, e) -> {
                    if (e == null) {
                        Toast.makeText(getContext(), "Renamed successful", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Renamed error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteConfirmDialog(ChatSession session) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Delete the chat")
                .setMessage("Are you sure you want to delete this chat?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    fbRepo.deleteChatSession(session.getId(), (message, e) -> {
                        if (e == null) {
                            Toast.makeText(getContext(), "Deleted successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Deleted error", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
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