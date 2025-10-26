package com.example.login_signup;

public class Task {
    private String id;
    private String title;
    private String category;
    private String time;
    private boolean completed;
    private String date;
    private String note;

    public Task() {}

    public Task(String id, String title, String category, String time,
                boolean completed, String date) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.time = time;
        this.completed = completed;
        this.date = date;
    }

    public Task(String id, String title, String category, String time,
                boolean completed, String date, String note) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.time = time;
        this.completed = completed;
        this.date = date;
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getTime() { return time; }
    public boolean isCompleted() { return completed; }
    public String getDate() { return date; }
}
