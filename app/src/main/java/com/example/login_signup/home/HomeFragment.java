package com.example.login_signup.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.login_signup.R;
import com.example.login_signup.achievement.AchievementsFragment;
import com.example.login_signup.analysis.AnalysisFragment;
import com.example.login_signup.history.TaskHistoryActivity;
import com.example.login_signup.task.TaskFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;

// HomeFragment: Fragment quản lý việc chuyển đổi giữa danh sách công việc (Task), thành tựu (Achievements) và phân tích (Analysis)
public class HomeFragment extends Fragment {
    private MaterialButtonToggleGroup toggleGroup;
    private Button btnTabHome, btnTabAchievements, btnTabAnalysis;
    private ImageButton btnHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        // Ánh xạ các thành phần giao diện
        toggleGroup = v.findViewById(R.id.toggleGroup);
        btnTabHome = v.findViewById(R.id.btnTabHome);
        btnTabAchievements = v.findViewById(R.id.btnTabAchievements);
        btnTabAnalysis = v.findViewById(R.id.btnTabAnalysis);
        btnHistory = v.findViewById(R.id.btnHistory);

        // Thiết lập tab mặc định khi lần đầu mở màn hình
        if (savedInstanceState == null) {
            loadFragment(new TaskFragment());
            updateButtonStyles(btnTabHome);
        }

        // Lắng nghe sự kiện khi người dùng nhấn chuyển đổi giữa các nút trong ToggleGroup
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnTabHome) {
                    // Chuyển sang danh sách công việc
                    loadFragment(new TaskFragment());
                    updateButtonStyles(btnTabHome);

                } else if (checkedId == R.id.btnTabAchievements) {
                    // Chuyển sang màn hình thành tựu
                    loadFragment(new AchievementsFragment());
                    updateButtonStyles(btnTabAchievements);

                } else if (checkedId == R.id.btnTabAnalysis) {
                    // Chuyển sang màn hình phân tích số liệu
                    loadFragment(new AnalysisFragment());
                    updateButtonStyles(btnTabAnalysis);
                }
            }
        });

        // Nút mở lịch sử hành động công việc (Logs)
        btnHistory.setOnClickListener(v1 ->{
            Intent intent = new Intent(getContext(), TaskHistoryActivity.class);
            startActivity(intent);
        });

        // Đảm bảo luôn có ít nhất một nút được chọn
        toggleGroup.setSelectionRequired(true);
        return v;
    }

    // Chuyển đổi Fragment được chọn
    private void loadFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    // Cập nhật màu sắc và kiểu dáng cho các nút Tab đang được chọn
    private void updateButtonStyles(Button activeButton) {
        int activeColor = ContextCompat.getColor(requireContext(), R.color.pastel_blue_dark);
        int inactiveColor = Color.WHITE;
        int activeTextColor = Color.WHITE;
        int inactiveTextColor = Color.parseColor("#64748B");

        // Đặt tất cả các nút về trạng thái bình thường (inactive)
        setButtonStyle(btnTabHome, inactiveColor, inactiveTextColor);
        setButtonStyle(btnTabAchievements, inactiveColor, inactiveTextColor);
        setButtonStyle(btnTabAnalysis, inactiveColor, inactiveTextColor);

        // Làm nổi bật nút đang được chọn (active)
        setButtonStyle(activeButton, activeColor, activeTextColor);
    }

    // Hàm gán màu nền và màu chữ cho Button
    private void setButtonStyle(Button btn, int backgroundColor, int textColor) {
        btn.setBackgroundColor(backgroundColor);
        btn.setTextColor(textColor);
    }
}
