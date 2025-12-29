package com.example.login_signup.achievement;

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

import com.example.login_signup.R;
import com.example.login_signup.classes.FirebaseRepo;

import java.util.ArrayList;
import java.util.List;

public class AchievementsFragment extends Fragment {

    private TextView tvCurrentLevel, tvNextLevelInfo, tvStreakDays;
    private ProgressBar pbLevelProgress;
    private RecyclerView rvAchievements;

    private FirebaseRepo firebaseRepo;

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

        firebaseRepo = new FirebaseRepo();

        tvCurrentLevel = view.findViewById(R.id.tvCurrentLevel);
        tvNextLevelInfo = view.findViewById(R.id.tvNextLevelInfo);
        tvStreakDays = view.findViewById(R.id.tvStreakDays);
        pbLevelProgress = view.findViewById(R.id.pbLevelProgress);
        rvAchievements = view.findViewById(R.id.rvAchievements);

        loadData();
    }

    private void loadData() {
        firebaseRepo.getUserStreak(new FirebaseRepo.OnStreakLoadedListener() {
            @Override
            public void onStreakLoaded(int streak) {
                currentStreak = streak;
                loadTaskStats();
            }

            @Override
            public void onError(Exception e) {
                Log.e("Achieve", "Error streak", e);
                loadTaskStats();
            }
        });
    }

    private void loadTaskStats() {
        firebaseRepo.getTaskStatistics(new FirebaseRepo.OnTaskStatsLoadedListener() {
            @Override
            public void onStatsLoaded(int totalCreated, int totalCompleted) {
                totalTasksCreated = totalCreated;
                totalTasksCompleted = totalCompleted;
                updateUI();
            }

            @Override
            public void onError(Exception e) {
                Log.e("Achieve", "Error analysis tasks", e);
                updateUI();
            }
        });
    }

    private void updateUI() {
        if (getContext() == null) return;

        int currentLevel = (totalTasksCreated / 10) + 1;
        int progressInLevel = totalTasksCreated % 10;
        int tasksNeedForNextLevel = 10 - progressInLevel;

        tvCurrentLevel.setText(String.valueOf(currentLevel));
        pbLevelProgress.setMax(10);
        pbLevelProgress.setProgress(progressInLevel);
        tvNextLevelInfo.setText("Create " + tasksNeedForNextLevel + " more tasks to reach Level " + (currentLevel + 1));

        tvStreakDays.setText(currentStreak + " Days");

        List<Achievement> list = new ArrayList<>();


        int colorCreate = Color.parseColor("#2196F3");

        list.add(new Achievement(R.drawable.baseline_flag_24, colorCreate,
                "Getting Started", "Create your first task",
                Math.min(totalTasksCreated, 1), 1));

        list.add(new Achievement(R.drawable.baseline_edit_note_24, colorCreate,
                "Planner", "Create 10 tasks",
                Math.min(totalTasksCreated, 10), 10));

        list.add(new Achievement(R.drawable.baseline_rocket_launch_24, Color.parseColor("#673AB7"),
                "Busy Bee", "Create 50 tasks",
                Math.min(totalTasksCreated, 50), 50));

        list.add(new Achievement(R.drawable.baseline_rocket_launch_24, Color.parseColor("#9C27B0"),
                "Task Master", "Create 100 tasks",
                Math.min(totalTasksCreated, 100), 100));

        int colorComplete = Color.parseColor("#4CAF50");

        list.add(new Achievement(R.drawable.baseline_check_circle_24, colorComplete,
                "Done!", "Complete your first task",
                Math.min(totalTasksCompleted, 1), 1));

        list.add(new Achievement(R.drawable.baseline_offline_bolt_24, colorComplete,
                "Productive", "Complete 10 tasks",
                Math.min(totalTasksCompleted, 10), 10));

        list.add(new Achievement(R.drawable.baseline_emoji_events_24, Color.parseColor("#FFC107"),
                "Excellent", "Complete 50 tasks",
                Math.min(totalTasksCompleted, 50), 50));

       
        int colorFire = Color.parseColor("#FF5722");

        list.add(new Achievement(R.drawable.ic_fire_24, colorFire,
                "Warming Up", "3-day streak",
                Math.min(currentStreak, 3), 3));

        list.add(new Achievement(R.drawable.ic_fire_24, Color.parseColor("#F44336"),
                "Persistent", "7-day streak",
                Math.min(currentStreak, 7), 7));

        list.add(new Achievement(R.drawable.baseline_emoji_events_24, Color.parseColor("#FFD700"),
                "Iron Habit", "30-day streak",
                Math.min(currentStreak, 30), 30));

        AchievementsAdapter adapter = new AchievementsAdapter(list);
        rvAchievements.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAchievements.setAdapter(adapter);
    }
}
