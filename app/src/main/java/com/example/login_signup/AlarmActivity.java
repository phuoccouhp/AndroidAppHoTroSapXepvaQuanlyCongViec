package com.example.login_signup;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        TextView tvAlarm = findViewById(R.id.tvAlarm);
        TextView tvCategory = findViewById(R.id.tvCategory);
        TextView tvTaskNote = findViewById(R.id.tvTaskNote);
        Button btnDismiss = findViewById(R.id.btnDismiss);

        String taskTitle = getIntent().getStringExtra("title");
        String taskCategory = getIntent().getStringExtra("category");
        String taskNote = getIntent().getStringExtra("note");

        tvAlarm.setText(taskTitle);
        tvCategory.setText(taskCategory != null ? "Category: " + taskCategory : "");
        tvTaskNote.setText(taskNote != null ? "Note: " + taskNote : "");

        Intent serviceIntent = new Intent(this, AlarmService.class);
        serviceIntent.putExtras(getIntent().getExtras());
        startService(serviceIntent);

        btnDismiss.setOnClickListener(v -> {
            stopService(new Intent(this, AlarmService.class));
            finishAndRemoveTask();
        });
    }
}
