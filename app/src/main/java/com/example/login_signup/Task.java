package com.example.login_signup;

public class Task {
    private String title;
    private String category;
    private String time;
    private boolean completed;

    public Task(String title, String category, String time, boolean completed) {
        this.title = title;
        this.category = category;
        this.time = time;
        this.completed = completed;
    }

    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getTime() { return time; }
    public boolean isCompleted() { return completed; }
}
