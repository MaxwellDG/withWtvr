package com.example.myapplication.rooms_and_voting;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.myapplication.R;

public class ActualVoting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actual_voting);

        // this might actually not need a service.... Can just add the votes to database and then at the end just run a standard separate thread that will grab the most popular option. Then, obv, delete the entire database //

    }
}
