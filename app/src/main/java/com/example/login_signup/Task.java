package com.example.login_signup;

public class Task {
    private String title;
    private String category;
    private String time;
    private boolean completed;
    private String date; // 🔹 thêm trường date (định dạng yyyy-MM-dd hoặc yyyyMMdd)

    public Task() {
        // Bắt buộc có constructor rỗng để Firestore có thể map dữ liệu
    }

    public Task(String title, String category, String time, boolean completed) {
        this.title = title;
        this.category = category;
        this.time = time;
        this.completed = completed;
    }

    public Task(String title, String category, String time, boolean completed, String date) {
        this.title = title;
        this.category = category;
        this.time = time;
        this.completed = completed;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getTime() {
        return time;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getDate() { // ✅ thêm getter này để DocumentsFragment dùng
        return date;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
