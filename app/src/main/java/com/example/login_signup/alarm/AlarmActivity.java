package com.example.login_signup.alarm;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.login_signup.home.HomeActivity;
import com.example.login_signup.R;

public class AlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Thiết lập để Activity có thể hiển thị đè lên màn hình khóa và tự động bật màn hình
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            );
        }

        setContentView(R.layout.activity_alarm);

        // Ánh xạ các view từ layout
        TextView tvTaskTitle = findViewById(R.id.tvDayOfWeek);
        TextView tvTaskCategory = findViewById(R.id.tvTaskCategory);
        TextView tvTaskNote = findViewById(R.id.tvTaskNote);
        Button btnStopAlarm = findViewById(R.id.btnStopAlarm);

        // Lấy dữ liệu công việc được truyền từ Intent
        String taskTitle = getIntent().getStringExtra("title");
        String taskCategory = getIntent().getStringExtra("category");
        String taskNote = getIntent().getStringExtra("note");

        // Hiển thị thông tin công việc lên giao diện
        tvTaskTitle.setText(taskTitle);
        tvTaskCategory.setText(taskCategory != null ? "Category: " + taskCategory : "");
        tvTaskNote.setText(taskNote != null ? "Note: " + taskNote : "");

        // Khởi chạy AlarmService để phát âm thanh báo thức hoặc rung
        Intent serviceIntent = new Intent(this, AlarmService.class);
        serviceIntent.putExtras(getIntent().getExtras());
        startService(serviceIntent);

        // Xử lý sự kiện khi người dùng nhấn nút "Dừng báo thức"
        btnStopAlarm.setOnClickListener(v -> {
            // Dừng service báo thức
            stopService(new Intent(this, AlarmService.class));

            // Chuyển hướng về màn hình chính (HomeActivity)
            Intent homeIntent = new Intent(this, HomeActivity.class);

            // Xóa các Activity trước đó trong stack để tránh quay lại màn hình báo thức khi nhấn Back
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);

            // Đóng Activity hiện tại
            finish();
        });
    }
}
