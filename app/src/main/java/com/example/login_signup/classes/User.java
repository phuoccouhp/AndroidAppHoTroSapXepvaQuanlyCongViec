package com.example.login_signup.classes;

// Lớp User đại diện cho thông tin người dùng
public class User {
    private String avatarId;
    private String email;
    private String name;
    private String uid;

    public User() {}

    public User(String avatarId, String email, String name, String uid) {
        this.avatarId = avatarId;
        this.email = email;
        this.name = name;
        this.uid = uid;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid(){
        return uid;
    }

    public void setUid(String uid){
        this.uid = uid;
    }
}
