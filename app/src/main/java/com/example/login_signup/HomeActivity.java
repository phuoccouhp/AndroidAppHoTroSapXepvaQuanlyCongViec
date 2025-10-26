package com.example.login_signup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    private ImageButton homeButton, calendarButton, documentsButton, settingsButton;
    private FloatingActionButton fabAddTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Ánh xạ view từ layout
        homeButton = findViewById(R.id.nav_home_button);
        calendarButton = findViewById(R.id.nav_calendar_button);
        documentsButton = findViewById(R.id.nav_documents_button);
        settingsButton = findViewById(R.id.nav_settings_button);
        fabAddTask = findViewById(R.id.fab_add_task);

        // Mặc định hiển thị HomeFragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // ================== XỬ LÝ CÁC NÚT ==================
        homeButton.setOnClickListener(v -> loadFragment(new HomeFragment()));

        // Nút thứ 2: hiển thị CalendarFragment
        calendarButton.setOnClickListener(v -> loadFragment(new CalendarFragment()));

        // Nút thứ 3: DocumentsFragment
        documentsButton.setOnClickListener(v -> loadFragment(new DocumentsFragment()));

        // Nút thứ 4: ProfileFragment (settings)
        settingsButton.setOnClickListener(v -> loadFragment(new ProfileFragment()));

        // Nút FloatingActionButton thêm task
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });
    }

    // Hàm load fragment chung
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}
