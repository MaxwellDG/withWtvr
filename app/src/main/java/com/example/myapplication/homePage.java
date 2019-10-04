package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.the_near_me.MapsActivity;
import com.example.myapplication.the_profile.ProfilePage;

public class homePage extends AppCompatActivity {

    public static final String ROOMNAME = "ROOMNAME";
    public static final String TAG = "TAG";

    private String username;
    private TextView createRoomButton;
    private TextView joinRoomButton;
    private ImageView needIdeasButton;
    private ImageView profileButton;
    private ImageView menuButton;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        username = getIntent().getStringExtra("USERNAME");
        context = this;
        Log.d(TAG, "onCreate: the context for this page is: " + context.toString());

        createRoomButton = findViewById(R.id.createRoomButton);
        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateRoomDialog createRoomDialog = new CreateRoomDialog(context, 1);
                createRoomDialog.show(getSupportFragmentManager(), "Create Room");
            }
        });

        joinRoomButton = findViewById(R.id.joinRoomButton);
        joinRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Application context is: " + getApplicationContext());
                CreateRoomDialog createRoomDialog = new CreateRoomDialog(getApplicationContext(), 2);
                createRoomDialog.show(getSupportFragmentManager(), "Join Room");
            }
        });

        needIdeasButton = findViewById(R.id.needIdeasButton);
        needIdeasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnActivity(MapsActivity.class);
            }
        });

        profileButton = findViewById(R.id.homeProfileButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnActivity(ProfilePage.class);
            }
        });

        ImageView helpButton = findViewById(R.id.homeHelpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: make a dialog here with sub-headers to explain what everything does //
            }
        });
    }

    public void startAnActivity(Class aClass){
        Intent intent = new Intent(this, aClass);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
    }
}


