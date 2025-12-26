package com.example.login_signup.taskHistory;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.login_signup.R;
import com.example.login_signup.classes.FirebaseRepo;
import com.example.login_signup.classes.TaskLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewLog;
    private FirebaseRepo firebaseRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Task History");
        }

        recyclerViewLog = findViewById(R.id.recyclerViewLog);
        recyclerViewLog.setLayoutManager(new LinearLayoutManager(this));

        firebaseRepo = new FirebaseRepo();
        loadHistory();
    }

    private void loadHistory() {
        firebaseRepo.getTaskLogs(new FirebaseRepo.OnLogLoadedListener() {
            @Override
            public void onLogsLoaded(List<TaskLog> logs) {
                List<Object> groupedList = groupLogsByDate(logs);
                TaskHistoryAdapter adapter = new TaskHistoryAdapter(groupedList);
                recyclerViewLog.setAdapter(adapter);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(TaskHistoryActivity.this, "Error loading history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Object> groupLogsByDate(List<TaskLog> logs) {
        List<Object> grouped = new ArrayList<>();
        SimpleDateFormat headerFormat = new SimpleDateFormat("dd MMM • EEEE", Locale.getDefault());
        String lastHeader = "";
        String today = headerFormat.format(new java.util.Date());

        for (TaskLog log : logs) {
            if (log.getTimestamp() == null) continue;

            String currentHeader = headerFormat.format(log.getTimestamp());

            if (currentHeader.equals(today)) {
                currentHeader = currentHeader.split("•")[0] + " • Today";
            }

            if (!currentHeader.equals(lastHeader)) {
                grouped.add(currentHeader);
                lastHeader = currentHeader;
            }
            grouped.add(log);
        }
        return grouped;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}