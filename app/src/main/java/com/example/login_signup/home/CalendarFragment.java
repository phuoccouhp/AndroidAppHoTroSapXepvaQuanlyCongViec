package com.example.login_signup.home;


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

import com.example.login_signup.R;
import com.example.login_signup.classes.FirebaseRepo;
import com.example.login_signup.classes.Task;
import com.example.login_signup.task.TaskAdapter;
import com.example.login_signup.task.TaskDetailActivity;
import com.example.login_signup.task.TaskWidgetProvider;

import java.text.SimpleDateFormat;
import java.util.*;

// Calendar Fragment: Hiển thị lịch và các công việc theo ngày được chọn trên lịch
public class CalendarFragment extends Fragment {
    private FirebaseRepo fbRepo;

    // Các đối tượng thành phần giao diện
    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private TextView tvTaskList;

    private TaskAdapter adapter; // Adapter hiển thị danh sách công việc
    private List<Task> taskList = new ArrayList<>(); // Danh sách công việc
    private Date selectedDate = new Date(); // Lấy ngày hôm nay
    private String todayDateString; // Chuẩn hóa thành chuỗi ngày hôm nay
    private ActivityResultLauncher<Intent> taskDetailLauncher; // Bộ khởi chạy để nhận kết quả trả về từ màn hình chi tiết công việc

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        // Lấy chuỗi định dạng ngày hôm nay
        todayDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Khởi tạo đối tượng FirebaseRepo
        fbRepo = new FirebaseRepo();

        // Ánh xạ các thành phần giao diện
        calendarView = v.findViewById(R.id.calendarView);
        recyclerView = v.findViewById(R.id.rvTasks);
        tvTaskList = v.findViewById(R.id.tvTaskList);

        // Cấu hình RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Cấu hình Adapter và các hành động (Click, Delete, Status, Priority)
        adapter = new TaskAdapter(taskList, new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onItemClick(Task task) {
                // Mở chi tiết công việc
                Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
                intent.putExtra("taskId", task.getId());
                taskDetailLauncher.launch(intent);
            }

            @Override
            public void onDeleteClick(Task task) {
                // Xóa công việc khỏi Firestore
                deleteTaskFromFirestore(task);
            }

            @Override
            public void onStatusClick(Task task) {
                // Đảo ngược trạng thái hoàn thành
                boolean newStatus = !task.isCompleted();
                updateTaskField(task, "completed", newStatus);
            }

            @Override
            public void onPriorityClick(Task task) {
                // Đảo ngược mức độ ưu tiên giữa High và Basic
                String current = task.getPriority();
                String newPriority = "High".equals(current) ? "Basic" : "High";
                updateTaskField(task, "priority", newPriority);
            }
        });
        recyclerView.setAdapter(adapter);

        // Khởi tạo bộ khởi chạy để nhận kết quả trả về từ màn hình chi tiết công việc
        taskDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Nếu có cập nhật ở màn hình chi tiết công việc, tải lại danh sách
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        if (result.getData().getBooleanExtra("isTaskUpdated", false)) {
                            loadTasksForDate(selectedDate);
                        }
                    }
                }
        );

        // Sự kiện khi người dùng chọn một ngày khác trên Lịch
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Lấy ngày được chọn trên lịch
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth, 0, 0, 0);

            selectedDate = c.getTime(); // Cập nhật ngày được chọn
            loadTasksForDate(selectedDate); // Tải danh sách công việc cho ngày được chọn
        });

        loadTasksForDate(selectedDate);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật lại danh sách mỗi khi quay lại Fragment
        if (selectedDate != null) {
            loadTasksForDate(selectedDate);
        }
    }

    // Tải danh sách công việc từ Firestore và lọc theo ngày đã chọn
    private void loadTasksForDate(Date dateToLoad) {
        if (fbRepo.getCurrentUser() == null) return;

        // Định dạng ngày để so sánh với ngày trong Firestore
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String selectedDayString = sdfDate.format(dateToLoad);

        // Cập nhật tiêu đề hiển thị ngày tháng
        if (selectedDayString.equals(todayDateString)) { // Nếu là hôm nay
            tvTaskList.setText("Your Task for Today");
        } else { // Nếu là ngày khác
            SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
            tvTaskList.setText("Task for " + sdfDisplay.format(dateToLoad));
        }

        // Gọi phương thức tải danh sách công việc của người dùng ở Firestore
        fbRepo.loadTasksForUser(new FirebaseRepo.OnTasksLoadedListener() {
            @Override
            public void onTasksLoaded(List<Task> allTasks) {
                taskList.clear();
                // Chỉ lấy những công việc trùng với ngày đang chọn
                for (Task t : allTasks) {
                    if (t.getTaskDate() != null) {
                        // Định dạng ngày để so sánh
                        String taskDayString = sdfDate.format(t.getTaskDate());

                        // Nếu trùng ngày được chọn
                        if (taskDayString.equals(selectedDayString)) {
                            taskList.add(t); // Thêm vào danh sách công việc của ngày đó
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

    // Cập nhật dữ liệu của công việc (completed hoặc priority)
    private void updateTaskField(Task task, String field, Object value) {
        if (task.getId() == null) return;

        // Cập nhật dữ liệu tạm thời trên UI để phản hồi nhanh
        if ("completed".equals(field)) task.setCompleted((Boolean) value);
        if ("priority".equals(field)) task.setPriority((String) value);
        adapter.notifyDataSetChanged();

        // Gửi yêu cầu cập nhật lên Firestore
        fbRepo.updateTaskField(task.getId(), field, value, (message, e) -> {
            if (e == null) {
                // Ghi log công việc nếu task đó hoàn thành
                if ("completed".equals(field) && (Boolean) value) {
                    fbRepo.logTaskAction(task.getId(), task.getTitle(), "COMPLETED");
                }
                updateWidget(); // Cập nhật Widget ngoài màn hình chủ
                loadTasksForDate(selectedDate); // Tải lại danh sách công việc
            } else {
                Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                loadTasksForDate(selectedDate);
            }
        });
    }

    // Xóa công việc khỏi Firestore
    private void deleteTaskFromFirestore(Task task) {
        if (task.getId() == null || task.getId().isEmpty()) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: Task ID is missing", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Gọi phương thức xóa công việc từ lớp FirebaseRepo
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

    // Thông báo cho hệ thống cập nhật lại Widget của ứng dụng
    private void updateWidget() {
        if (getContext() != null) {
            // Tạo đối tượng intent để cập nhật Widget
            Intent widgetUpdateIntent = new Intent(getContext(), TaskWidgetProvider.class);

            // Đặt hành động là cập nhật Widget
            widgetUpdateIntent.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);

            // Lấy danh sách ID của Widget
            int[] ids = android.appwidget.AppWidgetManager.getInstance(getContext()).getAppWidgetIds(
                    new android.content.ComponentName(getContext(), TaskWidgetProvider.class));

            // Gửi thông báo đến Widget để cập nhật dữ liệu
            widgetUpdateIntent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            getContext().sendBroadcast(widgetUpdateIntent);
        }
    }
}
