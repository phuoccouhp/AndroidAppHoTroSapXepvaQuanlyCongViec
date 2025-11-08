package com.example.login_signup;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TaskDetailFragment extends Fragment {

    private TextView tvFragmentTitle;
    private TextInputEditText etTaskName, etNotes;
    private Spinner spinnerCategories, spinnerVibration;
    private Button btnSetDueDate, btnSetTime, btnSetReminder, btnSelectRingtone;
    private FloatingActionButton fabSaveChanges;

    private FirebaseFirestore db;
    private String currentTaskId;

    private Calendar dueDateTime = Calendar.getInstance();
    private boolean reminderOn = false;
    private Uri selectedRingtoneUri;

    public static TaskDetailFragment newInstance(String taskId) {
        TaskDetailFragment fragment = new TaskDetailFragment();
        Bundle args = new Bundle();
        args.putString("TASK_ID", taskId);
        fragment.setArguments(args);
        return fragment;
    }

    private final ActivityResultLauncher<Intent> ringtonePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (uri != null) {
                        selectedRingtoneUri = uri;
                        updateRingtoneButtonText();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_task, container, false);

        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupSpinners();

        if (getArguments() != null) {
            currentTaskId = getArguments().getString("TASK_ID");
        }

        if (currentTaskId != null && !currentTaskId.isEmpty()) {
            loadTaskForEditing();
        } else {
            Toast.makeText(getContext(), "Error: Task ID not found", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        }

        setupListeners();

        return view;
    }

    private void initViews(View view) {
        tvFragmentTitle = view.findViewById(R.id.tv_add_task_title);
        etTaskName = view.findViewById(R.id.et_task_name);
        etNotes = view.findViewById(R.id.et_notes);
        spinnerCategories = view.findViewById(R.id.spinner_categories);
        spinnerVibration = view.findViewById(R.id.spinner_vibration);
        btnSetDueDate = view.findViewById(R.id.btn_set_due_date);
        btnSetTime = view.findViewById(R.id.btn_set_time);
        btnSetReminder = view.findViewById(R.id.btn_set_reminder);
        btnSelectRingtone = view.findViewById(R.id.btn_select_ringtone);
        fabSaveChanges = view.findViewById(R.id.fab_save_task);
    }

    private void setupSpinners() {
        String[] categories = {"Work", "Personal", "Health", "Shopping"};
        ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategories.setAdapter(categoriesAdapter);

        String[] vibrations = {"Default", "Short", "Long", "Heartbeat"};
        ArrayAdapter<String> vibrationsAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, vibrations);
        spinnerVibration.setAdapter(vibrationsAdapter);
    }

    private void loadTaskForEditing() {
        tvFragmentTitle.setText("Edit Task");
        db.collection("tasks").document(currentTaskId).get()
                .addOnSuccessListener(this::populateUiWithTaskData)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load task details.", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                });
    }

    private void populateUiWithTaskData(DocumentSnapshot doc) {
        etTaskName.setText(doc.getString("title"));

        
        String noteContent = doc.getString("note");
        if (noteContent == null) {
            noteContent = doc.getString("notes"); 
        }
        etNotes.setText(noteContent);

        setSpinnerSelection(spinnerCategories, doc.getString("category"));
        setSpinnerSelection(spinnerVibration, doc.getString("vibration"));

        Date taskDate = doc.getDate("taskDate");
        if (taskDate != null) {
            dueDateTime.setTime(taskDate);
            updateDateAndTimeButtons();
        }

        reminderOn = doc.getBoolean("reminder") != null ? doc.getBoolean("reminder") : false;
        updateReminderButton();

        String ringtoneUriString = doc.getString("ringtone");
        if (ringtoneUriString != null) {
            selectedRingtoneUri = Uri.parse(ringtoneUriString);
            updateRingtoneButtonText();
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setupListeners() {
        btnSetDueDate.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), (view, year, month, day) -> {
                dueDateTime.set(Calendar.YEAR, year);
                dueDateTime.set(Calendar.MONTH, month);
                dueDateTime.set(Calendar.DAY_OF_MONTH, day);
                updateDateAndTimeButtons();
            }, dueDateTime.get(Calendar.YEAR), dueDateTime.get(Calendar.MONTH), dueDateTime.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSetTime.setOnClickListener(v -> {
            new TimePickerDialog(requireContext(), (view, hour, minute) -> {
                dueDateTime.set(Calendar.HOUR_OF_DAY, hour);
                dueDateTime.set(Calendar.MINUTE, minute);
                updateDateAndTimeButtons();
            }, dueDateTime.get(Calendar.HOUR_OF_DAY), dueDateTime.get(Calendar.MINUTE), true).show();
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

        fabSaveChanges.setOnClickListener(v -> updateTask());
    }

    private void updateTask() {
        String name = etTaskName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Task name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("title", name);
        taskData.put("category", spinnerCategories.getSelectedItem().toString());
        taskData.put("note", etNotes.getText().toString().trim());
        taskData.put("reminder", reminderOn);
        taskData.put("taskDate", dueDateTime.getTime());
        taskData.put("vibration", spinnerVibration.getSelectedItem().toString());
        if (selectedRingtoneUri != null) {
            taskData.put("ringtone", selectedRingtoneUri.toString());
        } else {
            taskData.put("ringtone", null);
        }

        db.collection("tasks").document(currentTaskId).update(taskData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Task updated successfully", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error updating task", Toast.LENGTH_SHORT).show());
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

    private void updateRingtoneButtonText() {
        if (selectedRingtoneUri != null) {
            try {
                String title = RingtoneManager.getRingtone(getContext(), selectedRingtoneUri).getTitle(getContext());
                btnSelectRingtone.setText(title);
            } catch (Exception e) {
                btnSelectRingtone.setText("Select Sound");
            }
        } else {
            btnSelectRingtone.setText("Select Sound");
        }
    }
}
