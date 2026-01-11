package com.example.login_signup.history;

import java.util.Date;

// Lớp TaskLog đại diện cho một bản ghi nhật ký của một công việc
public class TaskLog {
    private String logId;
    private String userId;
    private String taskId;
    private String taskTitle;
    private String action;
    private Date timestamp;

    public TaskLog() { }

    public TaskLog(String userId, String taskId, String taskTitle, String action, Date timestamp) {
        this.userId = userId;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.action = action;
        this.timestamp = timestamp;
    }

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}