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
import com.google.firebase.firestore.DocumentReference;

import java.text.SimpleDateFormat;
import java.util.*;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTaskName, etNotes;
    private Spinner spinnerCategories, spinnerVibration;
    private Button btnSetDueDate, btnSetTime, btnSetReminder, btnSelectRingtone;
    private FloatingActionButton fabSaveTask;
    private ImageButton btnBack;

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
                        try {
                            btnSelectRingtone.setText(RingtoneManager.getRingtone(this, uri).getTitle(this));
                        } catch (Exception e) {
                            btnSelectRingtone.setText("Select Sound");
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        setupSpinners();
        setupListeners();
    }

    private void initViews() {
        etTaskName = findViewById(R.id.et_task_name);
        etNotes = findViewById(R.id.et_notes);
        spinnerCategories = findViewById(R.id.spinner_categories);
        spinnerVibration = findViewById(R.id.spinner_vibration);
        btnSetDueDate = findViewById(R.id.btn_set_due_date);
        btnSetTime = findViewById(R.id.btn_set_time);
        btnSetReminder = findViewById(R.id.btn_set_reminder);
        btnSelectRingtone = findViewById(R.id.btn_select_ringtone);
        fabSaveTask = findViewById(R.id.fab_save_task);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupSpinners() {
        String[] categories = {"Work", "Personal", "Health", "Shopping"};
        ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategories.setAdapter(categoriesAdapter);

        String[] vibrations = {"Default", "Short", "Long", "Heartbeat"};
        ArrayAdapter<String> vibrationsAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, vibrations);
        spinnerVibration.setAdapter(vibrationsAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSetDueDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                dueDateTime.set(Calendar.YEAR, year);
                dueDateTime.set(Calendar.MONTH, month);
                dueDateTime.set(Calendar.DAY_OF_MONTH, day);
                updateDateAndTimeButtons();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSetTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, hour, minute) -> {
                dueDateTime.set(Calendar.HOUR_OF_DAY, hour);
                dueDateTime.set(Calendar.MINUTE, minute);
                updateDateAndTimeButtons();
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        btnSetReminder.setOnClickListener(v -> {
            reminderOn = !reminderOn;
            updateReminderButton();
        });

        btnSelectRingtone.setOnClickListener(v -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedRingtoneUri);
            ringtonePickerLauncher.launch(intent);
        });

        fabSaveTask.setOnClickListener(v -> saveNewTask());
    }

    private void updateDateAndTimeButtons() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        btnSetDueDate.setText(dateFormat.format(dueDateTime.getTime()));

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        btnSetTime.setText(timeFormat.format(dueDateTime.getTime()));
    }

    private void updateReminderButton() {
        btnSetReminder.setText(reminderOn ? "Reminder ON" : "Reminder OFF");
    }

    private void saveNewTask() {
        String name = etTaskName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter task name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dueDateTime.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(this, "Please select a due date in the future", Toast.LENGTH_SHORT).show();
            return;
        }

        String noteContent = (etNotes != null && etNotes.getText() != null) ? etNotes.getText().toString().trim() : "";
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous";

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("uid", userId);
        taskData.put("title", name);
        taskData.put("category", spinnerCategories.getSelectedItem().toString());
        taskData.put("note", noteContent);
        taskData.put("reminder", reminderOn);
        taskData.put("completed", false);
        taskData.put("taskDate", dueDateTime.getTime());
        taskData.put("vibration", spinnerVibration.getSelectedItem().toString());
        if (selectedRingtoneUri != null) {
            taskData.put("ringtone", selectedRingtoneUri.toString());
        }

        db.collection("tasks").add(taskData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
                    if (reminderOn) {

                        Task taskToSchedule = new Task();
                        taskToSchedule.setId(documentReference.getId());
                        taskToSchedule.setTitle(name);
                        taskToSchedule.setNote(noteContent);
                        taskToSchedule.setCategory(spinnerCategories.getSelectedItem().toString());
                        taskToSchedule.setTaskDate(dueDateTime.getTime());
                        taskToSchedule.setVibration(spinnerVibration.getSelectedItem().toString());
                        if(selectedRingtoneUri != null) taskToSchedule.setRingtone(selectedRingtoneUri.toString());

                        scheduleAlarmsForTask(this, taskToSchedule);
                    }
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error saving task", Toast.LENGTH_SHORT).show());
    }

    private void scheduleAlarmsForTask(Context context, Task task) {
        long dueTime = task.getTaskDate().getTime();
        long currentTime = System.currentTimeMillis();
        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;
        if (dueTime > currentTime) {
            scheduleNotification(context, task, dueTime, false); // isAdvance = false
        }

        long timeDifference = dueTime - currentTime;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm 'ngày' dd/MM", Locale.getDefault());
        String taskInfo = task.getTitle() + "\n" +
                "Vào lúc: " + sdf.format(task.getTaskDate());

        int advanceNotificationId = task.getId().hashCode() + 1;

        if (timeDifference > 0 && timeDifference < twentyFourHoursInMillis) {

            NotificationHelper.showAdvanceNotification(
                    context,
                    "Công việc sắp tới!",
                    taskInfo,
                    advanceNotificationId
            );

        } else if (timeDifference >= twentyFourHoursInMillis) {

            long advanceTime = dueTime - twentyFourHoursInMillis;
            scheduleNotification(context, task, advanceTime, true); // isAdvance = true
        }
    }


    private void scheduleNotification(Context context, Task task, long time, boolean isAdvance) {
        String taskId = isAdvance ? task.getId() + "_advance" : task.getId();

        Intent intent = new Intent(context, TaskReminderReceiver.class);
        intent.putExtra("taskId", taskId);
        intent.putExtra("title", task.getTitle());
        intent.putExtra("note", task.getNote());
        intent.putExtra("category", task.getCategory());
        intent.putExtra("isAdvance", isAdvance);
        if (task.getRingtone() != null) {
            intent.putExtra("ringtone", task.getRingtone());
        }
        if (task.getVibration() != null) {
            intent.putExtra("vibration", task.getVibration());
        }

        int requestCode = taskId.hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            } catch (SecurityException se) {
                Toast.makeText(context, "Permission to schedule alarms not granted.", Toast.LENGTH_LONG).show();
            }
        }
    }
}