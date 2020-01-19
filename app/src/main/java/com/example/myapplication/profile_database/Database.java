package com.example.myapplication.profile_database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.myapplication.Message;
import com.example.myapplication.UserInfo;

@androidx.room.Database(entities = {UserInfo.class, Message.class}, version = 2)

public abstract class Database extends RoomDatabase {

    public abstract DAO_UserInfo getDAO_UserInfo();

    public abstract DAO_Message getDAO_Message();

    private static volatile Database withWtvrDatabase;

    public static Database getWithWtvrDatabase(final Context context){
        if (withWtvrDatabase == null){
            synchronized (Database.class) {
                if (withWtvrDatabase == null) {
                    withWtvrDatabase = Room.databaseBuilder(context.getApplicationContext(), Database.class,
                            "WithWtvrDatabase").build();
                }
            }
        } return withWtvrDatabase;
    }
}
