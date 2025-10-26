package com.example.login_signup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DocumentsFragment extends Fragment {

    private RecyclerView recyclerViewToday, recyclerViewFuture;
    private TaskAdapter adapterToday, adapterFuture;
    private List<Task> allTasks = new ArrayList<>();
    private List<Task> todayTasks = new ArrayList<>();
    private List<Task> futureTasks = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String selectedCategory = null;

    // Đổi LinearLayout -> ImageButton (đúng với file XML)
    private ImageButton btnWork, btnPersonal, btnHealth, btnShopping, btnHabit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_documents, container, false);

        recyclerViewToday = v.findViewById(R.id.recyclerViewTasksToday);
        recyclerViewFuture = v.findViewById(R.id.recyclerViewTasksFuture);

        recyclerViewToday.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewFuture.setLayoutManager(new LinearLayoutManager(getContext()));

        adapterToday = new TaskAdapter(todayTasks, task -> {
            Toast.makeText(getContext(), "Chọn: " + task.getTitle(), Toast.LENGTH_SHORT).show();
        }, task -> {
            Toast.makeText(getContext(), "Giữ vào: " + task.getTitle(), Toast.LENGTH_SHORT).show();
        });

        adapterFuture = new TaskAdapter(futureTasks, task -> {
            Toast.makeText(getContext(), "Chọn: " + task.getTitle(), Toast.LENGTH_SHORT).show();
        }, task -> {
            Toast.makeText(getContext(), "Giữ vào: " + task.getTitle(), Toast.LENGTH_SHORT).show();
        });

        recyclerViewToday.setAdapter(adapterToday);
        recyclerViewFuture.setAdapter(adapterFuture);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 🔹 Đồng bộ đúng ID trong XML
        btnWork = v.findViewById(R.id.btn_work);
        btnPersonal = v.findViewById(R.id.btn_personal);
        btnHealth = v.findViewById(R.id.btn_health);
        btnShopping = v.findViewById(R.id.btn_shopping);
        btnHabit = v.findViewById(R.id.btn_habit);

        // Gán sự kiện click
        setCategoryClick(btnWork, "Work");
        setCategoryClick(btnPersonal, "Personal");
        setCategoryClick(btnHealth, "Health");
        setCategoryClick(btnShopping, "Shopping");
        setCategoryClick(btnHabit, "Habit");

        loadAllTasks();
        return v;
    }

    private void setCategoryClick(View button, String category) {
        button.setOnClickListener(v -> {
            selectedCategory = category;
            filterTasks();
            Toast.makeText(getContext(), "Hiển thị: " + category, Toast.LENGTH_SHORT).show();

            // 🔹 Thêm hiệu ứng nhấn nhẹ (scale nhỏ + bóng)
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() ->
                            v.animate().scaleX(1f).scaleY(1f).setDuration(100)
                    ).start();

            v.setElevation(12f); // bóng nhẹ khi nhấn
            v.postDelayed(() -> v.setElevation(0f), 200);
        });
    }

    private void loadAllTasks() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        db.collection("tasks")
                .whereEqualTo("uid", uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allTasks.clear();
                        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date today = new Date();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Object rawDate = doc.get("taskDate");
                            String title = doc.getString("title");
                            String category = doc.getString("category");
                            boolean completed = doc.getBoolean("completed") != null && doc.getBoolean("completed");

                            String timeStr = "";
                            String dateStr = "";

                            if (rawDate instanceof com.google.firebase.Timestamp) {
                                Date taskDate = ((com.google.firebase.Timestamp) rawDate).toDate();
                                timeStr = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(taskDate);
                                dateStr = sdfDate.format(taskDate);
                            }

                            allTasks.add(new Task(title, category, timeStr, completed, dateStr));
                        }

                        filterTasks();
                    } else {
                        Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterTasks() {
        todayTasks.clear();
        futureTasks.clear();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayStr = sdf.format(new Date());

        for (Task t : allTasks) {
            if (selectedCategory == null || t.getCategory().equals(selectedCategory)) {
                if (t.getDate().equals(todayStr))
                    todayTasks.add(t);
                else
                    futureTasks.add(t);
            }
        }

        adapterToday.notifyDataSetChanged();
        adapterFuture.notifyDataSetChanged();
    }
}
