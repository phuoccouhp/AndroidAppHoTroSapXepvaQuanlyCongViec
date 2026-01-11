package com.example.login_signup.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_signup.R;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

// Adapter điều phối hiển thị danh sách tin nhắn trong giao diện hội thoại.
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    // Định nghĩa các loại tin nhắn để hiển thị giao diện khác nhau
    private static final int VIEW_TYPE_USER = 1; // Tin nhắn từ người dùng
    private static final int VIEW_TYPE_AI = 2;   // Tin nhắn từ trợ lý ảo AI

    private List<ChatMessage> messageList;
    private String currentUserId;

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
        // Lấy UID của người dùng hiện tại để làm căn cứ phân biệt tin nhắn gửi đi và nhận về
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
    }

    // Xác định loại view cho từng vị trí tin nhắn dựa trên UID người gửi
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position); // Lấy tin nhắn tại vị trí hiện tại

        // Hiển thị layout của User nếu là tin nhắn của người dùng
        if (message.getUserId().equals(currentUserId)) {
            return VIEW_TYPE_USER;
        }
        // Ngược lại thì hiển thị layout của AI
        else {
            return VIEW_TYPE_AI;
        }
    }

    // Khởi tạo layout tương ứng cho từng loại tin nhắn.
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_USER) {
            // Nạp layout cho tin nhắn người dùng, nằm ở bên phải
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
        } else {
            // Nạp layout cho tin nhắn AI, nằm ở bên trái
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_ai, parent, false);
        }
        return new MessageViewHolder(view);
    }

    // Gán dữ liệu nội dung tin nhắn vào TextView.
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        holder.textViewMessage.setText(message.getMessage());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder lưu trữ các thành phần giao diện cho mỗi tin nhắn
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
        }
    }
}
