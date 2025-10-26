package com.example.login_signup;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TaskDetailFragment extends Fragment {

    private EditText etTaskName, etNotes;
    private Spinner spinnerCategories;
    private Button btnPickDate, btnPickTime, btnReminder;
    private FloatingActionButton btnConfirmTask, btnCancelTask;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Calendar selectedDateTime;
    private String taskId;

    public TaskDetailFragment() {}

    public static TaskDetailFragment newInstance(Task task) {
        TaskDetailFragment fragment = new TaskDetailFragment();
        Bundle args = new Bundle();
        args.putString("taskId", task.getId());
        args.putString("title", task.getTitle());
        args.putString("category", task.getCategory());
        args.putString("time", task.getTime());
        args.putString("date", task.getDate());
        args.putString("note", task.getNote());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_task_detail, container, false);

        etTaskName = v.findViewById(R.id.et_task_name);
        etNotes = v.findViewById(R.id.et_notes);
        spinnerCategories = v.findViewById(R.id.spinner_categories);
        btnPickDate = v.findViewById(R.id.btn_pick_date);
        btnPickTime = v.findViewById(R.id.btn_pick_time);
        btnReminder = v.findViewById(R.id.btn_reminder);
        btnConfirmTask = v.findViewById(R.id.btn_confirm_task);
        btnCancelTask = v.findViewById(R.id.btn_cancel_task);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        selectedDateTime = Calendar.getInstance();

        String[] categories = {"Work", "Personal", "Health", "Shopping", "Habit"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategories.setAdapter(adapter);

        Bundle args = getArguments();
        if (args != null) {
            taskId = args.getString("taskId");
            etTaskName.setText(args.getString("title"));
            etNotes.setText(args.getString("note"));

            String cat = args.getString("category");
            if (cat != null) {
                int pos = adapter.getPosition(cat);
                if (pos >= 0) spinnerCategories.setSelection(pos);
            }

            String dateStr = args.getString("date");
            String timeStr = args.getString("time");
            if (dateStr != null && timeStr != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm", Locale.getDefault());
                    selectedDateTime.setTime(sdf.parse(dateStr + " " + timeStr));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        btnPickDate.setOnClickListener(view -> {
            int y = selectedDateTime.get(Calendar.YEAR);
            int m = selectedDateTime.get(Calendar.MONTH);
            int d = selectedDateTime.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(requireContext(), (DatePicker dp, int year, int month, int day) -> {
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, day);
                btnPickDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDateTime.getTime()));
            }, y, m, d).show();
        });

        btnPickTime.setOnClickListener(view -> {
            int h = selectedDateTime.get(Calendar.HOUR_OF_DAY);
            int min = selectedDateTime.get(Calendar.MINUTE);
            new TimePickerDialog(requireContext(), (TimePicker tp, int hour, int minute) -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedDateTime.set(Calendar.MINUTE, minute);
                btnPickTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
            }, h, min, true).show();
        });

        btnCancelTask.setOnClickListener(view ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        btnConfirmTask.setOnClickListener(view -> saveTaskChanges());

        return v;
    }

    private void saveTaskChanges() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null || TextUtils.isEmpty(taskId)) {
            Toast.makeText(getContext(), "Lỗi: Không có ID người dùng hoặc task!", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTaskName.getText().toString().trim();
        String note = etNotes.getText().toString().trim();
        String category = spinnerCategories.getSelectedItem().toString();

        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên task!", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference docRef = db.collection("tasks").document(taskId);
        docRef.update(
                "title", title,
                "note", note,
                "category", category,
                "taskDate", new Timestamp(selectedDateTime.getTime())
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Đã lưu thay đổi!", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
