package com.example.login_signup.achievement;

// Lớp Achievement đại diện cho một thành tựu
public class Achievement {
    int iconResId;
    int color;
    String title;
    String description;
    int currentProgress;
    int maxProgress;

    public Achievement(int iconResId, int color, String title, String description, int currentProgress, int maxProgress) {
        this.iconResId = iconResId;
        this.color = color;
        this.title = title;
        this.description = description;
        this.currentProgress = currentProgress;
        this.maxProgress = maxProgress;
    }

    // Phương thức kiểm tra xem thành tựu có được mở khóa hay chưa
    public boolean isUnlocked() {
        // So sánh tiến trình hiện tại với tiến trình định mức
        return currentProgress >= maxProgress;
    }
}