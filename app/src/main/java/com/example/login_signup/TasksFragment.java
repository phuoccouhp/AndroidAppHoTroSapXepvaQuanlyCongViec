package com.example.login_signup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_signup.classes.FirebaseRepo;
import com.example.login_signup.classes.Task;
import com.example.login_signup.task.TaskAdapter;
import com.example.login_signup.task.TaskDetailActivity;
import com.example.login_signup.task.TaskWidgetProvider;
import com.example.login_signup.taskHistory.TaskHistoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TasksFragment extends Fragment {
    private RecyclerView rvHighPriority, rvIncomplete, rvCompleted;
    private ProgressBar progressBarTask;
    private TextView tvProgressPercent, tvProgressCount, tvHighPriorityCount, tvCompletedCount, tvPendingCount;

    private ImageButton btnFilterHigh, btnFilterIncomplete, btnFilterCompleted, btnHistory;

    private TaskAdapter adapterHighPriority, adapterIncomplete, adapterCompleted;

    private List<Task> allTasks = new ArrayList<>();
    private List<Task> highPriorityList = new ArrayList<>();
    private List<Task> incompleteList = new ArrayList<>();
    private List<Task> completedList = new ArrayList<>();

    private ActivityResultLauncher<Intent> taskDetailLauncher;
    private FirebaseRepo fbRepo;

    private String filterCategoryHigh = null;
    private String filterCategoryIncomplete = null;
    private String filterCategoryCompleted = null;

    private String todayDateString;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tasks, container, false);

        todayDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        progressBarTask = v.findViewById(R.id.progressBarTask);
        tvProgressPercent = v.findViewById(R.id.tvProgressPercent);
        tvProgressCount = v.findViewById(R.id.tvProgressCount);

        tvHighPriorityCount = v.findViewById(R.id.tvHighPriorityCount);
        tvCompletedCount = v.findViewById(R.id.tvCompletedCount);
        tvPendingCount = v.findViewById(R.id.tvPendingCount);

        rvHighPriority = v.findViewById(R.id.rvHighPriority);
        rvIncomplete = v.findViewById(R.id.rvIncomplete);
        rvCompleted = v.findViewById(R.id.rvCompleted);

        btnFilterHigh = v.findViewById(R.id.btnFilterHigh);
        btnFilterIncomplete = v.findViewById(R.id.btnFilterIncomplete);
        btnFilterCompleted = v.findViewById(R.id.btnFilterCompleted);
        btnHistory = v.findViewById(R.id.btnHistory);

        rvHighPriority.setLayoutManager(new LinearLayoutManager(getContext()));
        rvIncomplete.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCompleted.setLayoutManager(new LinearLayoutManager(getContext()));

        fbRepo = new FirebaseRepo();

        btnHistory.setOnClickListener(v1 -> {
            Intent intent = new Intent(getContext(), TaskHistoryActivity.class);
            v1.getContext().startActivity(intent);
        });

        TaskAdapter.OnTaskActionListener actionListener = new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onItemClick(Task task) {
                Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
                intent.putExtra("taskId", task.getId());
                taskDetailLauncher.launch(intent);
            }

            @Override
            public void onDeleteClick(Task task) {
                deleteTaskFromFirestore(task);
            }

            @Override
            public void onStatusClick(Task task) {
                boolean newStatus = !task.isCompleted();
                updateTaskField(task, "completed", newStatus);
            }

            @Override
            public void onPriorityClick(Task task) {
                String current = task.getPriority();
                String newPriority = "High".equals(current) ? "Basic" : "High";
                updateTaskField(task, "priority", newPriority);
            }
        };

        adapterHighPriority = new TaskAdapter(highPriorityList, actionListener);
        adapterIncomplete = new TaskAdapter(incompleteList, actionListener);
        adapterCompleted = new TaskAdapter(completedList, actionListener);

        rvHighPriority.setAdapter(adapterHighPriority);
        rvIncomplete.setAdapter(adapterIncomplete);
        rvCompleted.setAdapter(adapterCompleted);

        taskDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        if (result.getData().getBooleanExtra("isTaskUpdated", false)) {
                            loadAllTasks();
                        }
                    }
                }
        );

        btnFilterHigh.setOnClickListener(view -> showFilterMenu(view, 1));
        btnFilterIncomplete.setOnClickListener(view -> showFilterMenu(view, 2));
        btnFilterCompleted.setOnClickListener(view -> showFilterMenu(view, 3));

        loadAllTasks();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllTasks();
    }

    // Hàm hiển thị Popup Menu chọn Tag
    private void showFilterMenu(View v, int type) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenu().add(Menu.NONE, 0, 0, "All");
        popup.getMenu().add(Menu.NONE, 1, 1, "Work");
        popup.getMenu().add(Menu.NONE, 2, 2, "Personal");
        popup.getMenu().add(Menu.NONE, 3, 3, "Health");
        popup.getMenu().add(Menu.NONE, 4, 4, "Shopping");

        popup.setOnMenuItemClickListener(item -> {
            String selected = item.getTitle().toString();
            if (selected.equals("All")) selected = null;

            if (type == 1) filterCategoryHigh = selected;
            else if (type == 2) filterCategoryIncomplete = selected;
            else if (type == 3) filterCategoryCompleted = selected;

            filterTasks(); // Chạy lại lọc
            return true;
        });
        popup.show();
    }

    private void loadAllTasks() {
        if (fbRepo.getCurrentUser() == null) return;

        fbRepo.loadTasksForUser(new FirebaseRepo.OnTasksLoadedListener() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                allTasks.clear();

                SimpleDateFormat sdfDisplayDate = new SimpleDateFormat("dd/MM", Locale.getDefault());

                for (Task t : tasks) {
                    if (t.getTaskDate() != null) {
                        t.setDate(sdfDisplayDate.format(t.getTaskDate()));
                    }
                    allTasks.add(t);
                }
                filterTasks();
            }

            @Override
            public void onError(Exception e) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading tasks", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filterTasks() {
        highPriorityList.clear();
        incompleteList.clear();
        completedList.clear();

        for (Task t : allTasks) {

            if (t.isCompleted()) {
                if (filterCategoryCompleted == null || t.getCategory().equals(filterCategoryCompleted)) {
                    completedList.add(t);
                }
            } else {
                String priority = t.getPriority();
                if ("High".equals(priority)) {
                    if (filterCategoryHigh == null || t.getCategory().equals(filterCategoryHigh)) {
                        highPriorityList.add(t);
                    }
                } else {
                    if (filterCategoryIncomplete == null || t.getCategory().equals(filterCategoryIncomplete)) {
                        incompleteList.add(t);
                    }
                }
            }
        }

        adapterHighPriority.notifyDataSetChanged();
        adapterIncomplete.notifyDataSetChanged();
        adapterCompleted.notifyDataSetChanged();
        updateProgressBar();
    }

    private void updateProgressBar() {
        List<Task> todayTasks = new ArrayList<>();
        SimpleDateFormat sdfCompare = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdfCompare.format(new Date());

        for(Task t : allTasks) {
            String tDate = sdfCompare.format(t.getTaskDate());
            if(tDate.equals(today)) todayTasks.add(t);
        }

        int todayTotal = todayTasks.size();
        int todayCompleted = 0;
        for (Task t : todayTasks) {
            if (t.isCompleted()) todayCompleted++;
        }
        tvProgressCount.setText(todayCompleted + "/" + todayTotal);
        int progress = 0;
        if (todayTotal > 0) {
            progress = (int) ((todayCompleted / (float) todayTotal) * 100);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            progressBarTask.setProgress(progress, true);
        } else {
            progressBarTask.setProgress(progress);
        }
        tvProgressPercent.setText(progress + "%");

        tvHighPriorityCount.setText(String.valueOf(highPriorityList.size()));
        tvCompletedCount.setText(String.valueOf(completedList.size()));
        tvPendingCount.setText(String.valueOf(incompleteList.size()));
    }

    private void updateTaskField(Task task, String field, Object value) {
        if (task.getId() == null) return;

        if ("completed".equals(field)) task.setCompleted((Boolean) value);
        if ("priority".equals(field)) task.setPriority((String) value);

        filterTasks();

        fbRepo.updateTaskField(task.getId(), field, value, (message, e) -> {
            if (e == null) {
                if ("completed".equals(field) && (Boolean) value) {
                    fbRepo.logTaskAction(task.getId(), task.getTitle(), "COMPLETED");
                }

                updateWidget();
                loadAllTasks();
            } else {
                Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                loadAllTasks();
            }
        });
    }

    private void deleteTaskFromFirestore(Task task) {
        if (task.getId() == null) return;
        fbRepo.deleteTask(task.getId(), task.getTitle(), (message, e) -> {
            if (e == null) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                    updateWidget();
                    loadAllTasks();
                }
            } else {
                Toast.makeText(getContext(), "Error deleting", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWidget() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), TaskWidgetProvider.class);
            intent.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] ids = android.appwidget.AppWidgetManager.getInstance(getContext()).getAppWidgetIds(
                    new android.content.ComponentName(getContext(), TaskWidgetProvider.class));
            intent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            getContext().sendBroadcast(intent);
        }
    }
}
