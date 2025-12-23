package com.example.login_signup;

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

    public boolean isUnlocked() {
        return currentProgress >= maxProgress;
    }
}