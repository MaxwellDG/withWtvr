package com.example.myapplication.rooms_and_voting;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;

import java.util.List;

public class VotingLobby extends AppCompatActivity implements TheHandler.AddToListListener {

    private Button addOptionButton;
    private Button startVoteButton;
    private Context context;
    private List<String> finalList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting_lobby);

        String roomName = getIntent().getStringExtra("ROOMNAME");

        // TODO: Maybe... here... now you create the Database, then IMMEDIATELY get it to post your unique ID number, which join room people will put in! //
        // YOU CAN USE THE SAME DATABASE WOOOOO! Just make a new table entity and a new DAO. This is so much easier :D //
        // ^ will have to figure out some foreign key bullshit though //

        final AddOptionThread addOptionThread = new AddOptionThread(this);
        addOptionThread.setName("Add Destinations");
        addOptionThread.start();

        /* TODO: create the database here. Remember to give it a unique ID along with its name.
            Try making a cap of 10 options that all start with null. Then, when someone adds an
            option have a switch case for all 10 that starts with "if != null" ->
            make this a thing. */

        /* TODO: The service here will then regularly in the background be looping through constantly
            to add any new options to the list.
         */
        startTheService();


        // just remember, drunk max, that the below setup is entirely PRACTICE. and not even what youre gonna do at all. AAAAANd AAAND. Remember that you have to be doing this long way because it HAS TO CHANGE THE UI ON OTHER PEOPLES" SCREENS TOO //
        addOptionButton = findViewById(R.id.addOptionButton);
        addOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText inputEdit = findViewById(R.id.addOptionText);
                String input = inputEdit.getText().toString();
                Message message = Message.obtain();
                message.obj = input;
                addOptionThread.handler.sendMessage(message);
            }
        });

        startVoteButton = findViewById(R.id.startVoteButton);
        startVoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVotingActivity();
            }
        });
    }

    public void startTheService(){
        // TODO: The purpose of the service will be to constantly be pulling info from the database to make new views //
        Intent intent = new Intent(this, PollingService.class);
        startService(intent);
    }

    @Override
    public void addToList(final String newDestination) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout linearLayout = findViewById(R.id.linearLayoutOptions);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                TextView newOption = new TextView(getApplicationContext());
                newOption.setText(newDestination);
                newOption.setPadding(0,16,0,0);
                linearLayout.addView(newOption);
                Toast.makeText(getApplicationContext(), "Poll option created.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void startVotingActivity(){
        Intent intent = new Intent(this, ActualVoting.class);
        // get the database to tell you what the unique ID for this specific lobby is and putExtra it //
        startActivity(intent);
    }
}
