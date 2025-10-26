package com.example.login_signup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> taskList;
    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    // ✅ Constructor hỗ trợ click + long click
    public TaskAdapter(List<Task> taskList, OnItemClickListener clickListener, OnItemLongClickListener longClickListener) {
        this.taskList = taskList;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTitle.setText(task.getTitle());
        holder.tvCategory.setText(task.getCategory());
        holder.tvTime.setText(task.getTime());

        // ✅ đổi icon khi task hoàn thành
        holder.imgCheck.setImageResource(task.isCompleted()
                ? R.drawable.baseline_check_circle_24
                : R.drawable.baseline_radio_button_unchecked_24);

        // ✅ sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(task);
        });

        // ✅ sự kiện giữ (long click)
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onItemLongClick(task);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvTime;
        ImageView imgCheck;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvCategory = itemView.findViewById(R.id.tvTaskCategory);
            tvTime = itemView.findViewById(R.id.tvTaskTime);
            imgCheck = itemView.findViewById(R.id.img_check);
        }
    }

    // Giao diện listener
    public interface OnItemClickListener {
        void onItemClick(Task task);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Task task);
    }
}
