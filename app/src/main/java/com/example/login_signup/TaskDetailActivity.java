package com.example.login_signup;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {

    /*
     * The following code is temporarily commented out to resolve a build error.
     * This error occurs because this Activity expects a layout with 'TextView's (like tv_task_name)
     * but the 'activity_task_detail.xml' has been updated with 'EditText's (like et_task_name) for the TaskDetailFragment.
     */

    // private TextView tvTaskName, tvTaskDetail, tvCategoryValue, tvDueDateValue, tvNotesValue;
    // private FirebaseFirestore db;
    // private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // db = FirebaseFirestore.getInstance();

        // tvTaskName = findViewById(R.id.tv_task_name);
        // tvTaskDetail = findViewById(R.id.tv_task_detail);
        // tvCategoryValue = findViewById(R.id.tv_category_value);
        // tvDueDateValue = findViewById(R.id.tv_due_date_value);
        // tvNotesValue = findViewById(R.id.tv_notes_value);

        // // Get task ID from intent
        // taskId = getIntent().getStringExtra("TASK_ID");

        // if (taskId != null && !taskId.isEmpty()) {
        //     loadTaskDetails();
        // } else {
        //     Toast.makeText(this, "Error: Task ID not found", Toast.LENGTH_SHORT).show();
        //     finish();
        // }

        // // Set listeners for buttons
        // findViewById(R.id.btn_confirm_task).setOnClickListener(v -> {
        //     // You can add logic to mark task as complete or update it
        //     finish(); 
        // });

        // findViewById(R.id.btn_cancel_task).setOnClickListener(v -> {
        //     // You can add logic for deleting the task
        //     finish();
        // });
    }

    /*
    private void loadTaskDetails() {
        db.collection("tasks").document(taskId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        displayTaskData(documentSnapshot);
                    } else {
                        Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayTaskData(DocumentSnapshot doc) {
        tvTaskName.setText(doc.getString("title"));
        
        // Display Detail
        String detail = doc.getString("detail");
        if (detail != null && !detail.isEmpty()) {
            tvTaskDetail.setText(detail);
            tvTaskDetail.setVisibility(View.VISIBLE);
            findViewById(R.id.tv_detail_label).setVisibility(View.VISIBLE);
        } else {
            tvTaskDetail.setVisibility(View.GONE);
            findViewById(R.id.tv_detail_label).setVisibility(View.GONE);
        }

        tvCategoryValue.setText(doc.getString("category"));
        
        // Format and display Date
        Date taskDate = doc.getDate("taskDate");
        if (taskDate != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault());
            tvDueDateValue.setText(formatter.format(taskDate));
        }

        // Display Notes
        String notes = doc.getString("notes");
        if (notes != null && !notes.isEmpty()) {
            tvNotesValue.setText(notes);
        } else {
            tvNotesValue.setText("No notes provided.");
        }
    }
    */
}
