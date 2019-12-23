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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.myapplication.R;

import java.nio.charset.Charset;
import java.util.ArrayList;

public class CreateDestinations extends AppCompatActivity implements TheHandler.AddToListListener {

    public static final String TAG = "TAG";
    private ArrayList<BluetoothDevice> connectedDevicesList;
    private boolean isHost;
    // TODO: might need a bunch of these if there's a bunch of connections //
    private static final String UUIDCLIENT = "238c71d5-924d-4f72-af44-89b9e2cc9582";
    private BluetoothDataTransferService bluetoothDataTransferService;
    private AddOptionThread addOptionThread;
    private ArrayList<String> allDestinations = new ArrayList<>();
    private BroadcastReceiver broadcastReceiverIncomingMessages = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ROGER????");
            String incoming = intent.getStringExtra("THE_MESSAGE");
            if ("BRUYOUREREADY".equals(incoming)) {
                Log.d(TAG, "onReceive: ROGER ROGER");
                changeUIWhenReadyForInput();
            } else {
                if (!allDestinations.contains(incoming) && !incoming.contains("@@@")) {
                    allDestinations.add(incoming);
                }
                Message message = Message.obtain();
                message.obj = intent.getStringExtra("THE_MESSAGE");
                addOptionThread.handler.sendMessage(message);
            }
        }
    };
    private boolean isBound = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothDataTransferService.TheBinder mybinder = (BluetoothDataTransferService.TheBinder) service;
            bluetoothDataTransferService = mybinder.getBluetoothDataTransferService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: DISCONNECTED!");
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
        progressBar = findViewById(R.id.destinationProgressBar);

        addOptionThread = new AddOptionThread(this);
        addOptionThread.setName("Add Destinations");
        addOptionThread.start();



        Intent intent = getIntent();
        if (intent != null){
            try {
                connectedDevicesList = intent.getParcelableArrayListExtra("CONNECTED_DEVICES");
                isHost = intent.getBooleanExtra("IS_HOST", false);
                Log.d(TAG, "onCreate: " + connectedDevicesList.toString());
            } catch (NullPointerException npe){
                npe.printStackTrace();
            }
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
                        Log.d(TAG, "onClick: Just practicing");
                    }
                    Message message = Message.obtain();
                    message.obj = inputEdit.getText().toString();
                    addOptionThread.handler.sendMessage(message);
                    if(!allDestinations.contains(inputEdit.getText().toString())) {
                        allDestinations.add(inputEdit.getText().toString());
                    }
                    inputEdit.setText("");
            }
        }
        });

        IntentFilter intentFilterIncomingMessages = new IntentFilter("INCOMING_MESSAGE");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverIncomingMessages, intentFilterIncomingMessages);

        Button startVotingButton = findViewById(R.id.startVotingButton);
        startVotingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ActualVoting.class);
                ArrayList<String> allDestinationsFinal = removeDuplicates(allDestinations);
                intent.putStringArrayListExtra("ALL_DESTINATIONS", allDestinationsFinal);
                intent.putExtra("IS_HOST", isHost);
                // TODO: something that only allows the host to actually do this. Otherwise the joiner's page just hangs with a different colour ready button //
                startActivity(intent);
            }
        });
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
            Log.d(TAG, "connectToPairedDevices: Youre the host");
            int connectionsMade = 0;
            for (BluetoothDevice device : pairedDevices) {
                // TODO: this will have to be changed when you have more than 1 device connected //
                Intent intent = new Intent(context, BluetoothDataTransferService.class);
                intent.putExtra("SERVICE_ID", 1);
                startService(intent);
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                connectionsMade++;
          }
            if (connectionsMade == deviceConnectionsToBeMade){
                Log.d(TAG, "connectToPairedDevices: We're atleast here.");
                Handler handler = new Handler();
                byte[] completionMessage = "BRUYOUREREADY".getBytes(Charset.defaultCharset());
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (bluetoothDataTransferService != null) {
                            if (bluetoothDataTransferService.getIsStreamConnected()) {
                                bluetoothDataTransferService.write(completionMessage);
                                changeUIWhenReadyForInput();
                                Log.d(TAG, "connectToPairedDevices: ROGER ROGER HOST");
                            } else {
                                Log.d(TAG, "run: WAS NULL TRYING AGAIN.");
                                handler.postDelayed(this, 1000);
                            }
                        } else {
                            handler.postDelayed(this, 1000);
                        }
                    }
                };
                handler.post(runnable);
            }
        } else {
            Log.d(TAG, "connectToPairedDevices: Youre the joiner. Matching with" + pairedDevices.get(0));
            Intent intent2 = new Intent(context, BluetoothDataTransferService.class);
            intent2.putExtra("DEVICE_TO_CONNECT", pairedDevices.get(0));
            intent2.putExtra("UUID", UUIDCLIENT);
            intent2.putExtra("SERVICE_ID", 2);
            startService(intent2);
            bindService(intent2, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void addToList(final String newDestination) {
        runOnUiThread(() -> {

            checkForDeleteDestination(newDestination);

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
                            Log.d(TAG, "onClick: REMOVED: " + text + "For host, allDestinations is now: " + allDestinations);
                            String textSend = "@@@" + text;
                            Log.d(TAG, "SENDING FOR DELETE: " + textSend);
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
                        Log.d(TAG, "onClick: position is: " + position);
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
        if (deleteString.contains("@@@")) {
            deleteString = deleteString.substring(3);
            allDestinations.remove(deleteString);
            Log.d(TAG, "DELETING: " + deleteString);
        }
        for (RelativeLayout layout : allTheViews) {
            TextView view = (TextView) layout.getChildAt(0);
            String text = view.getText().toString();
            if (text.equals(deleteString)) {
                linearLayout.removeView(layout);
            }
        }
        }

    public void changeUIWhenReadyForInput(){
        progressBar.setVisibility(View.INVISIBLE);
        promptText.setVisibility(View.VISIBLE);
        addOptionButton.setEnabled(true);
    }


    @Override
    protected void onDestroy() {
        try{
            unregisterReceiver(broadcastReceiverIncomingMessages);
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
