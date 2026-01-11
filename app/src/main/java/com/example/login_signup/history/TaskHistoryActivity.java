package com.example.login_signup.history;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_signup.R;
import com.example.login_signup.classes.FirebaseRepo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Activity hình hiển thị lịch sử các hành động (Logs) đã tương tác với Tasks
public class TaskHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerViewLog;
    private FirebaseRepo fbRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_history);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Task History");
        }

        // Cấu hình RecyclerView để hiển thị danh sách logs
        recyclerViewLog = findViewById(R.id.recyclerViewLog);
        recyclerViewLog.setLayoutManager(new LinearLayoutManager(this));

        fbRepo = new FirebaseRepo();
        loadHistory();
    }

    // Tải dữ liệu nhật ký logs từ Firebase và thực hiện phân nhóm theo ngày
    private void loadHistory() {
        fbRepo.getTaskLogs(new FirebaseRepo.OnLogLoadedListener() {
            @Override
            public void onLogsLoaded(List<TaskLog> logs) {
                // Phân nhóm dữ liệu để chèn thêm các tiêu đề ngày tháng (Headers)
                List<Object> groupedList = groupLogsByDate(logs);
                TaskHistoryAdapter adapter = new TaskHistoryAdapter(groupedList);
                recyclerViewLog.setAdapter(adapter);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(TaskHistoryActivity.this, "Error loading history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Logic phân nhóm nhật ký logs theo ngày
    private List<Object> groupLogsByDate(List<TaskLog> logs) {
        // Sắp xếp danh sách logs theo thời gian tăng dần
        List<Object> grouped = new ArrayList<>();

        // Định dạng ngày
        SimpleDateFormat headerFormat = new SimpleDateFormat("dd MMM • EEEE", Locale.getDefault());

        String lastHeader = "";
        String today = headerFormat.format(new java.util.Date());

        for (TaskLog log : logs) {
            if (log.getTimestamp() == null) continue;

            // Lấy ngày hiện tại
            String currentHeader = headerFormat.format(log.getTimestamp());

            // Nếu ngày là hôm nay, hiển thị chữ "Today"
            if (currentHeader.equals(today)) {
                currentHeader = currentHeader.split("•")[0] + " • Today";
            }

            // Nếu ngày hiện tại khác ngày của log trước đó, thêm một Header mới vào danh sách
            if (!currentHeader.equals(lastHeader)) {
                grouped.add(currentHeader);
                lastHeader = currentHeader; // Cập nhật ngày của log trước đó
            }
            // Thêm log công việc vào danh sách sau tiêu đề ngày
            grouped.add(log);
        }
        return grouped;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Xử lý khi nhấn nút quay lại trên Toolbar
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
