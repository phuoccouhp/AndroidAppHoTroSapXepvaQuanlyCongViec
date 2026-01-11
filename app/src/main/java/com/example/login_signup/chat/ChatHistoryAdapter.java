package com.example.login_signup.chat;

import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_signup.R;

import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

// Adapater hiển thị danh sách các phiên trò chuyện (Chat Sessions)
public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.SessionViewHolder> {

    private List<ChatSession> sessionList; // Danh sách các phiên trò chuyện
    private OnSessionLongClickListener longClickListener;

    // Interface để xử lý sự kiện khi người dùng nhấn giữ vào một phiên trò chuyện
    public interface OnSessionLongClickListener {
        void onSessionLongClick(View view, ChatSession session);
    }

    public ChatHistoryAdapter(List<ChatSession> sessionList, OnSessionLongClickListener longClickListener) {
        this.sessionList = sessionList;
        this.longClickListener = longClickListener;
    }


    // Tạo giao diện cho từng mục trong danh sách lịch sử chat
    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Nạp layout cho từng mục trong danh sách lịch sử chat
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_history, parent, false);
        return new SessionViewHolder(view);
    }

    // Gán dữ liệu từ đối tượng ChatSession vào các thành phần giao diện
    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        // Lấy đối tượng ChatSession tại vị trí hiện tại
        ChatSession session = sessionList.get(position);

        // Hiển thị tên và tin nhắn cuối cùng
        holder.textViewChatName.setText(session.getName());
        holder.textViewLastMessage.setText(session.getLastMessage());

        // Hiển thị thời gian tương đối
        if (session.getLastUpdated() != null) {
            holder.textViewTimestamp.setText(getRelativeTime(session.getLastUpdated().getTime()));
        } else {
            holder.textViewTimestamp.setText("");
        }

        // Xử lý sự kiện nhấn vào để mở màn hình chat chi tiết (ChatActivity)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ChatActivity.class);

            // Truyền thông tin về phiên trò chuyện cho ChatActivity
            intent.putExtra("CHAT_SESSION_ID", session.getId());
            intent.putExtra("CHAT_SESSION_NAME", session.getName());

            // Khởi động ChatActivity
            v.getContext().startActivity(intent);
        });

        // Xử lý sự kiện nhấn giữ
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onSessionLongClick(v, session);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    // Chuyển đổi mốc thời gian sang dạng văn bản dễ đọc
    private String getRelativeTime(long time) {
        return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
    }

    // ViewHolder lưu trữ các thành phần giao diện cho mỗi mục trong danh sách lịch sử chat
    static class SessionViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imageViewAvatar;
        TextView textViewChatName;
        TextView textViewLastMessage;
        TextView textViewTimestamp;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            textViewChatName = itemView.findViewById(R.id.textViewChatName);
            textViewLastMessage = itemView.findViewById(R.id.textViewLastMessage);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
        }
    }
}
