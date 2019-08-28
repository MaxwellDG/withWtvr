package com.example.myapplication;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@androidx.room.Entity
public class Message {

    @PrimaryKey
    private int messageID;
    private String User1;
    private String User2;
    private String text;
    private long time;

    public Message(){
    }

    public Message(int messageID, String user1, String user2, String text, long time) {
        this.messageID = messageID;
        User1 = user1;
        User2 = user2;
        this.text = text;
        this.time = time;
    }

    public int getMessageID() {
        return messageID;
    }

    public void setMessageID(int messageID) {
        this.messageID = messageID;
    }

    public String getUser1() {
        return User1;
    }

    public void setUser1(String user1) {
        User1 = user1;
    }

    public String getUser2() {
        return User2;
    }

    public void setUser2(String user2) {
        User2 = user2;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
