package com.example.login_signup;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Date selectedDate = new Date();

    private TextView tvTaskListLabel; // Thêm biến cho TextView tiêu đề
    private String todayDateString;   // Chuỗi ngày hôm nay

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        todayDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        calendarView = v.findViewById(R.id.calendarView);
        recyclerView = v.findViewById(R.id.recyclerViewTasks);
        tvTaskListLabel = v.findViewById(R.id.tvTaskListLabel); // Lấy TextView

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new TaskAdapter(taskList,
                task -> {
                    TaskDetailFragment detailFragment = TaskDetailFragment.newInstance(task);
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, detailFragment)
                            .addToBackStack(null)
                            .commit();
                },
                task -> {
                    deleteTaskFromFirestore(task);
                }
        );

        recyclerView.setAdapter(adapter);

        getParentFragmentManager().setFragmentResultListener(
                "task_updated_result",
                this,
                (requestKey, result) -> {
                    if (result.getBoolean("task_updated", false)) {
                        loadTasksForDate(selectedDate);
                    }
                }
        );

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth, 0, 0, 0);
            selectedDate = c.getTime();
            loadTasksForDate(selectedDate);
        });

        loadTasksForDate(selectedDate);
        return v;
    }

    private void loadTasksForDate(Date dateToLoad) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String selectedDayString = sdfDate.format(dateToLoad);
        if (selectedDayString.equals(todayDateString)) {
            tvTaskListLabel.setText("Your Task for Today");
        } else {
            SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
            tvTaskListLabel.setText("Task for " + sdfDisplay.format(dateToLoad));
        }

        db.collection("tasks")
                .whereEqualTo("uid", uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        taskList.clear();

                        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Object rawDate = doc.get("taskDate");
                            if (!(rawDate instanceof com.google.firebase.Timestamp)) continue;

                            Date taskDate = ((com.google.firebase.Timestamp) rawDate).toDate();
                            String taskDayString = sdfDate.format(taskDate);

                            if (taskDayString.equals(selectedDayString)) {
                                String id = doc.getId();
                                String title = doc.getString("title");
                                String category = doc.getString("category");
                                String note = doc.getString("note");
                                boolean completed = doc.getBoolean("completed") != null && doc.getBoolean("completed");
                                String timeStr = sdfTime.format(taskDate);
                                String dateStr = sdfDate.format(taskDate);

                                taskList.add(new Task(id, title, category, timeStr, completed, dateStr, note));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void deleteTaskFromFirestore(Task task) {
        if (task.getId() == null || task.getId().isEmpty()) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: Task ID is missing", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        db.collection("tasks").document(task.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                    }
                    loadTasksForDate(selectedDate);
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error deleting task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}