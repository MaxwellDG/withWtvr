package com.example.myapplication.rooms_and_voting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.R;

import java.nio.charset.Charset;
import java.util.ArrayList;

public class ActualVoting extends AppCompatActivity {

    private Context context = this;
    private ProgressBar progressBar;
    private BluetoothDataTransferService bluetoothDataTransferService;
    private boolean isHost;

    private ArrayList<String> itemsVoted;
    private VoteCalculations voteCalculations;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothDataTransferService.TheBinder myBinder = (BluetoothDataTransferService.TheBinder) service;
            bluetoothDataTransferService = myBinder.getBluetoothDataTransferService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (isHost) {
                ArrayList<BluetoothDataTransferService.SendingBetweenDevices> allConnections = bluetoothDataTransferService.getAllConnections();
                for (BluetoothDataTransferService.SendingBetweenDevices deviceConnection : allConnections) {
                    deviceConnection.cancelConnection();
                }
            } else {
                bluetoothDataTransferService.getClientSocketConnection().cancelClientSocket();
            }
        }
    };

    @Override
    protected void onStart() {
        // immediately bindingService //
        super.onStart();
        Intent intent = new Intent(this, BluetoothDataTransferService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actual_voting);

        SharedPreferences prefs = getSharedPreferences("PREFS_FILE", MODE_PRIVATE);
        isHost = getIntent().getBooleanExtra("IS_HOST", false);
        int maxVotes = prefs.getInt("MAXVOTES", 3);
        int timerLength = prefs.getInt("TIMERLENGTH", 15);
        progressBar = findViewById(R.id.actualProgress);
        progressBar.setMax(timerLength);
        progressBar.setProgress(timerLength);
        TextView actualInstructions = findViewById(R.id.actualInstructions);
        actualInstructions.setText(getString(R.string.profile_number_votes, String.valueOf(maxVotes)));
        ArrayList<String> allDestinations = getIntent().getStringArrayListExtra("ALL_DESTINATIONS");
        RecyclerView recyclerView = findViewById(R.id.actualRecycler);
        VotingListAdapter votingListAdapter = new VotingListAdapter(allDestinations, this, maxVotes);
        recyclerView.setAdapter(votingListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

        CountDownTimer countDownTimer = new CountDownTimer((timerLength*1000), 1000) {
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
                            // host cycles through testing to see if all votes are submitted, analyzes submissions, then sends out final answer //
                            if (bluetoothDataTransferService.getAreAllAnswersTransferredToHost()) {
                                voteCalculations.addSubmissions(bluetoothDataTransferService.getAllTheSubmissions());
                                voteCalculations.addSubmissions(itemsVoted);
                                String answer = voteCalculations.analyzeDataForFinalAnswer();
                                byte[] answerSend = answer.getBytes(Charset.defaultCharset());
                                bluetoothDataTransferService.write(answerSend);
                                bluetoothDataTransferService.setHasFinalAnswerBeenTransferred();
                            } else {
                                handler.postDelayed(this, 1000);
                            }
                        }
                    };
                    handler.post(runnable);
                } else {
                    // send submissions to host to analyze //
                    bluetoothDataTransferService.write(formatSubmissionsForHost());
                }
                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // the pop-up window with the final answer //
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
            }
        };
        countDownTimer.start();
    }

    public byte[] formatSubmissionsForHost(){
        StringBuilder allSubmissionsTogether = new StringBuilder();
        for (String word : itemsVoted) {
            word = word + "***";
            allSubmissionsTogether.append(word);
        }
        if(itemsVoted.size() > 1) {
            allSubmissionsTogether.delete(allSubmissionsTogether.length() - 3, allSubmissionsTogether.length());
        }
        return allSubmissionsTogether.toString().getBytes(Charset.defaultCharset());
    }

    @Override
    protected void onDestroy() {
        shutDownConnections();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        shutDownConnections();
        NavUtils.navigateUpFromSameTask(this);
        overridePendingTransition(R.anim.trans_in_left, R.anim.trans_out_right);
        finish();
    }

    public void shutDownConnections() {
        if(isHost) {
            ArrayList<BluetoothDataTransferService.SendingBetweenDevices> doop = bluetoothDataTransferService.getAllConnections();
            for (BluetoothDataTransferService.SendingBetweenDevices connection : doop) {
                connection.cancelConnection();
            }
        } else {
            bluetoothDataTransferService.getClientSocketConnection().cancelClientSocket();
        }
        unbindService(serviceConnection);
    }
}
