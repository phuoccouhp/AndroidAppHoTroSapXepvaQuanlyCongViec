package com.example.login_signup;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class AchievementsFragment extends Fragment {

    private TextView tvCurrentLevel, tvNextLevelInfo, tvStreakDays;
    private ProgressBar pbLevelProgress;
    private RecyclerView rvAchievements;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private int currentStreak = 0;
    private int totalTasksCreated = 0;
    private int totalTasksCompleted = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_achievements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvCurrentLevel = view.findViewById(R.id.tvCurrentLevel);
        tvNextLevelInfo = view.findViewById(R.id.tvNextLevelInfo);
        tvStreakDays = view.findViewById(R.id.tvStreakDays);
        pbLevelProgress = view.findViewById(R.id.pbLevelProgress);
        rvAchievements = view.findViewById(R.id.rvAchievements);

        loadUserData();
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long s = documentSnapshot.getLong("streak");
                        currentStreak = (s != null) ? s.intValue() : 0;
                    } else {
                        currentStreak = 0;
                    }
                    loadTaskData(uid);
                })
                .addOnFailureListener(e -> {
                    Log.e("Achieve", "Error loading user data", e);
                    loadTaskData(uid);
                });
    }

    private void loadTaskData(String uid) {
        db.collection("tasks").whereEqualTo("uid", uid).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    totalTasksCreated = queryDocumentSnapshots.size();
                    totalTasksCompleted = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Boolean completed = doc.getBoolean("completed");
                        if (completed != null && completed) {
                            totalTasksCompleted++;
                        }
                    }
                    updateUI();
                });
    }

    private void updateUI() {
        // LOGIC LEVEL
        int currentLevel = (totalTasksCreated / 10) + 1;
        int progressInLevel = totalTasksCreated % 10;
        int tasksNeedForNextLevel = 10 - progressInLevel;

        tvCurrentLevel.setText(String.valueOf(currentLevel));
        pbLevelProgress.setMax(10);
        pbLevelProgress.setProgress(progressInLevel);
        tvNextLevelInfo.setText("Tạo thêm " + tasksNeedForNextLevel + " công việc để lên Level " + (currentLevel + 1));

        tvStreakDays.setText(currentStreak + " Ngày");

        List<Achievement> list = new ArrayList<>();


        int colorCreate = Color.parseColor("#2196F3"); 

        list.add(new Achievement(R.drawable.baseline_flag_24, colorCreate,
                "Khởi Đầu", "Tạo công việc đầu tiên",
                Math.min(totalTasksCreated, 1), 1));

        list.add(new Achievement(R.drawable.baseline_edit_note_24, colorCreate,
                "Lập Kế Hoạch", "Tạo 10 công việc",
                Math.min(totalTasksCreated, 10), 10));

        list.add(new Achievement(R.drawable.baseline_rocket_launch_24, Color.parseColor("#673AB7"), 
                "Người Bận Rộn", "Tạo 50 công việc",
                Math.min(totalTasksCreated, 50), 50));

        list.add(new Achievement(R.drawable.baseline_rocket_launch_24, Color.parseColor("#9C27B0"), 
                "Chuyên Gia Task", "Tạo 100 công việc",
                Math.min(totalTasksCreated, 100), 100));

        int colorComplete = Color.parseColor("#4CAF50");

        list.add(new Achievement(R.drawable.baseline_check_circle_24, colorComplete,
                "Hoàn Thành", "Hoàn thành công việc đầu tiên",
                Math.min(totalTasksCompleted, 1), 1));

        list.add(new Achievement(R.drawable.baseline_offline_bolt_24, colorComplete,
                "Năng Suất", "Hoàn thành 10 công việc",
                Math.min(totalTasksCompleted, 10), 10));

        list.add(new Achievement(R.drawable.baseline_emoji_events_24, Color.parseColor("#FFC107"), 
                "Xuất Sắc", "Hoàn thành 50 công việc",
                Math.min(totalTasksCompleted, 50), 50));

       
        int colorFire = Color.parseColor("#FF5722");

        list.add(new Achievement(R.drawable.ic_fire_24, colorFire,
                "Làm Nóng", "Chuỗi 3 ngày liên tiếp",
                Math.min(currentStreak, 3), 3));

        list.add(new Achievement(R.drawable.ic_fire_24, Color.parseColor("#F44336"),
                "Kiên Trì", "Chuỗi 7 ngày liên tiếp",
                Math.min(currentStreak, 7), 7));

        list.add(new Achievement(R.drawable.baseline_emoji_events_24, Color.parseColor("#FFD700"),
                "Thói Quen Thép", "Chuỗi 30 ngày liên tiếp",
                Math.min(currentStreak, 30), 30));

        AchievementsAdapter adapter = new AchievementsAdapter(list);
        rvAchievements.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAchievements.setAdapter(adapter);
    }
}
