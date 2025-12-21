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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private List<ImageButton> navButtons;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        navButtons = new ArrayList<>();
        navButtons.add(findViewById(R.id.nav_home_button));
        navButtons.add(findViewById(R.id.nav_calendar_button));
        navButtons.add(findViewById(R.id.nav_documents_button));
        navButtons.add(findViewById(R.id.nav_settings_button));

        navButtons.get(0).setOnClickListener(v -> loadFragment(new HomeFragment(), v));
        navButtons.get(1).setOnClickListener(v -> loadFragment(new CalendarFragment(), v));
        navButtons.get(2).setOnClickListener(v -> loadFragment(new DocumentsFragment(), v));

        navButtons.get(3).setOnClickListener(v -> loadFragment(new MainProfileFragment(), v));


        if (savedInstanceState == null) {
            int targetTab = getIntent().getIntExtra("TARGET_TAB", 0);
            if (targetTab == 1) {
                loadFragment(new CalendarFragment(), navButtons.get(1));
            } else if (targetTab == 2) {
                loadFragment(new DocumentsFragment(), navButtons.get(2));
            } else {
                loadFragment(new HomeFragment(), navButtons.get(0));
            }
        }


        checkAndUpdateStreak();

        checkAndRequestAlarmPermission();
        checkBatteryOptimizations();
        checkDrawOverlayPermission();
    }


    private void checkAndUpdateStreak() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        DocumentReference userRef = db.collection("users").document(currentUser.getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String lastLoginDate = documentSnapshot.getString("lastLoginDate");
                Long currentStreakLong = documentSnapshot.getLong("streak");
                int currentStreak = (currentStreakLong != null) ? currentStreakLong.intValue() : 0;


                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String todayStr = sdf.format(new Date());

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -1);
                String yesterdayStr = sdf.format(cal.getTime());

                if (lastLoginDate == null) {

                    updateStreakInFirebase(userRef, 1, todayStr);
                } else if (lastLoginDate.equals(todayStr)) {

                    Log.d("Streak", "Already updated for today");
                } else if (lastLoginDate.equals(yesterdayStr)) {

                    updateStreakInFirebase(userRef, currentStreak + 1, todayStr);
                } else {
                   
                    updateStreakInFirebase(userRef, 1, todayStr);
                }
            }
        });
    }

    private void updateStreakInFirebase(DocumentReference userRef, int newStreak, String todayStr) {
        userRef.update("streak", newStreak, "lastLoginDate", todayStr)
                .addOnSuccessListener(aVoid -> Log.d("Streak", "Updated streak to: " + newStreak));
    }

    private void checkAndRequestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("This app needs permission to set precise alarms to function correctly.")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .create().show();
            }
        }
    }

    private void checkBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                new AlertDialog.Builder(this)
                        .setTitle("Allow Background Activity")
                        .setMessage("Please allow the app to run in the background for alarms to work.")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null).show();
            }
        }
    }

    private void checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("Please grant 'Display over other apps' permission for the alarm screen.")
                    .setPositiveButton("Go to Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null).show();
        }
    }

    private void loadFragment(Fragment fragment, View selectedButton) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
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