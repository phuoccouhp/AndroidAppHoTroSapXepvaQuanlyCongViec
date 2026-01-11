package com.example.login_signup.task;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
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

import com.example.login_signup.R;
import com.example.login_signup.classes.FirebaseRepo;
import com.example.login_signup.classes.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// TaskFragment: hiển thị danh sách công việc theo phân loại
public class TaskFragment extends Fragment {
    private FirebaseRepo fbRepo;

    // Các thành phần giao diện
    private RecyclerView rvHighPriority, rvIncomplete, rvCompleted;
    private ProgressBar progressBarTask;
    private TextView tvProgressPercent, tvProgressCount, tvHighPriorityCount, tvCompletedCount, tvPendingCount;

    private ImageButton btnFilterHigh, btnFilterIncomplete, btnFilterCompleted;

    // Các bộ điều phối danh sách (Adapters)
    private TaskAdapter adapterHighPriority, adapterIncomplete, adapterCompleted;

    // Danh sách dữ liệu
    private List<Task> allTasks = new ArrayList<>();
    private List<Task> highPriorityList = new ArrayList<>();
    private List<Task> incompleteList = new ArrayList<>();
    private List<Task> completedList = new ArrayList<>();

    // Bộ khởi chạy để mở màn hình chi tiết công việc
    private ActivityResultLauncher<Intent> taskDetailLauncher;

    // Biến lưu trữ trạng thái bộ lọc cho từng nhóm
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

        // Lấy ngày hôm nay định dạng yyyy-MM-dd để so sánh
        todayDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Ánh xạ các View
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

        // Thiết lập bố cục danh sách (LinearLayout)
        rvHighPriority.setLayoutManager(new LinearLayoutManager(getContext()));
        rvIncomplete.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCompleted.setLayoutManager(new LinearLayoutManager(getContext()));

        fbRepo = new FirebaseRepo();

        // Xử lý các hành động trên từng mục công việc (Task)
        TaskAdapter.OnTaskActionListener actionListener = new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onItemClick(Task task) {
                // Mở màn hình chi tiết công việc
                Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
                intent.putExtra("taskId", task.getId());
                taskDetailLauncher.launch(intent);
            }

            @Override
            public void onDeleteClick(Task task) {
                // Xóa công việc
                deleteTaskFromFirestore(task);
            }

            @Override
            public void onStatusClick(Task task) {
                // Đổi trạng thái Hoàn thành / Chưa xong
                boolean newStatus = !task.isCompleted();
                updateTaskField(task, "completed", newStatus);
            }

