package com.example.myapplication.rooms_and_voting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.homePage;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class VotingLobbyJoiner extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1001;
    private static final int REQUEST_ENABLE_BT_DISCOVERY = 1002;
    private static final int REQUEST_ENABLE_DT_CONNECT = 1003;
    private static final int DISCOVERY_DURATION = 300;

    private static final String TAG = "TAG";

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private ConstraintLayout constraintLayout;
    private TextView statusText;
    private TextView statusTextTop;

    private BluetoothDataTransferService bluetoothDataTransferService;
    private BluetoothDevice workingDevice;
    private DeviceListAdapter deviceListAdapter;
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    // for joiner activity deviceDisplayList is just a single device. //
    private ArrayList<BluetoothDevice> deviceDisplayList = new ArrayList<>();
    private onDeviceClickedListener listener;
    private ImageView joinerStatusImage;
    private Set<BluetoothDevice> pairedDevices;


    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: recibir una cosa...");
            String action = intent.getAction();
            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                Log.d(TAG, "onReceive: recibio un signal!");
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "onReceive: Prendido");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "onReceive: Prender.");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: Apagado");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "onReceive: Apagar");
                        break;
                }
            }
        }
    };
    private BroadcastReceiver bluetoothDiscoverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                final int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                switch (scanMode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        joinerStatusImage.setImageResource(R.drawable.baseline_phonelink_erase_black_48dp_yellow);
                        statusText.setText(getString(R.string.searching_for));
                        Log.d(TAG, "onReceive: Puede descubrir y conectar.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "onReceive: Puede conectar, pero no descubrir.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "onReceive: No esta trabajando.");
                        break;
                }
            }
        }
    };
    private BroadcastReceiver bluetoothFindOthersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                if (!deviceList.contains(device)) {
                    deviceList.add(device);
                }
            }
        }
    };
    private BroadcastReceiver bluetoothBondingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice someDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                switch (bondState) {
                    case BluetoothDevice.BOND_BONDED:
                        Snackbar.make(constraintLayout, "Successfully paired with " + someDevice.getName(), Snackbar.LENGTH_LONG).show();
                        Log.d(TAG, "onReceive: Aparatos han hecho amigos!");
                        workingDevice = someDevice;
                        bluetoothAdapter.startDiscovery();
                        joinerStatusImage.setImageResource(R.drawable.baseline_phonelink_erase_black_48dp_green);
                        statusTextTop.setText(getString(R.string.paired_with));
                        statusText.setText(someDevice.getName());
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.d(TAG, "onReceive: Aparatos estan haciendo amigos...");
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Snackbar.make(constraintLayout, "Something went wrong...", Snackbar.LENGTH_LONG).show();
                        Log.d(TAG, "onReceive: una cosa no esta trabajando correctamente");
                        bluetoothAdapter.startDiscovery();
                        break;
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting_lobby_joiner);

        /*String roomName = getIntent().getStringExtra("ROOMNAME");
        TextView theRoomName = findViewById(R.id.joinerRoomName);
        theRoomName.setText(roomName);
        */

        constraintLayout = findViewById(R.id.joinerConLay);
        context = this;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = bluetoothAdapter.getBondedDevices();
        joinerStatusImage = findViewById(R.id.joinerHostConnectedImage);
        statusText = findViewById(R.id.joinerStatusText);
        statusTextTop = findViewById(R.id.joinerStatusTextTop);
        listener = new onDeviceClickedListener() {
            @Override
            public void touchedDevice(int position) {
                if (!pairedDevices.contains(deviceList.get(position))){
                    Snackbar.make(constraintLayout, "Connecting to " + deviceList.get(position).getName() + "...", Snackbar.LENGTH_INDEFINITE).show();
                    bluetoothAdapter.cancelDiscovery();
                    deviceList.get(position).createBond();
                } else {
                    Snackbar.make(constraintLayout, "Successfully paired with " + deviceList.get(position).getName(), Snackbar.LENGTH_LONG).show();
                    joinerStatusImage.setImageResource(R.drawable.baseline_phonelink_erase_black_48dp_green);
                    statusTextTop.setText(getString(R.string.paired_with));
                    statusText.setText(deviceList.get(position).getName());
                    deviceDisplayList.add(deviceList.get(position));
                }
                workingDevice = deviceList.get(position);

            }
        };

        deviceListAdapter = new DeviceListAdapter(context, deviceList, listener);

        setUpBluetooth();

        IntentFilter bondingFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(bluetoothBondingReceiver, bondingFilter);

        ImageView refreshButton = findViewById(R.id.joinerRefreshButton);
        RecyclerView allTheDevices = findViewById(R.id.joinerRecycler);

        ImageView allowOthers = findViewById(R.id.joinerEnableBluetoothButton);
        CardView cardViewProgress = findViewById(R.id.joinerProgressCard);
        allTheDevices.setAdapter(deviceListAdapter);
        allTheDevices.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        allowOthers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionBluetoothDiscover();
                startGettingDiscovered();
                findOtherDevices();
                cardViewProgress.setVisibility(View.VISIBLE);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cardViewProgress.setVisibility(View.INVISIBLE);
                        deviceListAdapter.notifyDataSetChanged();
                    }
                },5000);
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceListAdapter.notifyDataSetChanged();
            }
        });

        Button joinerDestinationButton = findViewById(R.id.joinerDestinationsButton);
        joinerDestinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CreateDestinations.class);
                // intent.putExtra("ROOMNAME", roomName);
                intent.putExtra("IS_HOST", false);
                intent.putParcelableArrayListExtra("CONNECTED_DEVICES", deviceDisplayList);
                startActivity(intent);
            }
        });
    }

    public void setUpBluetooth(){
        if (bluetoothAdapter == null){
            Snackbar.make(constraintLayout, "Buy a new phone grandpa. It needs bluetooth.", Snackbar.LENGTH_LONG).show();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(context, homePage.class);
                    startActivity(intent);
                }
            }, 3000);
        } else {
            if (bluetoothAdapter.isEnabled()){
                IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(bluetoothReceiver, intentFilter);
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void startGettingDiscovered() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERY_DURATION);
        startActivityForResult(intent, REQUEST_ENABLE_BT_DISCOVERY);

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(bluetoothDiscoverReceiver, intentFilter);
    }

    public void findOtherDevices(){
        if (bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            bluetoothAdapter.startDiscovery();
        } else {
            bluetoothAdapter.startDiscovery();
        }
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothFindOthersReceiver, intentFilter);
    }

    public void checkPermissionBluetoothDiscover(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission
                    (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ENABLE_DT_CONNECT);
            } else {
                Log.d(TAG, "checkPermissionBluetoothDiscover: Ya aparato esta listo.");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK){
                    IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(bluetoothReceiver, intentFilter);
                    Toast.makeText(context, "Bluetooth has been enabled.", Toast.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(constraintLayout, "You need to permit bluetooth to continue.", Snackbar.LENGTH_LONG).show();
                }
                break;
            case REQUEST_ENABLE_BT_DISCOVERY:
                // pretty sure the resultCode here is a wonky bug. It's referring to the set duration //
                if (resultCode == DISCOVERY_DURATION){
                    // Toast.makeText(context, "Bluetooth has enabled discovery.", Toast.LENGTH_SHORT).show();
                } else if(resultCode == RESULT_CANCELED) {
                    Snackbar.make(constraintLayout, "Your device is not discoverable.", Snackbar.LENGTH_LONG).show();
                } else{
                    Snackbar.make(constraintLayout, "WOah....", Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ENABLE_DT_CONNECT:
                if (grantResults.length > 0) {
                    for (int i : grantResults) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(context, "Cannot proceed without bluetooth/location permissions.", Toast.LENGTH_SHORT).show();
                            break;
                        } else {
                            Toast.makeText(context, "Bluetooth/location permission granted.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        }
    }

    @Override
    protected void onDestroy() {
        BroadcastReceiver[] allTheReceivers = {bluetoothReceiver, bluetoothDiscoverReceiver, bluetoothFindOthersReceiver, bluetoothBondingReceiver};
        for (BroadcastReceiver receiver : allTheReceivers){
            try{
                unregisterReceiver(receiver);
            } catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
