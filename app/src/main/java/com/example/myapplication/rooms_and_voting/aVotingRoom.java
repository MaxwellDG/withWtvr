package com.example.myapplication.rooms_and_voting;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class aVotingRoom {

    @PrimaryKey(autoGenerate = true)
    private int uniqueID;
    // maybe combine the two tables? line below idea //
    private String roomCreator;
    private String roomName;
    private String roomPass;
    private String roomNo1;
    private String roomNo2;
    private String roomNo3;
    private String roomNo4;
    private String roomNo5;
    private String roomNo6;
    private String roomNo7;
    private String roomNo8;
    private String roomNo9;
    private String roomNo10;

    public aVotingRoom(int uniqueID, String roomName, String roomPass, String roomNo1, String roomNo2, String roomNo3, String roomNo4, String roomNo5, String roomNo6, String roomNo7, String roomNo8, String roomNo9, String roomNo10) {
        this.uniqueID = uniqueID;
        this.roomName = roomName;
        this.roomPass = roomPass;
        this.roomNo1 = null;
        this.roomNo2 = null;
        this.roomNo3 = null;
        this.roomNo4 = null;
        this.roomNo5 = null;
        this.roomNo6 = null;
        this.roomNo7 = null;
        this.roomNo8 = null;
        this.roomNo9 = null;
        this.roomNo10 = null;
    }

    public String getRoomCreator() {
        return roomCreator;
    }

    public void setRoomCreator(String roomCreator) {
        this.roomCreator = roomCreator;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(int uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomPass() {
        return roomPass;
    }

    public void setRoomPass(String roomPass) {
        this.roomPass = roomPass;
    }

    public String getRoomNo1() {
        return roomNo1;
    }

    public void setRoomNo1(String roomNo1) {
        this.roomNo1 = roomNo1;
    }

    public String getRoomNo2() {
        return roomNo2;
    }

    public void setRoomNo2(String roomNo2) {
        this.roomNo2 = roomNo2;
    }

    public String getRoomNo3() {
        return roomNo3;
    }

    public void setRoomNo3(String roomNo3) {
        this.roomNo3 = roomNo3;
    }

    public String getRoomNo4() {
        return roomNo4;
    }

    public void setRoomNo4(String roomNo4) {
        this.roomNo4 = roomNo4;
    }

    public String getRoomNo5() {
        return roomNo5;
    }

    public void setRoomNo5(String roomNo5) {
        this.roomNo5 = roomNo5;
    }

    public String getRoomNo6() {
        return roomNo6;
    }

    public void setRoomNo6(String roomNo6) {
        this.roomNo6 = roomNo6;
    }

    public String getRoomNo7() {
        return roomNo7;
    }

    public void setRoomNo7(String roomNo7) {
        this.roomNo7 = roomNo7;
    }

    public String getRoomNo8() {
        return roomNo8;
    }

    public void setRoomNo8(String roomNo8) {
        this.roomNo8 = roomNo8;
    }

    public String getRoomNo9() {
        return roomNo9;
    }

    public void setRoomNo9(String roomNo9) {
        this.roomNo9 = roomNo9;
    }

    public String getRoomNo10() {
        return roomNo10;
    }

    public void setRoomNo10(String roomNo10) {
        this.roomNo10 = roomNo10;
    }
}
