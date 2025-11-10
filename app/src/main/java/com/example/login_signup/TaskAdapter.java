package com.example.login_signup;

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

        holder.tvTaskTitle.setText(task.getTitle());
        holder.tvTaskCategory.setText(task.getCategory());
        holder.tvTaskTime.setText(task.getTime());
        holder.itemView.setOnClickListener(v -> clickListener.onItemClick(task));

        holder.btnDeleteTask.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Task taskToDelete = taskList.get(pos);
                deleteClickListener.onDeleteClick(taskToDelete);
            }
        });


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

                iconResId = R.drawable.baseline_check_circle_24;
                break;
        }
        holder.imgCategory.setImageResource(iconResId);


        holder.tvTaskCategory.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTitle, tvTaskCategory, tvTaskTime;
        ImageButton btnDeleteTask;
        ImageView imgCategory;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tvAlarmHeader);
            tvTaskCategory = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskTime = itemView.findViewById(R.id.tvTaskTime);
            btnDeleteTask = itemView.findViewById(R.id.btnDeleteTask);
            imgCategory = itemView.findViewById(R.id.imgCategory);
        }
    }
}