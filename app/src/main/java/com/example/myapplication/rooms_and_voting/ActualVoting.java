package com.example.myapplication.rooms_and_voting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.R;

import java.nio.charset.Charset;
import java.util.ArrayList;

public class ActualVoting extends AppCompatActivity {

    public static final String TAG = "TAG";

    private ArrayList<String> allDestinations;
    private ArrayList<String> itemsVoted;
    private ImageView wwLogo;
    private RecyclerView recyclerView;
    private Context context = this;
    private ProgressBar progressBar;
    private BluetoothDataTransferService bluetoothDataTransferService;
    private boolean isBound;
    private boolean isHost;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothDataTransferService.TheBinder myBinder = (BluetoothDataTransferService.TheBinder) service;
            bluetoothDataTransferService = myBinder.getBluetoothDataTransferService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BluetoothDataTransferService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actual_voting);

        /*String roomName = getIntent().getStringExtra("ROOMNAME");
        TextView theRoomName = findViewById(R.id.actualRoomName);
        theRoomName.setText(roomName);
         */

        isHost = getIntent().getBooleanExtra("IS_HOST", false);
        TextView actualInstructions = findViewById(R.id.actualInstructions);
        actualInstructions.setText(getString(R.string.profile_number_votes, "3")); // TODO: make this actually grab from userInfo database thing //
        allDestinations = getIntent().getStringArrayListExtra("ALL_DESTINATIONS");
        Log.d(TAG, "Sent: " + getIntent().getStringArrayListExtra("ALL_DESTINATIONS"));
        recyclerView = findViewById(R.id.actualRecycler);
        VotingListAdapter votingListAdapter = new VotingListAdapter(allDestinations, this);
        recyclerView.setAdapter(votingListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

        progressBar = findViewById(R.id.actualProgress);
        wwLogo = findViewById(R.id.actualLogo);
        CountDownTimer countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                progressBar.setProgress(progressBar.getProgress() - 1);
            }

            @Override
            public void onFinish() {
                itemsVoted = votingListAdapter.getItemsVoted();
                final StringBuilder alertAnswer = new StringBuilder();
                if (isHost) {
                    bluetoothDataTransferService.addHostsAnswers(itemsVoted);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bluetoothDataTransferService.analyzeDataForFinalAnswer();
                        }
                    }, 3000);
                } else {
                    StringBuilder tripleWord = new StringBuilder();
                    for (String word : itemsVoted) {
                        word = word + "***";
                        tripleWord.append(word);
                    }
                    tripleWord.delete(tripleWord.length() - 3, tripleWord.length());
                    byte[] bytes = tripleWord.toString().getBytes(Charset.defaultCharset());
                    bluetoothDataTransferService.write(bytes);
                }
                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FinalAnswerDialog finalAnswerDialog = new FinalAnswerDialog(bluetoothDataTransferService.getTheFINALANSWER(), context);
                        finalAnswerDialog.onCreateDialog(null);
                        finalAnswerDialog.show(getSupportFragmentManager(), null);
                    }
                }, 5000);
                progressBar.setVisibility(View.GONE);
                wwLogo.setVisibility(View.VISIBLE);
            }

        };
        countDownTimer.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // TODO: add a check to see if it's bound. Not working well for some reason :S //
        //TODO: maybe having the unbind as well as the stopservice is causing the weird problem on close? //
        unbindService(serviceConnection);
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(context, BluetoothDataTransferService.class);
        stopService(intent);
        super.onDestroy();
    }
}
