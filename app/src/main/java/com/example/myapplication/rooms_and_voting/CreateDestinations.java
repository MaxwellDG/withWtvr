package com.example.myapplication.rooms_and_voting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import com.example.myapplication.R;
import com.google.android.material.snackbar.Snackbar;

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
            if (intent.getStringExtra("THE_MESSAGE") != null) {
                allDestinations.add(intent.getStringExtra("THE_MESSAGE"));
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
    private Context context = this;
    private ProgressBar progressBar;
    private int connectionCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_destinations);

        linearLayout = findViewById(R.id.destinationLinear);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        promptText = findViewById(R.id.destinationPromptText);
        progressBar = findViewById(R.id.destinationProgressBar);

        /*String roomName = getIntent().getStringExtra("ROOMNAME");
        TextView theRoomName = findViewById(R.id.destinationsRoomText);
        theRoomName.setText(roomName);
         */

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

        Button addOptionButton = findViewById(R.id.destinationSubmitButton);
        addOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText inputEdit = findViewById(R.id.destinationEditText);
                if (!inputEdit.getText().toString().isEmpty()) {
                    byte[] bytes = inputEdit.getText().toString().getBytes(Charset.defaultCharset());
                    bluetoothDataTransferService.write(bytes);
                    Message message = Message.obtain();
                    message.obj = inputEdit.getText().toString();
                    addOptionThread.handler.sendMessage(message);
                    allDestinations.add(inputEdit.getText().toString());
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
                intent.putStringArrayListExtra("ALL_DESTINATIONS", allDestinations);
                intent.putExtra("IS_HOST", isHost);
                // intent.putExtra("ROOMNAME", roomName);
                // TODO: something that only allows the host to actually do this. Otherwise the joiner's page just hangs with a different colour ready button //
                startActivity(intent);
            }
        });
    }

    public void connectToPairedDevices(ArrayList<BluetoothDevice> pairedDevices){
        int numberOfConnectionsToBeMade = pairedDevices.size();
        if (isHost) {
            Log.d(TAG, "connectToPairedDevices: Youre the host");
            for (BluetoothDevice device : pairedDevices) {
                Intent intent = new Intent(context, BluetoothDataTransferService.class);
                intent.putExtra("SERVICE_ID", 1);
                startService(intent);
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

                connectionCounter++;
                if (connectionCounter == numberOfConnectionsToBeMade){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            promptText.setVisibility(View.VISIBLE);
                        }
                    });
                }
          }
        } else {
            Log.d(TAG, "connectToPairedDevices: Youre the joiner. Matched with" + pairedDevices.get(0));
            Intent intent2 = new Intent(context, BluetoothDataTransferService.class);
            progressBar.setVisibility(View.GONE);
            promptText.setVisibility(View.VISIBLE);

            // TODO: might not need a listener if you just do all of this stuff in the onStart lifecycle method.? Maybe. I don't know.
            //TODO: WE'LL DO THE LISTENER STUFF AFTER THE SERVICE IS ESTABLISHED + the interace class might need to become parcelable/serializable //
            // intent2.putExtra("JOINER_LISTENER", (Parcelable) onConnectionEstablishedListener);
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
            // TODO: Add something in HELP menu that states the rule: Only the host can delete views //
            if (!allDestinations.isEmpty()){
                promptText.setVisibility(View.GONE);
            } else {
                promptText.setVisibility(View.VISIBLE);
            }
            LinearLayout newOptionLayout = new LinearLayout(context);
            newOptionLayout.setBackgroundColor(Color.WHITE);
            newOptionLayout.setPadding(0,8,0,0);
            TextView newOption = new TextView(context);
            newOption.setText(newDestination);
            newOption.setTextSize(20f);
            newOption.setTextColor(Color.BLACK);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            newOptionLayout.addView(newOption, params);
            if (isHost){
                newOptionLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageView newOptionX = new ImageView(context);
                        newOptionX.setImageResource(R.drawable.baseline_close_black_18dp);
                        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params2.gravity = Gravity.RIGHT;
                        ColorDrawable colorDrawable = (ColorDrawable) newOptionLayout.getBackground();
                        int color = colorDrawable.getColor();
                        if (color == Color.YELLOW) {
                            newOptionLayout.setBackgroundColor(Color.WHITE);
                            try {
                                newOptionLayout.removeView(newOptionX);
                            } catch (NullPointerException e){
                                e.printStackTrace();
                            }
                        } else {
                            newOptionLayout.setBackgroundColor(Color.YELLOW);
                            newOptionLayout.addView(newOptionX, params2);
                        }
                        newOptionX.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                linearLayout.removeView(newOptionLayout);
                                // TODO: somehow transfer this removal to all the participants //
                            }
                        });
                    }
                });
            }
            linearLayout.addView(newOptionLayout);
        });
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
}
