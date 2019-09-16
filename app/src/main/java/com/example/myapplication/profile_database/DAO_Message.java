package com.example.myapplication.profile_database;

import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication.Message;

@androidx.room.Dao
public interface DAO_Message {

    @Insert
        void insertMessage(Message message);

    @Query("DELETE from Message WHERE messageID=:number")
        void deleteMessage(int number);
}
