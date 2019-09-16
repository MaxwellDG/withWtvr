package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.the_near_me.MapsActivity;
import com.example.myapplication.the_profile.ProfilePage;

public class homePage extends AppCompatActivity {

    public static final String ROOMNAME = "ROOMNAME";
    public static final String TAG = "TAG";

    private String username;
    private Button createRoomButton;
    private Button joinRoomButton;
    private Button needIdeasButton;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

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
                startNearMeMapActivity();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.profileButton:
                Intent intent = new Intent(this, ProfilePage.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
                break;
            case R.id.logoutButton:
                Intent intent2 = new Intent(this, MainActivity.class);
                startActivity(intent2);
                break;
            case R.id.buttonforhelp:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startNearMeMapActivity(){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}


