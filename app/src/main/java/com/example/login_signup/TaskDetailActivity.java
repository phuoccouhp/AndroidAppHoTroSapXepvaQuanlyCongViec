package com.example.login_signup;

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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TaskDetailActivity extends AppCompatActivity {

    private EditText etTaskName, etNotes;
    private Spinner spinnerCategories, spinnerVibration;
    private Button btnSetDueDate, btnSetTime, btnSetReminder, btnSelectRingtone;
    private ImageButton btnBack;
    private FloatingActionButton btnSaveTask;

    private FirebaseFirestore db;
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

        db = FirebaseFirestore.getInstance();
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
        db.collection("tasks").document(taskId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String category = documentSnapshot.getString("category");
                        String vibration = documentSnapshot.getString("vibration");
                        String note = documentSnapshot.getString("notes");
                        Date taskDate = documentSnapshot.getDate("taskDate");
                        reminderOn = Boolean.TRUE.equals(documentSnapshot.getBoolean("reminder"));
                        String ringtoneUriString = documentSnapshot.getString("ringtone");
                        if (ringtoneUriString != null) {
                            selectedRingtoneUri = Uri.parse(ringtoneUriString);
                        }

                        etTaskName.setText(title);
                        etNotes.setText(note);

                        if (category != null) {
                            ArrayAdapter<String> categoryAdapter = (ArrayAdapter<String>) spinnerCategories.getAdapter();
                            int categoryPosition = categoryAdapter.getPosition(category);
                            if (categoryPosition >= 0) {
                                spinnerCategories.setSelection(categoryPosition);
                            }
                        }

                        if (vibration != null) {
                            ArrayAdapter<String> vibrationAdapter = (ArrayAdapter<String>) spinnerVibration.getAdapter();
                            int vibrationPosition = vibrationAdapter.getPosition(vibration);
                            if (vibrationPosition >= 0) {
                                spinnerVibration.setSelection(vibrationPosition);
                            }
                        }

                        if (taskDate != null) {
                            dueDateTime.setTime(taskDate);
                            updateDateAndTimeButtons();
                        }

                        updateReminderButton();
                        if (selectedRingtoneUri != null) {
                            try {
                                btnSelectRingtone.setText(RingtoneManager.getRingtone(this, selectedRingtoneUri).getTitle(this));
                            } catch (Exception e) {
                                btnSelectRingtone.setText("Select Sound");
                            }
                        }

                    } else {
                        Toast.makeText(TaskDetailActivity.this, "Task not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TaskDetailActivity.this, "Error loading task", Toast.LENGTH_SHORT).show();
                    finish();
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


        db.collection("tasks").document(taskId)
                .update(taskUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TaskDetailActivity.this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("isTaskUpdated", true);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(TaskDetailActivity.this, "Error updating task", Toast.LENGTH_SHORT).show());
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
}
