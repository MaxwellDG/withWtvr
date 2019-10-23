package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@androidx.room.Entity
public class UserInfo {

    @NonNull
    @PrimaryKey
    private String username;
    private String password;
    private String email;
    private int avatarId;
    private boolean GPS;


    public UserInfo(@NonNull String username, String password, String email, int avatarId, boolean GPS) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.avatarId = avatarId;
        this.GPS = GPS;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(int avatarId) {
        this.avatarId = avatarId;
    }

    public boolean getGPS() {
        return GPS;
    }

    public void setGPS(boolean GPS) {
        this.GPS = GPS;
    }
}
