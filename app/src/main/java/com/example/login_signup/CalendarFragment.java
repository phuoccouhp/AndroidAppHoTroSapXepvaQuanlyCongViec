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
    private Date selectedDate = new Date(); // 🟢 lưu ngày đang chọn để reload

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = v.findViewById(R.id.calendarView);
        recyclerView = v.findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TaskAdapter(taskList,
                new TaskAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Task task) {
                        // ✅ Khi nhấn vào task -> mở TaskDetailFragment
                        TaskDetailFragment detailFragment = TaskDetailFragment.newInstance(task);

                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, detailFragment)
                                .addToBackStack(null) // cho phép quay lại
                                .commit();
                    }
                },
                new TaskAdapter.OnItemLongClickListener() {
                    @Override
                    public void onItemLongClick(Task task) {
                        // Có thể thêm chức năng xoá nếu cần
                    }
                }
        );

        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 🟢 Lắng nghe kết quả từ TaskDetailFragment
        getParentFragmentManager().setFragmentResultListener(
                "task_updated_result",
                this,
                (requestKey, result) -> {
                    if (result.getBoolean("task_updated", false)) {
                        loadTasksForDate(selectedDate); // reload dữ liệu khi task thay đổi
                    }
                }
        );

        // 🟢 Khi người dùng chọn ngày
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth, 0, 0, 0);
            selectedDate = c.getTime(); // lưu lại ngày đang chọn
            loadTasksForDate(selectedDate);
        });

        // 🟢 Mặc định tải task hôm nay
        loadTasksForDate(selectedDate);
        return v;
    }

    private void loadTasksForDate(Date selectedDate) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        db.collection("tasks")
                .whereEqualTo("uid", uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        taskList.clear();
                        SimpleDateFormat sdfDay = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                        String selectedDay = sdfDay.format(selectedDate);

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Object rawDate = doc.get("taskDate");

                            if (rawDate instanceof com.google.firebase.Timestamp) {
                                Date taskDate = ((com.google.firebase.Timestamp) rawDate).toDate();
                                String taskDay = sdfDay.format(taskDate);

                                if (taskDay.equals(selectedDay)) {
                                    String id = doc.getId();
                                    String title = doc.getString("title");
                                    String category = doc.getString("category");
                                    String note = doc.getString("note");
                                    boolean completed = doc.getBoolean("completed") != null && doc.getBoolean("completed");
                                    String timeStr = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(taskDate);

                                    // ✅ Gán id + note vào Task
                                    taskList.add(new Task(id, title, category, timeStr, completed, taskDay, note));
                                }
                            }
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
