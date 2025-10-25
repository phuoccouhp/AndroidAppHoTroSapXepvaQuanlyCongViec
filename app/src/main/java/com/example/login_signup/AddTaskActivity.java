package com.example.login_signup;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTaskName, etNotes;
    private Spinner spinnerCategories;
    private Button btnSetDueDate, btnSetTime, btnSetReminder;
    private FloatingActionButton fabSaveTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Khởi tạo các view
        etTaskName = findViewById(R.id.et_task_name);
        etNotes = findViewById(R.id.et_notes);
        spinnerCategories = findViewById(R.id.spinner_categories);
        btnSetDueDate = findViewById(R.id.btn_set_due_date);
        btnSetTime = findViewById(R.id.btn_set_time);
        btnSetReminder = findViewById(R.id.btn_set_reminder);
        fabSaveTask = findViewById(R.id.fab_save_task);

        // TODO: (Làm sau)
        // 1. Thêm data cho Spinner (ví dụ: "Work", "Personal")
        // 2. Thêm OnClickListener cho các nút Set Date, Set Time, Set Reminder
        //    (để mở DatePickerDialog, TimePickerDialog)
        // 3. Thêm OnClickListener cho fabSaveTask để lưu dữ liệu

        fabSaveTask.setOnClickListener(v -> {
            // Tạm thời chỉ đóng Activity khi nhấn Save
            finish();
        });
    }
}