            @Override
            public void onPriorityClick(Task task) {
                // Đổi mức độ ưu tiên (High và Basic)
                String current = task.getPriority();
                String newPriority = "High".equals(current) ? "Basic" : "High";
                updateTaskField(task, "priority", newPriority);
            }
        };

        // Khởi tạo các adapter cho 3 danh sách khác nhau
        adapterHighPriority = new TaskAdapter(highPriorityList, actionListener);
        adapterIncomplete = new TaskAdapter(incompleteList, actionListener);
        adapterCompleted = new TaskAdapter(completedList, actionListener);

        rvHighPriority.setAdapter(adapterHighPriority);
        rvIncomplete.setAdapter(adapterIncomplete);
        rvCompleted.setAdapter(adapterCompleted);

        // Lắng nghe kết quả trả về từ màn hình chi tiết công việc
        taskDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        if (result.getData().getBooleanExtra("isTaskUpdated", false)) {
                            loadAllTasks(); // Cập nhật lại danh sách công việc
                        }
                    }
                }
        );

        // Thiết lập sự kiện cho các nút lọc danh mục
        btnFilterHigh.setOnClickListener(view -> showFilterMenu(view, 1));
        btnFilterIncomplete.setOnClickListener(view -> showFilterMenu(view, 2));
        btnFilterCompleted.setOnClickListener(view -> showFilterMenu(view, 3));

        // Tải toàn bộ công việc từ Firebase
        loadAllTasks();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật lại dữ liệu mỗi khi quay lại màn hình
        loadAllTasks();
    }

    // Hiển thị menu Popup để chọn danh mục lọc công việc
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

            // Lưu danh mục lọc dựa trên nhóm danh sách (1: High, 2: Incomplete, 3: Completed)
            if (type == 1) filterCategoryHigh = selected;
            else if (type == 2) filterCategoryIncomplete = selected;
            else if (type == 3) filterCategoryCompleted = selected;

            filterTasks(); // Chạy lại logic lọc dữ liệu
            return true;
        });
        popup.show();
    }

    // Tải toàn bộ công việc của người dùng hiện tại từ Firebase
    private void loadAllTasks() {
        if (fbRepo.getCurrentUser() == null) return;

        fbRepo.loadTasksForUser(new FirebaseRepo.OnTasksLoadedListener() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                allTasks.clear();

                SimpleDateFormat sdfDisplayDate = new SimpleDateFormat("dd/MM", Locale.getDefault());

                for (Task t : tasks) {
                    // Định dạng lại ngày hiển thị cho từng công việc
                    if (t.getTaskDate() != null) {
                        t.setDate(sdfDisplayDate.format(t.getTaskDate()));
                    }
                    allTasks.add(t);
                }
                filterTasks(); // Sau khi tải xong, tiến hành phân loại vào các nhóm
            }

            @Override
            public void onError(Exception e) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading tasks", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Phân loại danh sách tổng `allTasks` vào 3 nhóm dựa trên trạng thái, mức độ ưu tiên và bộ lọc
    private void filterTasks() {
        highPriorityList.clear();
        incompleteList.clear();
        completedList.clear();

        for (Task t : allTasks) {
            if (t.isCompleted()) {
                // Nhóm Đã hoàn thành
                if (filterCategoryCompleted == null || t.getCategory().equals(filterCategoryCompleted)) {
                    completedList.add(t);
                }
            } else {
                // Nhóm Chưa hoàn thành
                String priority = t.getPriority();
                if ("High".equals(priority)) {
                    // Nhóm Ưu tiên cao
                    if (filterCategoryHigh == null || t.getCategory().equals(filterCategoryHigh)) {
                        highPriorityList.add(t);
                    }
                } else {
                    // Nhóm Bình thường
                    if (filterCategoryIncomplete == null || t.getCategory().equals(filterCategoryIncomplete)) {
                        incompleteList.add(t);
                    }
                }
            }
        }

        // Cập nhật giao diện danh sách
        adapterHighPriority.notifyDataSetChanged();
        adapterIncomplete.notifyDataSetChanged();
        adapterCompleted.notifyDataSetChanged();
        updateProgressBar(); // Tính toán lại tiến độ hoàn thành trong ngày
    }

    // Tính toán và hiển thị tiến độ hoàn thành các công việc có hạn trong ngày hôm nay
    private void updateProgressBar() {
        List<Task> todayTasks = new ArrayList<>();
        SimpleDateFormat sdfCompare = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdfCompare.format(new Date());

        // Lọc các công việc có hạn là ngày hôm nay
        for(Task t : allTasks) {
            if (t.getTaskDate() != null) {
                String tDate = sdfCompare.format(t.getTaskDate());
                if(tDate.equals(today)) todayTasks.add(t);
            }
        }

        int todayTotal = todayTasks.size();
        int todayCompleted = 0;
        for (Task t : todayTasks) {
            if (t.isCompleted()) todayCompleted++;
        }

        // Hiển thị số lượng Xong / Tổng số công việc trong ngày
        tvProgressCount.setText(todayCompleted + "/" + todayTotal);
        int progress = 0;
        if (todayTotal > 0) {
            progress = (int) ((todayCompleted / (float) todayTotal) * 100);
        }

        // Cập nhật thanh ProgressBar tiến độ hoàn thành công việc hôm nay
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            progressBarTask.setProgress(progress, true);
        } else {
            progressBarTask.setProgress(progress);
        }
        tvProgressPercent.setText(progress + "%");

        // Hiển thị số lượng công việc theo phân loại
        tvHighPriorityCount.setText(String.valueOf(highPriorityList.size()));
        tvCompletedCount.setText(String.valueOf(completedList.size()));
        tvPendingCount.setText(String.valueOf(incompleteList.size()));
    }

    // Cập nhật trạng thái công việc lên Firestore
    private void updateTaskField(Task task, String field, Object value) {
        if (task.getId() == null) return;

        // Cập nhật tạm thời trên UI
        if ("completed".equals(field)) task.setCompleted((Boolean) value);
        if ("priority".equals(field)) task.setPriority((String) value);

        filterTasks();

        // Gọi phương thức cập nhật trạng thái công việc trên Firestore
        fbRepo.updateTaskField(task.getId(), field, value, (message, e) -> {
            if (e == null) {
                // Nếu hoàn thành công việc, ghi log và cập nhật Streak
                if ("completed".equals(field) && (Boolean) value) {
                    fbRepo.logTaskAction(task.getId(), task.getTitle(), "COMPLETED");
                    fbRepo.updateStreak();
                }

                updateWidget(); // Đồng bộ dữ liệu với Widget
                loadAllTasks(); // Tải lại toàn bộ để đảm bảo đồng nhất dữ liệu
            } else {
                Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                loadAllTasks();
            }
        });
    }

    // Xóa công việc khỏi Firestore
    private void deleteTaskFromFirestore(Task task) {
        if (task.getId() == null) return;
        // Gọi phương thức xóa công việc trên Firestore
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

    // Gửi tín hiệu yêu cầu cập nhật giao diện cho Widget
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
