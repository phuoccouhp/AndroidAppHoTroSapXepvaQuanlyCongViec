package com.example.login_signup.task;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.login_signup.HomeActivity;
import com.example.login_signup.NotificationHelper;
import com.example.login_signup.R;
import com.example.login_signup.TaskReminderReceiver;
import com.example.login_signup.classes.FirebaseRepo;
import com.example.login_signup.classes.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTaskName, etNotes;
    private Spinner spinnerCategories, spinnerVibration, spinnerPriority;
    private Button btnSetDueDate, btnSetTime, btnSetReminder, btnSelectRingtone;
    private FloatingActionButton btnSaveTask;
    private ImageButton btnBack;

    private Calendar dueDateTime = Calendar.getInstance();
    private boolean reminderOn = false;
    private Uri selectedRingtoneUri;

    private FirebaseRepo fbRepo;
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

        fbRepo = new FirebaseRepo();

        initViews();
        setupSpinners();
        setupListeners();
    }

    private void initViews() {
        etTaskName = findViewById(R.id.etTaskName);
        etNotes = findViewById(R.id.etNotes);
        spinnerCategories = findViewById(R.id.spinnerCategories);
        spinnerVibration = findViewById(R.id.spinnerVibration);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        btnSetDueDate = findViewById(R.id.btnSetDueDate);
        btnSetTime = findViewById(R.id.btnSetTime);
        btnSetReminder = findViewById(R.id.btnSetReminder);
        btnSelectRingtone = findViewById(R.id.btnSelectRingtone);
        btnSaveTask = findViewById(R.id.btnSaveTask);
        btnBack = findViewById(R.id.btnBack);
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

        String[] priorities = {"Normal", "High"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, priorities);
        spinnerPriority.setAdapter(priorityAdapter);
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

        btnSaveTask.setOnClickListener(v -> saveNewTask());
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
        Date creationDate = new Date();

        FirebaseUser currentUser = fbRepo.getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "anonymous";

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("uid", userId);
        taskData.put("title", name);
        taskData.put("category", spinnerCategories.getSelectedItem().toString());
        taskData.put("priority", spinnerPriority.getSelectedItem().toString());
        taskData.put("notes", noteContent);
        taskData.put("reminder", reminderOn);
        taskData.put("completed", false);
        taskData.put("taskDate", dueDateTime.getTime());
        taskData.put("creationDate", creationDate);
        taskData.put("vibration", spinnerVibration.getSelectedItem().toString());
        if (selectedRingtoneUri != null) {
            taskData.put("ringtone", selectedRingtoneUri.toString());
        }

        fbRepo.addTask(taskData, new FirebaseRepo.OnAddTaskListener() {
            @Override
            public void onSuccess(String taskId) {
                Toast.makeText(AddTaskActivity.this, "Task saved", Toast.LENGTH_SHORT).show();

                if (reminderOn) {
                    Task taskToSchedule = new Task();
                    taskToSchedule.setId(taskId);
                    taskToSchedule.setTitle(name);
                    taskToSchedule.setNote(noteContent);
                    taskToSchedule.setCategory(spinnerCategories.getSelectedItem().toString());
                    taskToSchedule.setTaskDate(dueDateTime.getTime());
                    taskToSchedule.setVibration(spinnerVibration.getSelectedItem().toString());
                    if(selectedRingtoneUri != null) taskToSchedule.setRingtone(selectedRingtoneUri.toString());

                    scheduleAlarmsForTask(AddTaskActivity.this, taskToSchedule);
                }

                fbRepo.logTaskAction(taskId, name, "CREATED");

                updateWidget();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AddTaskActivity.this, "Error saving task", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWidget() {
        Intent widgetUpdateIntent = new Intent(this, TaskWidgetProvider.class);
        widgetUpdateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(
                new ComponentName(getApplication(), TaskWidgetProvider.class));
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(widgetUpdateIntent);
    }

    private void scheduleAlarmsForTask(Context context, Task task) {
        long dueTime = task.getTaskDate().getTime();
        long currentTime = System.currentTimeMillis();
        long twentyFourHoursInMillis = 24 * 60 * 60 * 1000;

        if (dueTime > currentTime) {
            scheduleNotification(context, task, dueTime, false);
        }

        long timeDifference = dueTime - currentTime;
        if (timeDifference > 0 && timeDifference < twentyFourHoursInMillis) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM", Locale.getDefault());
            String taskInfo = "Due at: " + sdf.format(task.getTaskDate());
            NotificationHelper.showAdvanceNotification(context, task.getTitle(), taskInfo, task.getId().hashCode() + 1);
        } else if (timeDifference >= twentyFourHoursInMillis) {
            long advanceTime = dueTime - twentyFourHoursInMillis;
            scheduleNotification(context, task, advanceTime, true);
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
                if (!isAdvance) {
                    Intent showTaskIntent = new Intent(context, HomeActivity.class);
                    PendingIntent showTaskPendingIntent = PendingIntent.getActivity(context, requestCode, showTaskIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(time, showTaskPendingIntent);
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                }
            } catch (SecurityException se) {
                Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
}