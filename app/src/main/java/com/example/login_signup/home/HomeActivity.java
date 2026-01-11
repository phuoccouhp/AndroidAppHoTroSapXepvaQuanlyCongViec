package com.example.login_signup.home;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.login_signup.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.login_signup.task.AddTaskActivity;

import java.util.ArrayList;
import java.util.List;

// HomeActivity: Màn hình chính của ứng dụng, quản lý việc chuyển đổi giữa các Fragment
public class HomeActivity extends AppCompatActivity {

    private List<ImageButton> navButtons; // Danh sách các nút điều hướng chuyển Fragment
    private FloatingActionButton btnFabAdd; // Button nổi để thêm 1 công việc mới

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Xử lý Window Insets để giao diện không bị đè bởi thanh hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo danh sách các nút điều hướng
        navButtons = new ArrayList<>();
        navButtons.add(findViewById(R.id.btnHome));
        navButtons.add(findViewById(R.id.btnCalendar));
        navButtons.add(findViewById(R.id.btnDocuments));
        navButtons.add(findViewById(R.id.btnProfile));

        // Thiết lập sự kiện click cho nút thêm công việc
        btnFabAdd = findViewById(R.id.btnFabAdd);
        btnFabAdd.setOnClickListener(v -> {
            // Chuyển hướng đến màn hình AddTaskActivity để thêm công việc
            Intent intent = new Intent(HomeActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        // Gán sự kiện chuyển đổi Fragment cho từng nút điều hướng
        navButtons.get(0).setOnClickListener(v -> loadFragment(new HomeFragment(), v));
        navButtons.get(1).setOnClickListener(v -> loadFragment(new CalendarFragment(), v));
        navButtons.get(2).setOnClickListener(v -> loadFragment(new ChatHistoryFragment(), v));
        navButtons.get(3).setOnClickListener(v -> loadFragment(new ProfileFragment(), v));

        // Hiển thị Fragment mặc định (Home) khi lần đầu mở Activity
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), navButtons.get(0));
        }

        // Kiểm tra các quyền đặc biệt cần thiết cho tính năng báo thức/nhắc nhở
        checkAndRequestAlarmPermission();
        checkBatteryOptimizations();
        checkDrawOverlayPermission();
    }

    // Kiểm tra quyền đặt báo thức để AlarmManager hoạt động
    private void checkAndRequestAlarmPermission() {
        // Kiểm tra xem có phải là phiên bản Android 12+ không
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Lấy đối tượng AlarmManager để kiểm tra quyền đặt báo thức
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            // Kiểm tra xem có quyền đặt báo thức chính xác không
            if (!alarmManager.canScheduleExactAlarms()) {
                // Nếu không thì hiển thị hộp thoại yêu cầu người dùng cấp quyền
                new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("This app needs permission to set precise alarms to function correctly. Please grant this permission in the settings.")

                        // Button để mở cài đặt để cấp quyền
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            }
        }
    }

    // Yêu cầu người dùng bỏ tối ưu hóa pin để ứng dụng không bị hệ thống "giết" khi chạy ngầm
    private void checkBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                new AlertDialog.Builder(this)
                        .setTitle("Important: Allow Background Activity")
                        .setMessage("For alarms to work correctly even when the app is closed, please allow the app to run in the background without restrictions. 'Unrestricted' is the recommended setting.")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    // Kiểm tra và yêu cầu quyền hiển thị trên các ứng dụng khác
    private void checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("Crucial Permission Required")
                    .setMessage("To ensure the alarm screen appears instantly over other apps, please grant the 'Display over other apps' permission.")
                    .setPositiveButton("Go to Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    // Load Fragment được chọn và cập nhật trạng thái nút điều hướng
    private void loadFragment(Fragment fragment, View selectedButton) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        updateNavButtons(selectedButton);
    }

    // Cập nhật hiệu ứng (phóng to/thu nhỏ) và trạng thái chọn cho các nút điều hướng
    private void updateNavButtons(View selectedButton) {
        for (ImageButton button : navButtons) {
            boolean isSelected = (button == selectedButton);
            button.setSelected(isSelected);

            // Hiệu ứng phóng to nút được chọn để làm nổi bật
            if (isSelected) {
                button.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start();
            } else {
                button.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            }
        }
    }
}
