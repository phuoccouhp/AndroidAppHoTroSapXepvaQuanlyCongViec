package com.example.login_signup.chat;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

// Lớp ChatMessage đại diện cho một tin nhắn trong phiên trò chuyện
public class ChatMessage {
    private String message;
    private String userId;
    private Date timestamp;

    public ChatMessage() {
        
    }

    public ChatMessage(String message, String userId) {
        this.message = message;
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}