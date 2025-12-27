package com.example.login_signup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList = new ArrayList<>();
    private FirebaseRepo fbRepo;
    private Date selectedDate = new Date();

    private TextView tvTaskList;
    private String todayDateString;

    private ActivityResultLauncher<Intent> taskDetailLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        todayDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        calendarView = v.findViewById(R.id.calendarView);
        recyclerView = v.findViewById(R.id.rvTasks);
        tvTaskList = v.findViewById(R.id.tvTaskList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fbRepo = new FirebaseRepo();

        adapter = new TaskAdapter(taskList, new TaskAdapter.OnTaskActionListener() {
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
        });
        recyclerView.setAdapter(adapter);

        taskDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        if (result.getData().getBooleanExtra("isTaskUpdated", false)) {
                            loadTasksForDate(selectedDate);
                        }
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

    @Override
    public void onResume() {
        super.onResume();
        if (selectedDate != null) {
            loadTasksForDate(selectedDate);
        }
    }

    private void loadTasksForDate(Date dateToLoad) {
        if (fbRepo.getCurrentUser() == null) return;

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String selectedDayString = sdfDate.format(dateToLoad);

        if (selectedDayString.equals(todayDateString)) {
            tvTaskList.setText("Your Task for Today");
        } else {
            SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
            tvTaskList.setText("Task for " + sdfDisplay.format(dateToLoad));
        }

        fbRepo.loadTasksForUser(new FirebaseRepo.OnTasksLoadedListener() {
            @Override
            public void onTasksLoaded(List<Task> allTasks) {
                taskList.clear();

                for (Task t : allTasks) {
                    if (t.getTaskDate() != null) {
                        String taskDayString = sdfDate.format(t.getTaskDate());
                        if (taskDayString.equals(selectedDayString)) {
                            taskList.add(t);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateTaskField(Task task, String field, Object value) {
        if (task.getId() == null) return;

        if ("completed".equals(field)) task.setCompleted((Boolean) value);
        if ("priority".equals(field)) task.setPriority((String) value);
        adapter.notifyDataSetChanged();

        fbRepo.updateTaskField(task.getId(), field, value, (message, e) -> {
            if (e == null) {
                if ("completed".equals(field) && (Boolean) value) {
                    fbRepo.logTaskAction(task.getId(), task.getTitle(), "COMPLETED");
                }

                updateWidget();
                loadTasksForDate(selectedDate);
            } else {
                Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                loadTasksForDate(selectedDate);
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

        fbRepo.deleteTask(task.getId(), task.getTitle(), (message, e) -> {
            if (e == null) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                    updateWidget();
                }
                loadTasksForDate(selectedDate);
            } else {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error deleting task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateWidget() {
        if (getContext() != null) {
            Intent widgetUpdateIntent = new Intent(getContext(), TaskWidgetProvider.class);
            widgetUpdateIntent.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int[] ids = android.appwidget.AppWidgetManager.getInstance(getContext()).getAppWidgetIds(
                    new android.content.ComponentName(getContext(), TaskWidgetProvider.class));
            widgetUpdateIntent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            getContext().sendBroadcast(widgetUpdateIntent);
        }
    }
}
