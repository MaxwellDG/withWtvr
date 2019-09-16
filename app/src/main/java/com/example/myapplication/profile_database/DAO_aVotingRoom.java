package com.example.myapplication.profile_database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication.rooms_and_voting.aVotingRoom;

@Dao
public interface DAO_aVotingRoom {

    @Insert
        void insertaVotingRoom(aVotingRoom aVotingRoom);

    @Query("SELECT * FROM aVotingRoom WHERE roomCreator=:creator")
        void getaVotingRoom(String creator);


}
