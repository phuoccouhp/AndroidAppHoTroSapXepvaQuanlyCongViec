package com.example.login_signup;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Conversation {
    private String id;
    private String lastMessage;
    private Date timestamp;

    public Conversation() {}

    public Conversation(String id, String lastMessage, Date timestamp) {
        this.id = id;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
