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
    // XÓA hoặc comment dòng dưới:
    // private FloatingActionButton fabAddTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homeButton = findViewById(R.id.nav_home_button);
        calendarButton = findViewById(R.id.nav_calendar_button);
        documentsButton = findViewById(R.id.nav_documents_button);
        settingsButton = findViewById(R.id.nav_settings_button);

        // XÓA phần này:
        // fabAddTask = findViewById(R.id.fab_add_task);
        // fabAddTask.setOnClickListener(v -> {
        //     Intent intent = new Intent(HomeActivity.this, AddTaskActivity.class);
        //     startActivity(intent);
        // });

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        homeButton.setOnClickListener(v -> loadFragment(new HomeFragment()));
        calendarButton.setOnClickListener(v -> loadFragment(new CalendarFragment()));
        documentsButton.setOnClickListener(v -> loadFragment(new DocumentsFragment()));
        settingsButton.setOnClickListener(v -> loadFragment(new ProfileFragment()));
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
