package com.example.myapplication.rooms_and_voting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.UserInfo;
import com.example.myapplication.profile_database.Database;

import java.nio.charset.Charset;
import java.util.ArrayList;

public class CreateDestinations extends AppCompatActivity implements TheHandler.AddToListListener {

    public static final String NOTIFICATION_FOR_JOINERS_WHEN_VOTING = "BRUWEREADY";

    private int settingsDestinations;
    private int settingsVotes;
    private int settingsTimer;
    private String userName;
    private ArrayList<BluetoothDevice> connectedDevicesList;
    private boolean isHost = false;
    private BluetoothDataTransferService bluetoothDataTransferService;
    private AddOptionThread addOptionThread;
    private ArrayList<String> allDestinations = new ArrayList<>();
    private BroadcastReceiver broadcastReceiverIncomingMessages = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Receives messages and notifications from host //
            String incoming = intent.getStringExtra("THE_MESSAGE");
            String incomingSettings = intent.getStringExtra("THE_SETTINGS_AND_NOTIFICATION");
            if (incomingSettings != null) {
                // receiving host's settings //
                incomingSettings = incomingSettings.substring(1, incomingSettings.length()-1);
                String[] settings = incomingSettings.split(", ");
                settingsDestinations = Integer.parseInt(settings[0]);
                settingsVotes = Integer.parseInt(settings[1]);
                settingsTimer = Integer.parseInt(settings[2]);
                changeUIWhenReadyForInput();
            } else if (NOTIFICATION_FOR_JOINERS_WHEN_VOTING.equals(incoming)){
                // receiving prompt to start ActualVoting activity //
                startActivityActualVoting();
                finish();
            } else {
                // receiving a destination option //
                if (!allDestinations.contains(incoming) && !incoming.contains("@@@")) {
                    allDestinations.add(incoming);
                }
                if (allDestinations.size() <= settingsDestinations) {
                    Message message = Message.obtain();
                    message.obj = intent.getStringExtra("THE_MESSAGE");
                    addOptionThread.handler.sendMessage(message);
                } else {
                    allDestinations.remove(intent.getStringExtra("THE_MESSAGE"));
                    Toast.makeText(context, "Host's 'Max Destinations' setting at: " + settingsDestinations, Toast.LENGTH_SHORT).show();
                }
                checkForDeleteDestination(intent.getStringExtra("THE_MESSAGE"));
            }
        }
    };
    private ServiceConnection serviceConnection = new ServiceConnection() {
        // the iBinder to the service //
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothDataTransferService.TheBinder mybinder = (BluetoothDataTransferService.TheBinder) service;
            bluetoothDataTransferService = mybinder.getBluetoothDataTransferService();
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

    private LinearLayout linearLayout;
    private TextView promptText;
    private Button addOptionButton;
    private Context context = this;
    private ProgressBar progressBar;
    private ArrayList<RelativeLayout> allTheViews = new ArrayList<>();
    private ArrayList<ImageView> allTheXs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_destinations);

        linearLayout = findViewById(R.id.destinationLinear);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        promptText = findViewById(R.id.destinationPromptText);
        promptText.setVisibility(View.INVISIBLE);
        progressBar = findViewById(R.id.destinationProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        Button startVotingButton = findViewById(R.id.startVotingButton);

        addOptionThread = new AddOptionThread(this);
        addOptionThread.setName("Add Destinations");
        addOptionThread.start();

        Intent intent = getIntent();
        if (intent != null){
            try {
                connectedDevicesList = intent.getParcelableArrayListExtra("CONNECTED_DEVICES");
                isHost = intent.getBooleanExtra("IS_HOST", false);
                userName = intent.getStringExtra("USERNAME");

            } catch (NullPointerException npe){
                npe.printStackTrace();
            }
        }
        if(!isHost){
            startVotingButton.setEnabled(false);
            startVotingButton.setText(R.string.waiting_for_host);
        }else{
            getSettingsFromDatabase();
        }

        connectToPairedDevices(connectedDevicesList);

        addOptionButton = findViewById(R.id.destinationSubmitButton);
        addOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText inputEdit = findViewById(R.id.destinationEditText);
                if (!inputEdit.getText().toString().isEmpty()) {
                    byte[] bytes = inputEdit.getText().toString().getBytes(Charset.defaultCharset());
                    try {
                        bluetoothDataTransferService.write(bytes);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    if(!allDestinations.contains(inputEdit.getText().toString())) {
                        allDestinations.add(inputEdit.getText().toString());
                        if (allDestinations.size() <= settingsDestinations) {
                            Message message = Message.obtain();
                            message.obj = inputEdit.getText().toString();
                            addOptionThread.handler.sendMessage(message);
                        } else {
                            allDestinations.remove(inputEdit.getText().toString());
                            Toast.makeText(context, "'Max Destinations' setting at: " + settingsDestinations, Toast.LENGTH_SHORT).show();
                        }
                    }
                    inputEdit.setText("");
            }
        }
        });

        IntentFilter intentFilterIncomingMessages = new IntentFilter("INCOMING_MESSAGE");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverIncomingMessages, intentFilterIncomingMessages);

        startVotingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // only host will be able to click this button //
                bluetoothDataTransferService.write(NOTIFICATION_FOR_JOINERS_WHEN_VOTING.getBytes(Charset.defaultCharset()));
                startActivityActualVoting();
                finish();
            }
        });
    }

    public void startActivityActualVoting(){
        Intent intent = new Intent(context, ActualVoting.class);
        ArrayList<String> allDestinationsFinal = removeDuplicates(allDestinations);
        intent.putStringArrayListExtra("ALL_DESTINATIONS", allDestinationsFinal);
        intent.putExtra("IS_HOST", isHost);
        startActivity(intent);
        overridePendingTransition(R.anim.trans_in_right, R.anim.trans_out_left);
    }

    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {
        ArrayList<T> newList = new ArrayList<T>();
        for (T element : list) {
            if (!newList.contains(element)) {
                newList.add(element);
            }
        }
        return newList;
    }

    public void connectToPairedDevices(ArrayList<BluetoothDevice> pairedDevices){
        int deviceConnectionsToBeMade = pairedDevices.size();
        if (isHost) {
            int connectionsMade = 0;
            for (BluetoothDevice device : pairedDevices) {
                Intent intent = new Intent(context, BluetoothDataTransferService.class);
                intent.putExtra("SERVICE_ID", 1);
                intent.putParcelableArrayListExtra("DEVICE_LIST", pairedDevices);
                startService(intent);
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                connectionsMade++;
          }
            if (connectionsMade == deviceConnectionsToBeMade){
                Handler handler = new Handler();
                byte[] completionMessageWhichDoublesAsSettingsInfo = buildStringAndBytesForCompletionAndSettingsMessage();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (bluetoothDataTransferService != null) {
                            if (bluetoothDataTransferService.getAreAllConnectionsEstablished()) {
                                bluetoothDataTransferService.write(completionMessageWhichDoublesAsSettingsInfo);
                                changeUIWhenReadyForInput();
                            } else {
                                handler.postDelayed(this, 3000);
                            }
                        } else {
                            handler.postDelayed(this, 3000);
                        }
                    }
                };
                handler.post(runnable);
            }
        } else {
            Intent intent2 = new Intent(context, BluetoothDataTransferService.class);
            intent2.putExtra("DEVICE_TO_CONNECT", pairedDevices.get(0));
            intent2.putExtra("SERVICE_ID", 2);
            startService(intent2);
            bindService(intent2, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void getSettingsFromDatabase(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Database database = Database.getWithWtvrDatabase(getApplicationContext());
                UserInfo userInfo = database.getDAO_UserInfo().profileUpload(userName);
                settingsDestinations = userInfo.getMaxDestinations();
                settingsVotes = userInfo.getMaxVotes();
                settingsTimer = userInfo.getTimerLength();
            }
        };
        new Thread(runnable).start();
    }

    public void changeUIWhenReadyForInput(){
        progressBar.setVisibility(View.INVISIBLE);
        promptText.setVisibility(View.VISIBLE);
        addOptionButton.setEnabled(true);
    }

    private byte[] buildStringAndBytesForCompletionAndSettingsMessage(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(settingsDestinations).append("!!!").append(settingsVotes)
                .append("!!!").append(settingsTimer);
        return stringBuilder.toString().getBytes(Charset.defaultCharset());
    }



    @Override
    public void addToList(final String newDestination) {
        runOnUiThread(() -> {

            if (!newDestination.contains("@@@")){
                if (!allDestinations.isEmpty()) {
                    promptText.setVisibility(View.GONE);
                } else {
                    promptText.setVisibility(View.VISIBLE);
                }

            RelativeLayout newOptionLayout = new RelativeLayout(context);
            allTheViews.add(newOptionLayout);
            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params1.setMargins(0, 24, 0, 0);
            newOptionLayout.setBackgroundColor(Color.WHITE);
            newOptionLayout.setLayoutParams(params1);

            ImageView imageView = new ImageView(context);
            allTheXs.add(imageView);
            imageView.setImageResource(R.drawable.baseline_close_black_18dp);
            imageView.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params3.addRule(RelativeLayout.CENTER_VERTICAL);
            params3.setMargins(16, 0, 16, 0);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (RelativeLayout layout : allTheViews) {
                        if (layout.indexOfChild(v) != -1) {
                            TextView view = (TextView) layout.getChildAt(0);
                            String text = view.getText().toString();
                            allDestinations.remove(text);
                            String textSend = "@@@" + text;
                            byte[] bytes = textSend.getBytes(Charset.defaultCharset());
                            bluetoothDataTransferService.write(bytes);
                            linearLayout.removeView(layout);

                        }
                    }
                }
            });

            TextView newOptionText = new TextView(context);
            newOptionText.setText(newDestination);
            newOptionText.setTextSize(32f);
            newOptionText.setTextColor(Color.BLACK);
            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params2.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params2.setMargins(16, 0, 16, 0);

            newOptionLayout.addView(newOptionText, params2);
            newOptionLayout.addView(imageView, params3);

            if (isHost) {
                newOptionLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = allTheViews.indexOf(v);
                        for (ImageView image : allTheXs) {
                            if (allTheXs.indexOf(image) == position) {
                                if (image.getVisibility() == View.VISIBLE) {
                                    image.setVisibility(View.GONE);
                                } else {
                                    image.setVisibility(View.VISIBLE);
                                }
                            } else {
                                image.setVisibility(View.GONE);
                            }
                        }
                        for (RelativeLayout layout : allTheViews) {
                            ColorDrawable colorDrawable = (ColorDrawable) layout.getBackground();
                            int color = colorDrawable.getColor();
                            if (allTheViews.indexOf(layout) == position) {
                                if (color == Color.YELLOW) {
                                    layout.setBackgroundColor(Color.WHITE);
                                } else {
                                    layout.setBackgroundColor(Color.rgb(255, 204, 184));
                                }
                            } else {
                                layout.setBackgroundColor(Color.WHITE);
                            }
                        }
                    }
                });
            }
            linearLayout.addView(newOptionLayout);
        }
        });
        }


    public void checkForDeleteDestination(String deleteString){
        // Duplicate check. Removes older version if duplicate present //
        if (deleteString.contains("@@@")) {
            deleteString = deleteString.substring(3);
            allDestinations.remove(deleteString);
        }
        for (RelativeLayout layout : allTheViews) {
            TextView view = (TextView) layout.getChildAt(0);
            String text = view.getText().toString();
            if (text.equals(deleteString)) {
                linearLayout.removeView(layout);
            }
        }
        }

    @Override
    public void onBackPressed() {
        shutDownConnections();
        NavUtils.navigateUpFromSameTask(this);
        overridePendingTransition(R.anim.trans_in_left, R.anim.trans_out_right);
        finish();
    }

    public void shutDownConnections(){
        ArrayList<BluetoothDataTransferService.SendingBetweenDevices> doop = bluetoothDataTransferService.getAllConnections();
        for (BluetoothDataTransferService.SendingBetweenDevices connection : doop){
            connection.cancelConnection();
        }
        unbindService(serviceConnection);
        unregisterReceiver(broadcastReceiverIncomingMessages);
    }


}
