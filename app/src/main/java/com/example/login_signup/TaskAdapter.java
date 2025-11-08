package com.example.login_signup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnItemClickListener clickListener;
    private OnDeleteClickListener deleteClickListener;

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }
    public interface OnDeleteClickListener {
        void onDeleteClick(Task task);
    }

    public TaskAdapter(List<Task> taskList,
                       OnItemClickListener clickListener,
                       OnDeleteClickListener deleteClickListener) {
        this.taskList = taskList;
        this.clickListener = clickListener;
        this.deleteClickListener = deleteClickListener;
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

        holder.tvTitle.setText(task.getTitle());
        holder.tvCategory.setText(task.getCategory());
        holder.tvTime.setText(task.getTime());
        holder.itemView.setOnClickListener(v -> clickListener.onItemClick(task));

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Task taskToDelete = taskList.get(pos);
                deleteClickListener.onDeleteClick(taskToDelete);
            }
        });

        // FIX: Change the main task icon (img_check) to the category icon.
        int iconResId;
        switch (task.getCategory()) {
            case "Work":
                iconResId = R.drawable.baseline_work_24;
                break;
            case "Personal":
                iconResId = R.drawable.baseline_person_24;
                break;
            case "Health":
                iconResId = R.drawable.baseline_health_24;
                break;
            case "Shopping":
                iconResId = R.drawable.baseline_shopping_cart_24;
                break;
            default:
                // A default icon if the category doesn't match
                iconResId = R.drawable.baseline_check_circle_24;
                break;
        }
        holder.imgCheck.setImageResource(iconResId);

        // FIX: Remove the compound drawable from the TextView to ensure only one icon is displayed.
        holder.tvCategory.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvTime;
        ImageButton btnDelete;
        ImageView imgCheck;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvCategory = itemView.findViewById(R.id.tvTaskCategory);
            tvTime = itemView.findViewById(R.id.tvTaskTime);
            btnDelete = itemView.findViewById(R.id.btnDeleteTask);
            imgCheck = itemView.findViewById(R.id.img_check);
        }
    }
}