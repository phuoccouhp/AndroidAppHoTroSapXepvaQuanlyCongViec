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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = v.findViewById(R.id.calendarView);
        recyclerView = v.findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // ✅ Adapter có cả click và long click
        adapter = new TaskAdapter(
                taskList,
                new TaskAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Task task) {
                        Toast.makeText(getContext(), "Chọn: " + task.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                },
                new TaskAdapter.OnItemLongClickListener() {
                    @Override
                    public void onItemLongClick(Task task) {
                        Toast.makeText(getContext(), "Giữ vào: " + task.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // ✅ Sửa lại listener chuẩn cú pháp
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar c = Calendar.getInstance();
                c.set(year, month, dayOfMonth);
                loadTasksForDate(c.getTime());
            }
        });


        loadTasksForDate(new Date());
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
                                    String title = doc.getString("title");
                                    String category = doc.getString("category");
                                    String timeStr = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(taskDate);
                                    boolean completed = doc.getBoolean("completed") != null && doc.getBoolean("completed");

                                    taskList.add(new Task(title, category, timeStr, completed));
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
