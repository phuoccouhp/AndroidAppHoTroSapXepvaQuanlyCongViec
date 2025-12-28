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

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.SessionViewHolder> {

    private List<ChatSession> sessionList;
    private OnSessionLongClickListener longClickListener;

    public interface OnSessionLongClickListener {
        void onSessionLongClick(View view, ChatSession session);
    }

    public ChatHistoryAdapter(List<ChatSession> sessionList, OnSessionLongClickListener longClickListener) {
        this.sessionList = sessionList;
        this.longClickListener = longClickListener;
    }


    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_history, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        ChatSession session = sessionList.get(position);
        holder.textViewChatName.setText(session.getName());
        holder.textViewLastMessage.setText(session.getLastMessage());

        if (session.getLastUpdated() != null) {
            holder.textViewTimestamp.setText(getRelativeTime(session.getLastUpdated().getTime()));
        } else {
            holder.textViewTimestamp.setText("");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ChatActivity.class);
            intent.putExtra("CHAT_SESSION_ID", session.getId());
            intent.putExtra("CHAT_SESSION_NAME", session.getName());
            v.getContext().startActivity(intent);
        });

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

    private String getRelativeTime(long time) {
        return DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
    }

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
