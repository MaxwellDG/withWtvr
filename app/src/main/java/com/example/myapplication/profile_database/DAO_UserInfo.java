package com.example.myapplication.profile_database;

import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication.UserInfo;

import java.util.List;

@androidx.room.Dao
public interface DAO_UserInfo {

    // App functionality //

    @Insert
    void insertUserInfo(UserInfo userInfo);

    @Query("SELECT * FROM UserInfo WHERE username=:userInput AND password=:passInput")
        UserInfo loginAttempt(String userInput, String passInput);

    @Query("SELECT * FROM UserInfo WHERE username=:userInput")
        UserInfo profileUpload(String userInput);

    // Getting data //

    @Query("SELECT * FROM UserInfo")
        List<UserInfo> getThemAll();

    @Query("SELECT * FROM UserInfo WHERE avatarId=:number")
        List<UserInfo> getAvatarNumbers(int number);

    // Update queries //

    @Query("UPDATE UserInfo SET GPS=:enabled WHERE username=:name")
        void updateGPS(boolean enabled, String name);

    @Query("UPDATE UserInfo SET avatarId=:avatar WHERE username=:name")
        void updateAvatar(int avatar, String name);

    @Query("UPDATE UserInfo SET password=:pass WHERE username=:name")
        void updatePassword(String pass, String name);

    @Query("UPDATE UserInfo SET email=:emailInput WHERE username=:name")
        void updateEmail(String emailInput, String name);

    @Query("UPDATE UserInfo SET maxDestinations=:maxDests WHERE username=:name")
    void updateMaxDestinations(String maxDests, String name);

    @Query("UPDATE UserInfo SET maxVotes=:maxNoVotes WHERE username=:name")
    void updateMaxVotes(String maxNoVotes, String name);

    @Query("UPDATE UserInfo SET timerLength=:time WHERE username=:name")
    void updateTimerLength(String time, String name);
}
