package com.example.login_signup;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskActionListener actionListener;

    public interface OnTaskActionListener {
        void onItemClick(Task task);
        void onDeleteClick(Task task);
        void onStatusClick(Task task);
        void onPriorityClick(Task task);
    }

    public TaskAdapter(List<Task> taskList, OnTaskActionListener actionListener) {
        this.taskList = taskList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTaskTitle.setText(task.getTitle());
        holder.tvTags.setText(task.getCategory());
        holder.tvTaskTime.setText(task.getTime());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvTaskDay.setText(sdf.format(task.getTaskDate()));

        if (task.isCompleted()) {
            holder.btnStatus.setText("Done");
            holder.btnStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#62EF63")));
        } else {
            holder.btnStatus.setText("Unfinished");
            holder.btnStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B0BEC5")));
        }

        if ("High".equals(task.getPriority())) {
            holder.btnPriority.setText("Priority");
            holder.btnPriority.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9F0B")));
        } else {
            holder.btnPriority.setText("Normal");
            holder.btnPriority.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#64B5F6")));
        }

        int iconResId, tagColor;
        String category = task.getCategory();

        if (category == null) category = "Other";

        switch (category) {
            case "Work":
                iconResId = R.drawable.baseline_work_24;
                tagColor = Color.parseColor("#FF9800"); // Màu Cam
                break;
            case "Personal":
                iconResId = R.drawable.baseline_person_24;
                tagColor = Color.parseColor("#2196F3"); // Màu Xanh dương
                break;
            case "Health":
                iconResId = R.drawable.baseline_health_24;
                tagColor = Color.parseColor("#E91E63"); // Màu Hồng
                break;
            case "Shopping":
                iconResId = R.drawable.baseline_shopping_cart_24;
                tagColor = Color.parseColor("#4CAF50"); // Màu Xanh lá
                break;
            default:
                iconResId = R.drawable.baseline_check_circle_24;
                tagColor = Color.parseColor("#9E9E9E"); // Màu Xám mặc định
                break;
        }
        holder.imgTags.setImageResource(iconResId);
        holder.imgTags.setColorFilter(tagColor);

        holder.itemView.setOnClickListener(v -> actionListener.onItemClick(task));

        holder.btnDeleteTask.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                actionListener.onDeleteClick(taskList.get(pos));
            }
        });

        holder.btnStatus.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                actionListener.onStatusClick(taskList.get(pos));
            }
        });

        holder.btnPriority.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                actionListener.onPriorityClick(taskList.get(pos));
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTitle, tvTags, tvTaskTime, tvTaskDay;
        Button btnStatus, btnPriority, btnDeleteTask;;
        ImageView imgTags;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTags = itemView.findViewById(R.id.tvTags);
            imgTags = itemView.findViewById(R.id.imgTags);

            tvTaskTime = itemView.findViewById(R.id.tvTaskTime);
            tvTaskDay = itemView.findViewById(R.id.tvTaskDay);

            btnDeleteTask = itemView.findViewById(R.id.btnDeleteTask);
            btnStatus = itemView.findViewById(R.id.btnStatus);
            btnPriority = itemView.findViewById(R.id.btnPriority);
        }
    }
}