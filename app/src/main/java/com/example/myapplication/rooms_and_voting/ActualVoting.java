package com.example.myapplication.rooms_and_voting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
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

    private ArrayList<String> itemsVoted;
    private ImageView wwLogo;
    private Context context = this;
    private ProgressBar progressBar;
    private BluetoothDataTransferService bluetoothDataTransferService;
    private boolean isBound;
    private boolean isHost;
    private VoteCalculations voteCalculations;
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

        isHost = getIntent().getBooleanExtra("IS_HOST", false);
        TextView actualInstructions = findViewById(R.id.actualInstructions);
        actualInstructions.setText(getString(R.string.profile_number_votes, "3")); // TODO: make this actually grab from userInfo database thing //
        ArrayList<String> allDestinations = getIntent().getStringArrayListExtra("ALL_DESTINATIONS");
        RecyclerView recyclerView = findViewById(R.id.actualRecycler);
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
                voteCalculations = new VoteCalculations();
                if (isHost) {
                    Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if (bluetoothDataTransferService.getAreAllAnswersTransferredToHost()) {
                                voteCalculations.addSubmissions(bluetoothDataTransferService.getAllTheSubmissions());
                                voteCalculations.addSubmissions(itemsVoted);
                                String answer = voteCalculations.analyzeDataForFinalAnswer();
                                Log.d(TAG, "run: Answer is: " + answer);
                                byte[] answerSend = answer.getBytes(Charset.defaultCharset());
                                bluetoothDataTransferService.write(answerSend);
                                bluetoothDataTransferService.setHasFinalAnswerBeenTransferred(true);
                            } else {
                                Log.d(TAG, "run: not ready for Final Dialog YET");
                                handler.postDelayed(this, 1000);
                            }
                        }
                    };
                    handler.post(runnable);
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
                        String theAnswer;
                        if (bluetoothDataTransferService.getHasFinalAnswerBeenTransferred()){
                                if(isHost){
                                    theAnswer = voteCalculations.getFinalAnswer();
                                } else {
                                    theAnswer = bluetoothDataTransferService.getTheFinalAnswer();
                                }
                                FinalAnswerDialog finalAnswerDialog = new FinalAnswerDialog(theAnswer, context);
                                finalAnswerDialog.onCreateDialog(null);
                                finalAnswerDialog.show(getSupportFragmentManager(), null);
                        } else {
                            handler2.postDelayed(this, 1000);
                        }
                    }
                }, 2000);
                progressBar.setVisibility(View.GONE);
                wwLogo.setVisibility(View.VISIBLE);
            }

        };
        countDownTimer.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(context, BluetoothDataTransferService.class);
        stopService(intent);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //TODO: disconnect socket with host //
        NavUtils.navigateUpFromSameTask(this);
    }
}
