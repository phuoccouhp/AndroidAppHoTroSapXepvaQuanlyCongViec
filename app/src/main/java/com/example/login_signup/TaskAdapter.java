package com.example.login_signup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(Task task);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Task task);
    }

    public TaskAdapter(List<Task> taskList,
                       OnItemClickListener clickListener,
                       OnItemLongClickListener longClickListener) {
        this.taskList = taskList;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
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
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onItemLongClick(task);
            return true;
        });

        // 🗑️ Sự kiện xoá
        holder.btnDelete.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            if (task.getId() != null && !task.getId().isEmpty()) {
                db.collection("tasks").document(task.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            int pos = holder.getAdapterPosition();
                            taskList.remove(pos);
                            notifyItemRemoved(pos);
                            Toast.makeText(v.getContext(),
                                    "✅ Đã xoá: " + task.getTitle(),
                                    Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(v.getContext(),
                                    "⚠️ Xoá thất bại: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(v.getContext(),
                        "Không tìm thấy ID công việc để xoá!",
                        Toast.LENGTH_SHORT).show();
            }
        });
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
