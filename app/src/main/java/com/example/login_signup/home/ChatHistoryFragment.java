package com.example.login_signup.home;

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
import com.example.login_signup.chat.ChatActivity;
import com.example.login_signup.chat.ChatHistoryAdapter;
import com.example.login_signup.chat.ChatSession;
import com.example.login_signup.classes.FirebaseRepo;
import com.example.login_signup.history.TaskHistoryActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

// ChatHistory Fragment: Quản lý danh sách các cuộc hội thoại của người dùng với AI
public class ChatHistoryFragment extends Fragment {
    private FirebaseRepo fbRepo;
    private static final String TAG = "ChatHistoryFragment";

    // Các đối tượng thành phần giao diện
    private RecyclerView recyclerViewChatHistory;
    private ImageButton btnHistory;
    FloatingActionButton fabNewChat;

    // Quản lý giao diện cho danh sách các phiên chat
    private ChatHistoryAdapter adapter;
    private List<ChatSession> sessionList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_history, container, false);

        fbRepo = new FirebaseRepo();
        initViews(view);
        loadChatSessions();

        return view;
    }

    // Khởi tạo các thành phần giao diện và cấu hình RecyclerView
    void initViews(View view) {
        // Ánh xạ các thành phần giao diện
        recyclerViewChatHistory = view.findViewById(R.id.recyclerViewChatHistory);
        btnHistory = view.findViewById(R.id.btnHistory);
        fabNewChat = view.findViewById(R.id.fabNewChat);

        // Cấu hình RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerViewChatHistory.setLayoutManager(layoutManager);

        // Khởi tạo adapter với sự kiện nhấn giữ (Long Click) để hiện menu chức năng
        adapter = new ChatHistoryAdapter(sessionList, (itemview, session) -> {
            showContextMenu(itemview, session);
        });
        recyclerViewChatHistory.setAdapter(adapter);

        // Thêm đường kẻ phân cách giữa các dòng trong danh sách
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewChatHistory.getContext(), layoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.list_divider));
        recyclerViewChatHistory.addItemDecoration(dividerItemDecoration);

        // Mở TaskHistoryActivity khi nhấn nút lịch sử
        btnHistory.setOnClickListener(v ->{
            Intent intent = new Intent(getContext(), TaskHistoryActivity.class);
            startActivity(intent);
        });

        // Nút nổi (FAB) để bắt đầu một phiên chat mới
        fabNewChat.setOnClickListener(v -> createNewChatSession());
    }

    // Hiển thị Menu Context (Rename, Delete) khi người dùng nhấn giữ một phiên chat
    private void showContextMenu(View view, ChatSession session) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        // Menu với các tùy chọn: Rename, Delete
        popup.getMenu().add(0, 1, 0, "Rename");
        popup.getMenu().add(0, 2, 1, "Delete");

        // Xử lý sự kiện khi người dùng chọn một tùy chọn trong Menu
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: // Chọn Đổi tên
                    showRenameDialog(session); // Hiển thị hộp thoại nhập tên mới
                    return true;
                case 2: // Chọn Xóa
                    showDeleteConfirmDialog(session); // Hiển thị hộp thoại xác nhận xóa
                    return true;
                default:
                    return false;
            }
        });
        popup.show(); // Hiển thị Menu Context
    }

    // Hiển thị hộp thoại nhập tên mới cho phiên chat
    private void showRenameDialog(ChatSession session) {
        if (getContext() == null) return;

        // Tạo hộp thoại dialog để đổi tên phiên chat
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Tạo EditText để nhập tên mới cho phiên chat
        final EditText input = new EditText(getContext());
        input.setText(session.getName());

        builder.setTitle("Rename the chat"); // Đặt tiêu đề cho hộp thoại
        builder.setView(input); // Đặt EditText vào hộp thoại

        // Xử lý sự kiện khi người dùng nhấn nút OK trong hộp thoại
        builder.setPositiveButton("OK", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                // Gọi phương thức cập nhật tên mới của phiên chat trên Firebase
                fbRepo.renameChatSession(session.getId(), newName, (message, e) -> {
                    if (e == null) {
                        Toast.makeText(getContext(), "Renamed successful", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Renamed error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        // Ẩn hộp thoại khi người dùng nhất nút Cancel
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Hiển thị hộp thoại dialog
        builder.show();
    }

    // Hiển thị hộp thoại xác nhận trước khi xóa phiên chat
    private void showDeleteConfirmDialog(ChatSession session) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Delete the chat") // Tiêu đề của hộp thoại
                .setMessage("Are you sure you want to delete this chat?") // Nội dung của hộp thoại

                // Xử lý sự kiện khi người dùng nhấn nút Delete trong hộp thoại
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Gọi phương thức xóa phiên chat trên Firebase
                    fbRepo.deleteChatSession(session.getId(), (message, e) -> {
                        if (e == null) {
                            Toast.makeText(getContext(), "Deleted successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Deleted error", Toast.LENGTH_SHORT).show();
                        }
                    });
                })

                // Ẩn hộp thoại khi người dùng nhất nút Cancel
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Lắng nghe và cập nhật danh sách các phiên chat từ Firebase theo thời gian thực
    private void loadChatSessions() {
        // Gọi phương thức lấy danh sách các phiên chat từ Firebase
        fbRepo.listenForChatSessions(new FirebaseRepo.OnChatSessionsListener() {
            @Override
            public void onSessionsLoaded(List<ChatSession> sessions) {
                sessionList.clear();
                sessionList.addAll(sessions); // Cập nhật danh sách phiên chat
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

    // Tạo một cuộc trò chuyện mới và chuyển hướng sang màn hình ChatActivity
    private void createNewChatSession() {
        // Gọi phương thức tạo một phiên chat mới trên Firebase
        fbRepo.createNewChatSession(new FirebaseRepo.OnCreateSessionListener() {
            @Override
            public void onSuccess(ChatSession newSession, String documentId) {
                // Tạo đối tượng intent để truyền dữ liệu và chuyển hướng đến ChatActivity
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
