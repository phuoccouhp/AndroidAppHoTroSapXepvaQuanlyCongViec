package com.example.login_signup;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.ConversationViewHolder> {

    private final Context context;
    private final List<Conversation> conversationList;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ConversationListAdapter(Context context, List<Conversation> conversationList) {
        this.context = context;
        this.conversationList = conversationList;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);

        // Assuming Conversation has a title or a way to represent it. 
        // For now, let's use a placeholder title with the conversation ID.
        holder.conversationTitle.setText("Chat: " + conversation.getId());
        holder.lastMessage.setText(conversation.getLastMessage());

        if (conversation.getTimestamp() != null) {
            holder.timestamp.setText(sdf.format(conversation.getTimestamp()));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("conversationId", conversation.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView conversationTitle;
        TextView lastMessage;
        TextView timestamp;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            conversationTitle = itemView.findViewById(R.id.conversation_title);
            lastMessage = itemView.findViewById(R.id.last_message);
            timestamp = itemView.findViewById(R.id.timestamp);
        }
    }
}
