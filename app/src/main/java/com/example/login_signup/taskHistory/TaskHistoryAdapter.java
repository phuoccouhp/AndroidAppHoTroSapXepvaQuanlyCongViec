package com.example.login_signup.taskHistory;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_signup.R;
import com.example.login_signup.classes.TaskLog;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TaskHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> items;

    public TaskHistoryAdapter(List<Object> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header_date, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_log, parent, false);
            return new LogViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            String headerTitle = (String) items.get(position);
            ((HeaderViewHolder) holder).tvHeader.setText(headerTitle);
        } else {
            TaskLog log = (TaskLog) items.get(position);
            LogViewHolder logHolder = (LogViewHolder) holder;

            String actionText;
            int actionColor = Color.parseColor("#2196F3");

            if ("CREATED".equals(log.getAction())) {
                actionText = "You added a task";
                actionColor = Color.parseColor("#2196F3"); //Blue
            } else if ("COMPLETED".equals(log.getAction())) {
                actionText = "You completed a task";
                actionColor = Color.parseColor("#4CAF50"); //Green
            } else if ("DELETED".equals(log.getAction())) {
                actionText = "You deleted a task";
                actionColor = Color.parseColor("#F44336"); //Red
            } else {
                actionText = "Updated task";
            }

            logHolder.tvContent.setText(actionText + ": " + log.getTaskTitle());
            logHolder.tvContent.setTextColor(actionColor);

            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            logHolder.tvTime.setText(sdf.format(log.getTimestamp()));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        HeaderViewHolder(View itemView) {
            super(itemView);
            if (itemView instanceof TextView) {
                tvHeader = (TextView) itemView;
            } else {
                tvHeader = (TextView) itemView;
            }
        }
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;
        LogViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}