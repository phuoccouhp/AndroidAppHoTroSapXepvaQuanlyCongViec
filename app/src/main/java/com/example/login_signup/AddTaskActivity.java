package com.example.login_signup;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTaskName, etNotes;
    private Spinner spinnerCategories, spinnerVibration;
    private Button btnSetDueDate, btnSetTime, btnSetReminder, btnSelectRingtone;
    private FloatingActionButton fabSaveTask;

    private Calendar dueDateTime = Calendar.getInstance();
    private boolean reminderOn = false;
    private Uri selectedRingtoneUri;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private final ActivityResultLauncher<Intent> ringtonePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (uri != null) {
                        selectedRingtoneUri = uri;
                        btnSelectRingtone.setText(RingtoneManager.getRingtone(this, uri).getTitle(this));
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        NotificationHelper.createNotificationChannels(this);

        etTaskName = findViewById(R.id.et_task_name);
        etNotes = findViewById(R.id.et_notes);
        spinnerCategories = findViewById(R.id.spinner_categories);
        spinnerVibration = findViewById(R.id.spinner_vibration);
        btnSetDueDate = findViewById(R.id.btn_set_due_date);
        btnSetTime = findViewById(R.id.btn_set_time);
        btnSetReminder = findViewById(R.id.btn_set_reminder);
        btnSelectRingtone = findViewById(R.id.btn_select_ringtone);
        fabSaveTask = findViewById(R.id.fab_save_task);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        String[] categories = {"Work", "Personal", "Health", "Shopping"};
        ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategories.setAdapter(categoriesAdapter);

        String[] vibrations = {"Default", "Short", "Long", "Heartbeat"};
        ArrayAdapter<String> vibrationsAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, vibrations);
        spinnerVibration.setAdapter(vibrationsAdapter);

        btnSetDueDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                dueDateTime.set(Calendar.YEAR, year);
                dueDateTime.set(Calendar.MONTH, month);
                dueDateTime.set(Calendar.DAY_OF_MONTH, day);
                btnSetDueDate.setText(day + "/" + (month + 1) + "/" + year);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSetTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, hour, minute) -> {
                dueDateTime.set(Calendar.HOUR_OF_DAY, hour);
                dueDateTime.set(Calendar.MINUTE, minute);
                btnSetTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        btnSetReminder.setOnClickListener(v -> {
            reminderOn = !reminderOn;
            btnSetReminder.setText(reminderOn ? "Reminder ON" : "Reminder OFF");
        });

        btnSelectRingtone.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedRingtoneUri);
            ringtonePickerLauncher.launch(intent);
        });

        fabSaveTask.setOnClickListener(v -> saveTask());
    }

    private void saveTask() {
        String name = etTaskName.getText().toString().trim();
        String category = spinnerCategories.getSelectedItem().toString();
        String vibration = spinnerVibration.getSelectedItem().toString();
        String notes = etNotes.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter task name", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous";
        Map<String, Object> taskData = new HashMap<>();

        taskData.put("uid", userId);
        taskData.put("title", name);
        taskData.put("category", category);
        taskData.put("notes", notes);
        taskData.put("reminder", reminderOn);
        taskData.put("completed", false);
        taskData.put("taskDate", dueDateTime.getTime());
        taskData.put("vibration", vibration);
        if (selectedRingtoneUri != null) {
            taskData.put("ringtone", selectedRingtoneUri.toString());
        }

        db.collection("tasks")
                .add(taskData)
                .addOnSuccessListener(doc -> {
                    if (reminderOn) {
                        scheduleAlarms(doc.getId(), name, notes, category, dueDateTime.getTimeInMillis());
                    }
                    Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void scheduleAlarms(String taskId, String title, String note, String category, long dueTime) {
        // Schedule the main due-time alarm
        scheduleNotification(taskId, title, note, category, dueTime, false);

        // Schedule the advance notification
        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;
        long advanceTime = dueTime - twentyFourHoursInMillis;
        if (dueTime > System.currentTimeMillis() + twentyFourHoursInMillis) {
            // Due more than 24 hours from now: schedule for 24h before.
            scheduleNotification(taskId + "_advance", title, note, category, advanceTime, true);
        } else if (dueTime > System.currentTimeMillis()) {
            // Due in less than 24 hours (but in the future): show notification immediately.
            String taskInfo = "Title: " + title + "\nCategory: " + category + "\nNote: " + note;
            NotificationHelper.showAdvanceNotification(this, title, taskInfo, (taskId + "_advance").hashCode());
        }
    }

    private void scheduleNotification(String taskId, String title, String note, String category, long time, boolean isAdvance) {
        Intent intent = new Intent(this, TaskReminderReceiver.class);
        intent.putExtra("taskId", taskId);
        intent.putExtra("title", title);
        intent.putExtra("note", note);
        intent.putExtra("category", category);
        intent.putExtra("isAdvance", isAdvance);
        if(selectedRingtoneUri != null) {
            intent.putExtra("ringtone", selectedRingtoneUri.toString());
        }
        intent.putExtra("vibration", spinnerVibration.getSelectedItem().toString());

        int requestCode = taskId.hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
    }
}
