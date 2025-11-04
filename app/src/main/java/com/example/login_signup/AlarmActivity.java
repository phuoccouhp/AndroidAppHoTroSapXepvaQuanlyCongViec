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
        setContentView(R.layout.activity_alarm);

        // Make the activity show over the lock screen and turn the screen on
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

        TextView tvTaskTitle = findViewById(R.id.tv_alarm_task_title);
        TextView tvTaskCategory = findViewById(R.id.tv_alarm_task_category);
        TextView tvTaskNote = findViewById(R.id.tv_alarm_task_note);
        Button btnStopAlarm = findViewById(R.id.btn_stop_alarm);

        String taskTitle = getIntent().getStringExtra("title");
        String taskCategory = getIntent().getStringExtra("category");
        String taskNote = getIntent().getStringExtra("note");

        tvTaskTitle.setText(taskTitle);
        tvTaskCategory.setText("Category: " + taskCategory);
        tvTaskNote.setText("Note: " + taskNote);

        // Start the service to play sound and vibrate
        Intent serviceIntent = new Intent(this, AlarmService.class);
        serviceIntent.putExtras(getIntent().getExtras()); // Pass all extras to the service
        startService(serviceIntent);

        btnStopAlarm.setOnClickListener(v -> {
            // Stop the service and finish the activity
            stopService(new Intent(this, AlarmService.class));
            finish();
        });
    }
}
