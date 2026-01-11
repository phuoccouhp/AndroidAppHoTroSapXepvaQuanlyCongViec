package com.example.login_signup;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;
import android.widget.ProgressBar;
import android.view.View;

import com.example.login_signup.log_sign.Login;

// MainActivity: Màn hình chào khi vừa mở ứng dụng
public class MainActivity extends AppCompatActivity {
    private static final int SPLASH_TIME_OUT = 3000; // Thời gian hiển thị (3 giây)
    Animation bottomToTopAnim;
    TextView lbTodo, textWelcome, textQuote;
    ImageView imageBusiness;
    ProgressBar progressBar;
    private boolean isSplashScreenStarted = false;

    // Bộ khởi chạy để yêu cầu quyền thông báo trên Android 13+
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
            });

    // Ánh xạ các thành phần giao diện
    private void Init() {
        lbTodo = findViewById(R.id.lb_todo);
        textWelcome = findViewById(R.id.text_welcome);
        textQuote = findViewById(R.id.text_quote);
        imageBusiness = findViewById(R.id.image_business);
        progressBar = findViewById(R.id.progressBar);
    }

    // Lấy một câu trích dẫn ngẫu nhiên từ tài nguyên hệ thống và hiển thị
    private void setupRandom() {
        String[] quote = getResources().getStringArray(R.array.inspirational_quotes);
        Random random = new Random();
        int randomIndex = random.nextInt(quote.length);
        textQuote.setText(quote[randomIndex]);
    }

    // Bắt đầu Animation cho các thành phần UI
    private void startAnim() {
        bottomToTopAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top);
        lbTodo.startAnimation(bottomToTopAnim);
        imageBusiness.startAnimation(bottomToTopAnim);
        textWelcome.startAnimation(bottomToTopAnim);
        textQuote.startAnimation(bottomToTopAnim);
    }

    // Xử lý việc chuyển đổi từ màn hình chào sang màn hình Đăng nhập (Login)
    private void startSplashScreen() {
        if (isSplashScreenStarted) {
            return;
        }
        isSplashScreenStarted = true;
        progressBar.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        }, SPLASH_TIME_OUT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Xử lý khoảng cách lề cho thanh trạng thái và thanh điều hướng
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Init();
        setupRandom();
        startAnim();

        // Kiểm tra quyền và điều hướng
        handlePermissionsAndNavigation();
    }

    // Kiểm tra quyền thông báo (POST_NOTIFICATIONS) trên Android 13 trở lên
    private void handlePermissionsAndNavigation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            startSplashScreen();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Kiểm tra quyền báo thức mỗi khi quay lại ứng dụng
        checkExactAlarmPermission();
    }

    // Kiểm tra và yêu cầu quyền thiết lập báo thức chính xác trên Android 12 trở lên
    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName()));
                if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                    startActivity(intent);
                }
            }
        }
    }
}
