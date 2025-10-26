package com.example.login_signup;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTaskName, etNotes;
    private Spinner spinnerCategories;
    private Button btnSetDueDate, btnSetTime, btnSetReminder;
    private FloatingActionButton fabSaveTask;

    private String dueDate = "";
    private String dueTime = "";
    private boolean reminderOn = false;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        etTaskName = findViewById(R.id.et_task_name);
        etNotes = findViewById(R.id.et_notes);
        spinnerCategories = findViewById(R.id.spinner_categories);
        btnSetDueDate = findViewById(R.id.btn_set_due_date);
        btnSetTime = findViewById(R.id.btn_set_time);
        btnSetReminder = findViewById(R.id.btn_set_reminder);
        fabSaveTask = findViewById(R.id.fab_save_task);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Spinner dữ liệu
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Work", "Personal", "Study", "Other"});
        spinnerCategories.setAdapter(adapter);

        // Chọn ngày
        btnSetDueDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                dueDate = day + "/" + (month + 1) + "/" + year;
                btnSetDueDate.setText(dueDate);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Chọn giờ
        btnSetTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, hour, minute) -> {
                dueTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                btnSetTime.setText(dueTime);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        // Bật/Tắt nhắc nhở
        btnSetReminder.setOnClickListener(v -> {
            reminderOn = !reminderOn;
            btnSetReminder.setText(reminderOn ? "Reminder ON" : "Reminder OFF");
        });

        // Lưu Task
        fabSaveTask.setOnClickListener(v -> saveTask());
    }

    private void saveTask() {
        String name = etTaskName.getText().toString().trim();
        String category = spinnerCategories.getSelectedItem().toString();
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

        // Lưu thời gian
        Calendar c = Calendar.getInstance();
        if (!dueDate.isEmpty() && !dueTime.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                c.setTime(sdf.parse(dueDate + " " + dueTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        taskData.put("taskDate", c.getTime());

        db.collection("tasks")
                .add(taskData)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
