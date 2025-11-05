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
        holder.bind(conversation);
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView lastMessageTextView;
        TextView timestampTextView;

        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.conversationTitleTextView);
            lastMessageTextView = itemView.findViewById(R.id.lastMessageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Conversation conversation = conversationList.get(position);
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("conversationId", conversation.getId());
                    context.startActivity(intent);
                }
            });
        }

        void bind(Conversation conversation) {
            // The title could be the first message or a generated title
            titleTextView.setText("Cuộc trò chuyện - " + conversation.getId().substring(0, 5));
            lastMessageTextView.setText(conversation.getLastMessage());

            if (conversation.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM", Locale.getDefault());
                timestampTextView.setText(sdf.format(conversation.getTimestamp()));
            }
        }
    }
}
