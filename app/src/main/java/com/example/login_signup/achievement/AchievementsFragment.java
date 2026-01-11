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

// Fragment hiển thị các thành tựu và cấp độ của người dùng
public class AchievementsFragment extends Fragment {

    private TextView tvCurrentLevel, tvNextLevelInfo, tvStreakDays;
    private ProgressBar pbLevelProgress;
    private RecyclerView rvAchievements;

    private int currentStreak = 0;
    private int totalTasksCreated = 0;
    private int totalTasksCompleted = 0;

    private FirebaseRepo fbRepo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_achievements, container, false);
    }

    // Phương thức khởi tạo, ánh xạ và tải dữ liệu sau khi View được tạo
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fbRepo = new FirebaseRepo(); // Khởi tạo đối tượng cho các phương thức xử lý dữ liệu với Firebase

        // Ánh xạ các dữ liệu từ giao diện XML
        tvCurrentLevel = view.findViewById(R.id.tvCurrentLevel);
        tvNextLevelInfo = view.findViewById(R.id.tvNextLevelInfo);
        tvStreakDays = view.findViewById(R.id.tvStreakDays);
        pbLevelProgress = view.findViewById(R.id.pbLevelProgress);
        rvAchievements = view.findViewById(R.id.rvAchievements);

        loadData(); // Tải dữ liệu từ Firebase
    }

    // Tải dữ liệu chuỗi ngày đăng nhập (streak) liên tiếp
    private void loadData() {
        // Gọi phương thức getUserStreak để tải dữ liệu streak của người dùng
        fbRepo.getUserStreak(new FirebaseRepo.OnStreakLoadedListener() {
            @Override
            public void onStreakLoaded(int streak) {
                currentStreak = streak;
                loadTaskStats(); // Tải thống kê về số lượng công việc đã tạo và đã hoàn thành
            }

            @Override
            public void onError(Exception e) {
                Log.e("Achieve", "Error streak", e);
                loadTaskStats();
            }
        });
    }

    // Tải thống kê về số lượng công việc đã tạo và đã hoàn thành
    private void loadTaskStats() {
        // Gọi phương thức getTaskStatistics để tải thống kê về số lượng công việc đã tạo và đã hoàn thành
        fbRepo.getTaskStatistics(new FirebaseRepo.OnTaskStatsLoadedListener() {
            @Override
            public void onStatsLoaded(int totalCreated, int totalCompleted) {
                totalTasksCreated = totalCreated;
                totalTasksCompleted = totalCompleted;

                updateUI(); // Cập nhật giao diện sau khi đã có đầy đủ dữ liệu
            }

            @Override
            public void onError(Exception e) {
                Log.e("Achieve", "Error analysis tasks", e);
                updateUI();
            }
        });
    }

    // Tính toán logic cấp độ và hiển thị danh sách thành tựu lên màn hình
    private void updateUI() {if (getContext() == null) return;

        // Logic tính cấp độ: Cứ mỗi 10 công việc tạo ra sẽ tăng 1 cấp
        int currentLevel = (totalTasksCreated / 10) + 1;
        int progressInLevel = totalTasksCreated % 10;
        int tasksNeedForNextLevel = 10 - progressInLevel;

        // Hiển thị thông tin cấp độ hiện tại
        tvCurrentLevel.setText(String.valueOf(currentLevel));
        tvNextLevelInfo.setText("Create " + tasksNeedForNextLevel + " more tasks to reach Level " + (currentLevel + 1));
        pbLevelProgress.setMax(10);
        pbLevelProgress.setProgress(progressInLevel);

        // Hiển thị số ngày streak
        tvStreakDays.setText(currentStreak + " Days");

        // Khởi tạo danh sách các mốc thành tựu để hiển thị trong RecyclerView
        List<Achievement> list = new ArrayList<>();

        // -- Achievement tạo công việc --
        int colorCreate = Color.parseColor("#2196F3"); // Xanh dương

        // Mốc 1: Tạo 1 công việc
        list.add(new Achievement(R.drawable.baseline_flag_24, colorCreate,
                "Getting Started", "Create your first task",
                Math.min(totalTasksCreated, 1), 1));

        // Mốc 2: Tạo 10 công việc
        list.add(new Achievement(R.drawable.baseline_edit_note_24, colorCreate,
                "The Planner", "Create 10 tasks",
                Math.min(totalTasksCreated, 10), 10));

        // Mốc 3: Tạo 50 công việc
        list.add(new Achievement(R.drawable.baseline_rocket_launch_24, Color.parseColor("#673AB7"),
                "Hard Working", "Create 50 tasks",
                Math.min(totalTasksCreated, 50), 50));

        // Mốc 4: Tạo 100 công việc
        list.add(new Achievement(R.drawable.baseline_rocket_launch_24, Color.parseColor("#9C27B0"),
                "Task Master", "Create 100 tasks",
                Math.min(totalTasksCreated, 100), 100));

        // -- Achievement hoàn thành công việc --
        int colorComplete = Color.parseColor("#4CAF50"); // Xanh lá

        // Mốc 1: Hoàn thành 1 công việc
        list.add(new Achievement(R.drawable.baseline_check_circle_24, colorComplete,
                "Done!", "Complete your first task",
                Math.min(totalTasksCompleted, 1), 1));

        // Mốc 2: Hoàn thành 10 công việc
        list.add(new Achievement(R.drawable.baseline_offline_bolt_24, colorComplete,
                "Productive", "Complete 10 tasks",
                Math.min(totalTasksCompleted, 10), 10));

        // Mốc 3: Hoàn thành 50 công việc
        list.add(new Achievement(R.drawable.baseline_emoji_events_24, Color.parseColor("#FFC107"),
                "Excellence", "Complete 50 tasks",
                Math.min(totalTasksCompleted, 50), 50));


        // -- Achievement chuỗi ngày đăng nhập --
        int colorFire = Color.parseColor("#FF5722"); // Đỏ

        // Mốc 1: Đăng nhập 1 ngày
        list.add(new Achievement(R.drawable.ic_fire_24, colorFire,
                "Kickstart", "3-day streak",
                Math.min(currentStreak, 3), 3));

        // Mốc 2: Đăng nhập 7 ngày
        list.add(new Achievement(R.drawable.ic_fire_24, Color.parseColor("#F44336"),
                "Persistence", "7-day streak",
                Math.min(currentStreak, 7), 7));

        // Mốc 3: Đăng nhập 30 ngày
        list.add(new Achievement(R.drawable.baseline_emoji_events_24, Color.parseColor("#FFD700"),
                "Iron Habit", "30-day streak",
                Math.min(currentStreak, 30), 30));

        // Thiết lập Adapter và hiển thị lên RecyclerView
        AchievementsAdapter adapter = new AchievementsAdapter(list);
        rvAchievements.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAchievements.setAdapter(adapter);
    }
}