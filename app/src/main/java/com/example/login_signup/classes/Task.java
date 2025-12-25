package com.example.login_signup.classes;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {

    @Exclude private String id;
    private String uid;
    private String title;
    private String category;
    private String note; 
    private boolean reminder;
    private boolean completed;
    private Date taskDate;
    private String vibration;
    private String ringtone;

    private String priority;

    @Exclude private String time;
    @Exclude private String date;

    public Task() {
    }

    public Task(String id, String title, String category, String time, boolean completed, String date, String note, String priority) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.time = time;
        this.completed = completed;
        this.date = date;
        this.note = note;
        this.priority = priority;
    }

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public boolean isReminder() { return reminder; }
    public void setReminder(boolean reminder) { this.reminder = reminder; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Date getTaskDate() { return taskDate; }
    public void setTaskDate(Date taskDate) { this.taskDate = taskDate; }

    public String getVibration() { return vibration; }
    public void setVibration(String vibration) { this.vibration = vibration; }

    public String getRingtone() { return ringtone; }
    public void setRingtone(String ringtone) { this.ringtone = ringtone; }

    public String getPriority() { return priority != null ? priority : "Basic"; }
    public void setPriority(String priority) { this.priority = priority; }

    @Exclude
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    @Exclude
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
