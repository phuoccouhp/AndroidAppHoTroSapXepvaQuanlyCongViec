package com.example.login_signup;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private List<ImageButton> navButtons;
    private FloatingActionButton btnFabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        navButtons = new ArrayList<>();
        navButtons.add(findViewById(R.id.btnHome));
        navButtons.add(findViewById(R.id.btnCalendar));
        navButtons.add(findViewById(R.id.btnDocuments));
        navButtons.add(findViewById(R.id.btnProfile));

        btnFabAdd = findViewById(R.id.btnFabAdd);
        btnFabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        navButtons.get(0).setOnClickListener(v -> loadFragment(new HomeFragment(), v));
        navButtons.get(1).setOnClickListener(v -> loadFragment(new CalendarFragment(), v));
        navButtons.get(2).setOnClickListener(v -> loadFragment(new DocumentsFragment(), v));
        navButtons.get(3).setOnClickListener(v -> loadFragment(new ProfileFragment(), v));

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), navButtons.get(0));
        }

        checkAndRequestAlarmPermission();
        checkBatteryOptimizations();
        checkDrawOverlayPermission();
    }

    private void checkAndRequestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("This app needs permission to set precise alarms to function correctly. Please grant this permission in the settings.")
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

    private void loadFragment(Fragment fragment, View selectedButton) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        updateNavButtons(selectedButton);
    }

    private void updateNavButtons(View selectedButton) {
        for (ImageButton button : navButtons) {
            boolean isSelected = (button == selectedButton);
            button.setSelected(isSelected);

            if (isSelected) {
                button.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start();
            } else {
                button.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            }
        }
    }
}
