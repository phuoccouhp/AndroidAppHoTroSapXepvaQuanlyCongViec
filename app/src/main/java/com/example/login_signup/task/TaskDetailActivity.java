package com.example.login_signup.task;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.login_signup.R;
import com.example.login_signup.classes.FirebaseRepo;
import com.example.login_signup.classes.Task;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TaskDetailActivity extends AppCompatActivity {

    private EditText etTaskName, etNotes;
    private Spinner spinnerCategories, spinnerVibration;
    private Button btnSetDueDate, btnSetTime, btnSetReminder, btnSelectRingtone;
    private ImageButton btnBack;
    private FloatingActionButton btnSaveTask;

    private FirebaseRepo fbRepo;
    private String taskId;
    private Calendar dueDateTime = Calendar.getInstance();
    private boolean reminderOn = false;
    private Uri selectedRingtoneUri;

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
        setContentView(R.layout.activity_task_detail);

        fbRepo = new FirebaseRepo();
        taskId = getIntent().getStringExtra("taskId");

        initViews();
        setupListeners();
        setupCategorySpinner();
        setupVibrationSpinner();

        if (taskId != null) {
            loadTaskDetails();
        } else {
            Toast.makeText(this, "Task ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        etTaskName = findViewById(R.id.etTaskName);
        spinnerCategories = findViewById(R.id.spinnerCategories);
        spinnerVibration = findViewById(R.id.spinnerVibration);
        btnSetDueDate = findViewById(R.id.btnSetDueDate);
        btnSetTime = findViewById(R.id.btnSetTime);
        etNotes = findViewById(R.id.etNotes);
        btnBack = findViewById(R.id.btnBack);
        btnSaveTask = findViewById(R.id.btnSaveTask);
        btnSetReminder = findViewById(R.id.btnSetReminder);
        btnSelectRingtone = findViewById(R.id.btnSelectRingtone);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSaveTask.setOnClickListener(v -> updateTask());

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
                dueDateTime.set(Calendar.SECOND, 0);
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
    }

    private void setupCategorySpinner() {
        String[] categories = {"Work", "Personal", "Health", "Shopping"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategories.setAdapter(adapter);
    }

    private void setupVibrationSpinner() {
        String[] vibrations = {"Default", "Short", "Long", "Heartbeat"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vibrations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVibration.setAdapter(adapter);
    }

    private void loadTaskDetails() {
        fbRepo.getTaskDetails(taskId, new FirebaseRepo.OnTaskDetailLoadedListener() {
            @Override
            public void onTaskLoaded(Task task) {
                etTaskName.setText(task.getTitle());
                etNotes.setText(task.getNote());
                reminderOn = task.isReminder();

                if (task.getCategory() != null) {
                    ArrayAdapter<String> categoryAdapter = (ArrayAdapter<String>) spinnerCategories.getAdapter();
                    int categoryPosition = categoryAdapter.getPosition(task.getCategory());
                    if (categoryPosition >= 0) {
                        spinnerCategories.setSelection(categoryPosition);
                    }
                }

                if (task.getVibration() != null) {
                    ArrayAdapter<String> vibrationAdapter = (ArrayAdapter<String>) spinnerVibration.getAdapter();
                    int vibrationPosition = vibrationAdapter.getPosition(task.getVibration());
                    if (vibrationPosition >= 0) {
                        spinnerVibration.setSelection(vibrationPosition);
                    }
                }

                if (task.getTaskDate() != null) {
                    dueDateTime.setTime(task.getTaskDate());
                    updateDateAndTimeButtons();
                }

                if (task.getRingtone() != null) {
                    selectedRingtoneUri = Uri.parse(task.getRingtone());
                    try {
                        btnSelectRingtone.setText(RingtoneManager.getRingtone(TaskDetailActivity.this, selectedRingtoneUri).getTitle(TaskDetailActivity.this));
                    } catch (Exception e) {
                        btnSelectRingtone.setText("Select Sound");
                    }
                }

                updateReminderButton();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(TaskDetailActivity.this, "Error loading task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateTask() {
        String title = etTaskName.getText().toString().trim();
        String category = spinnerCategories.getSelectedItem().toString();
        String vibration = spinnerVibration.getSelectedItem().toString();
        String notes = etNotes.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Task name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> taskUpdates = new HashMap<>();
        taskUpdates.put("title", title);
        taskUpdates.put("category", category);
        taskUpdates.put("vibration", vibration);
        taskUpdates.put("notes", notes);
        taskUpdates.put("taskDate", dueDateTime.getTime());
        taskUpdates.put("reminder", reminderOn);
        if (selectedRingtoneUri != null) {
            taskUpdates.put("ringtone", selectedRingtoneUri.toString());
        }

        fbRepo.updateTask(taskId, taskUpdates, (message, e) -> {
            if (e == null) {
                Toast.makeText(TaskDetailActivity.this, message, Toast.LENGTH_SHORT).show();

                if (reminderOn) {
                    Task updatedTask = new Task();
                    updatedTask.setId(taskId);
                    updatedTask.setTitle(title);
                    updatedTask.setCategory(category);
                    updatedTask.setNote(notes);
                    updatedTask.setTaskDate(dueDateTime.getTime());
                    updatedTask.setVibration(vibration);
                    if (selectedRingtoneUri != null) {
                        updatedTask.setRingtone(selectedRingtoneUri.toString());
                    }

                    scheduleAlarmsForTask(TaskDetailActivity.this, updatedTask);
                }

                Intent resultIntent = new Intent();
                resultIntent.putExtra("isTaskUpdated", true);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(TaskDetailActivity.this, "Error updating task", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void scheduleAlarmsForTask(android.content.Context context, Task task) {
        long dueTime = task.getTaskDate().getTime();
        long currentTime = System.currentTimeMillis();
        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;

        if (dueTime > currentTime) {
            scheduleNotification(context, task, dueTime, false);
        }

        long timeDifference = dueTime - currentTime;
        if (timeDifference > 0 && timeDifference < twentyFourHoursInMillis) {
        } else if (timeDifference >= twentyFourHoursInMillis) {
            long advanceTime = dueTime - twentyFourHoursInMillis;
            scheduleNotification(context, task, advanceTime, true);
        }
    }

    private void scheduleNotification(android.content.Context context, Task task, long time, boolean isAdvance) {
        String uniqueId = isAdvance ? task.getId() + "_advance" : task.getId();

        Intent intent = new Intent(context, com.example.login_signup.TaskReminderReceiver.class);
        intent.putExtra("taskId", uniqueId);
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

        int requestCode = uniqueId.hashCode();

        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(android.content.Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                if (!isAdvance) {
                    Intent showTaskIntent = new Intent(context, com.example.login_signup.HomeActivity.class);
                    android.app.PendingIntent showTaskPendingIntent = android.app.PendingIntent.getActivity(
                            context,
                            requestCode,
                            showTaskIntent,
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
                    );
                    android.app.AlarmManager.AlarmClockInfo alarmClockInfo = new android.app.AlarmManager.AlarmClockInfo(time, showTaskPendingIntent);
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
                } else {
                    alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, time, pendingIntent);
                }
            } catch (SecurityException se) {
                Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
}