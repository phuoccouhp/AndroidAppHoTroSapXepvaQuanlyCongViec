package com.example.login_signup;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_signup.classes.FirebaseRepo;
import com.example.login_signup.classes.Task;
import com.example.login_signup.task.TaskAdapter;
import com.example.login_signup.task.TaskDetailFragment;
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

    private FirebaseRepo fbRepo;
    private String selectedCategory = null;
    private ImageButton btnAll, btnWork, btnPersonal, btnHealth, btnShopping;

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

        adapterToday = new TaskAdapter(todayTasks, this::openTaskDetail, this::deleteTaskFromFirestore);
        adapterFuture = new TaskAdapter(futureTasks, this::openTaskDetail, this::deleteTaskFromFirestore);

        recyclerViewToday.setAdapter(adapterToday);
        recyclerViewFuture.setAdapter(adapterFuture);

        fbRepo = new FirebaseRepo();

        btnAll = v.findViewById(R.id.btn_all);
        btnWork = v.findViewById(R.id.btn_work);
        btnPersonal = v.findViewById(R.id.btn_personal);
        btnHealth = v.findViewById(R.id.btn_health);
        btnShopping = v.findViewById(R.id.btn_shopping);

        btnAll.setOnClickListener(view -> {
            selectedCategory = null;
            filterTasks();
            view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(100))
                    .start();
        });

        setCategoryClick(btnWork, "Work");
        setCategoryClick(btnPersonal, "Personal");
        setCategoryClick(btnHealth, "Health");
        setCategoryClick(btnShopping, "Shopping");

        loadAllTasks();
        return v;
    }

    private void openTaskDetail(Task task) {
        TaskDetailFragment detailFragment = TaskDetailFragment.newInstance(task.getId());
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
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
        fbRepo.listenToAllTasks(new FirebaseRepo.OnTasksLoadedListener() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                allTasks.clear();
                allTasks.addAll(tasks);

                todayDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                filterTasks();
            }

            @Override
            public void onError(Exception e) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
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
        fbRepo.deleteTask(task.getId(), (message, e) -> {
            if (getContext() == null) return;

            if (e != null) {
                Toast.makeText(getContext(), "Error deleting task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
