package com.example.login_signup;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class ChatSession {
    private String id;
    private String name;
    private String lastMessage;
    private Date lastUpdated;
    private String userId; // To know which user this session belongs to

    public ChatSession() {
        // Constructor rỗng cần thiết cho Firestore
    }

    public ChatSession(String name, String userId) {
        this.name = name;
        this.userId = userId;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    @ServerTimestamp
    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}