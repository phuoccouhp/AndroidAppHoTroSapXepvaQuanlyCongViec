package com.example.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
// import androidx.activity.EdgeToEdge; // Bỏ dòng này đi
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
public class HomeActivity extends AppCompatActivity {

    // Đây là file HomeActivity.java của bạn
    private ImageButton homeButton, calendarButton, documentsButton, settingsButton;
    private FloatingActionButton fabAddTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Ánh xạ các nút từ file KHUNG (activity_home.xml)
        homeButton = findViewById(R.id.nav_home_button);
        calendarButton = findViewById(R.id.nav_calendar_button);
        documentsButton = findViewById(R.id.nav_documents_button);
        settingsButton = findViewById(R.id.nav_settings_button); // Nút thứ 4
        fabAddTask = findViewById(R.id.fab_add_task);

        // Load HomeFragment làm mặc định
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // --- CÁC NÚT BẤM ---

        homeButton.setOnClickListener(v -> loadFragment(new HomeFragment()));

        documentsButton.setOnClickListener(v -> loadFragment(new DocumentsFragment()));

        // (Nút calendar...)
        calendarButton.setOnClickListener(v -> {
            // loadFragment(new CalendarFragment());
        });

        // ============ THÊM ĐOẠN NÀY ============
        // Khi nhấn nút thứ 4 (Settings) -> Load ProfileFragment
        settingsButton.setOnClickListener(v -> loadFragment(new ProfileFragment()));
        // =======================================

        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });
    }

    // Hàm helper để thay đổi Fragment (bạn đã có)
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}