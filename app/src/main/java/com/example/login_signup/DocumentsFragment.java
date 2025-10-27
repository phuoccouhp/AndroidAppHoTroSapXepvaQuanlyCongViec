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

public class DocumentsFragment extends Fragment {

    private RecyclerView recyclerViewToday, recyclerViewFuture;
    private TaskAdapter adapterToday, adapterFuture;

    private List<Task> allTasks = new ArrayList<>();
    private List<Task> todayTasks = new ArrayList<>();
    private List<Task> futureTasks = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String selectedCategory = null;
    private ImageButton btnWork, btnPersonal, btnHealth, btnShopping, btnHabit;

    private String todayDateString;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_documents, container, false);

        todayDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        recyclerViewToday = v.findViewById(R.id.recyclerViewTasksToday);
        recyclerViewFuture = v.findViewById(R.id.recyclerViewTasksFuture);
        recyclerViewToday.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewFuture.setLayoutManager(new LinearLayoutManager(getContext()));

        adapterToday = new TaskAdapter(todayTasks, task -> {
            TaskDetailFragment detailFragment = TaskDetailFragment.newInstance(task);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }, task -> {
            deleteTaskFromFirestore(task);
        });

        adapterFuture = new TaskAdapter(futureTasks, task -> {
            TaskDetailFragment detailFragment = TaskDetailFragment.newInstance(task);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }, task -> {
            deleteTaskFromFirestore(task);
        });

        recyclerViewToday.setAdapter(adapterToday);
        recyclerViewFuture.setAdapter(adapterFuture);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnWork = v.findViewById(R.id.btn_work);
        btnPersonal = v.findViewById(R.id.btn_personal);
        btnHealth = v.findViewById(R.id.btn_health);
        btnShopping = v.findViewById(R.id.btn_shopping);
        btnHabit = v.findViewById(R.id.btn_habit);

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
            if (Objects.equals(selectedCategory, category)) {
                selectedCategory = null;
            } else {
                selectedCategory = category;
            }

            filterTasks();

            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100))
                    .start();
        });
    }

    private void loadAllTasks() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) return;

        db.collection("tasks")
                .whereEqualTo("uid", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        return; // Lỗi
                    }

                    allTasks.clear();

                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

                    for (QueryDocumentSnapshot doc : value) {
                        Object rawDate = doc.get("taskDate");
                        if (!(rawDate instanceof com.google.firebase.Timestamp)) continue;

                        Date taskDate = ((com.google.firebase.Timestamp) rawDate).toDate();

                        String id = doc.getId();
                        String title = doc.getString("title");
                        String category = doc.getString("category");
                        String note = doc.getString("note");
                        boolean completed = doc.getBoolean("completed") != null && doc.getBoolean("completed");
                        String timeStr = sdfTime.format(taskDate);
                        String dateStr = sdfDate.format(taskDate);

                        allTasks.add(new Task(id, title, category, timeStr, completed, dateStr, note));
                    }

                    todayDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                    filterTasks();
                });
    }

    private void filterTasks() {
        todayTasks.clear();
        futureTasks.clear();

        for (Task t : allTasks) {
            if (selectedCategory == null || t.getCategory().equals(selectedCategory)) {

                String taskDateStr = t.getDate();

                if (taskDateStr.equals(todayDateString)) {
                    todayTasks.add(t);
                } else if (taskDateStr.compareTo(todayDateString) > 0) {
                    futureTasks.add(t);
                }
            }
        }

        adapterToday.notifyDataSetChanged();
        adapterFuture.notifyDataSetChanged();
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
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error deleting task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}