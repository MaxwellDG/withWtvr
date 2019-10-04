package com.example.myapplication.rooms_and_voting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.homePage;
import com.google.android.material.snackbar.Snackbar;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class VotingLobbyJoiner extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1001;
    private static final int REQUEST_ENABLE_BT_DISCOVERY = 1002;
    private static final int REQUEST_ENABLE_DT_CONNECT = 1003;
    private static final int DISCOVERY_DURATION = 100;
    private static final UUID UUID = java.util.UUID.fromString("238c71d5-924d-4f72-af44-89b9e2cc9582");
    private static final String TAG = "TAG";

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private ConstraintLayout constraintLayout;

    private BluetoothDataTransferService bluetoothDataTransferService;
    private BluetoothDevice workingDevice;
    private DeviceListAdapter deviceListAdapter;
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    private onDeviceClickedListener listener;
    private ImageView joinerStatusImage;
    private boolean blueoothPermission;

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
                Log.d(TAG, "onReceive: " + deviceName + " , " + deviceHardwareAddress);
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
                        Log.d(TAG, "onReceive: Aparatos han hecho amigos!");
                        workingDevice = someDevice;
                        bluetoothAdapter.startDiscovery();
                        joinerStatusImage.setImageResource(R.drawable.baseline_phonelink_erase_black_48dp_green);
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.d(TAG, "onReceive: Aparatos estan haciendo amigos...");
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.d(TAG, "onReceive: una cosa no esta trabajando correctamente");
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting_lobby_joiner);

        String roomName = getIntent().getStringExtra("ROOMNAME");
        TextView theRoomName = findViewById(R.id.joinerRoomName);
        theRoomName.setText(roomName);

        constraintLayout = findViewById(R.id.joinerConLay);
        context = this;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        joinerStatusImage = findViewById(R.id.joinerHostConnectedImage);
        listener = new onDeviceClickedListener() {
            @Override
            public void touchedDevice(int position) {
                bluetoothDataTransferService = new BluetoothDataTransferService(context);
                Snackbar.make(constraintLayout, "Connecting to " + deviceList.get(position).getName() + "...", Snackbar.LENGTH_LONG);
                workingDevice = deviceList.get(position);
                bluetoothAdapter.cancelDiscovery();
                deviceList.get(position).createBond();
            }
        };

        deviceListAdapter = new DeviceListAdapter(context, deviceList, listener);

        setUpBluetooth();

        IntentFilter bondingFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(bluetoothBondingReceiver, bondingFilter);

        ImageView refreshButton = findViewById(R.id.joinerRefreshButton);
        RecyclerView allTheDevices = findViewById(R.id.joinerRecycler);

        Button allowOthers = findViewById(R.id.joinerEnableBluetoothButton);
        allowOthers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionBluetoothDiscover();
                startGettingDiscovered();
                findOtherDevices();
                allTheDevices.setAdapter(deviceListAdapter);
                allTheDevices.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allTheDevices.setAdapter(deviceListAdapter);
                deviceListAdapter.notifyDataSetChanged();
            }
        });

        Button joinerDestinationButton = findViewById(R.id.joinerDestinationsButton);
        joinerDestinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnActivity(CreateDestinations.class);
            }
        });

        Button createConnectionButton = findViewById(R.id.joinerCreateConnection);
        createConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBTConnection(workingDevice, UUID);
            }
        });

        EditText practiceTransfer = findViewById(R.id.joinerTextView);
        Button practiceSend = findViewById(R.id.joinerSend);
        practiceSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bytes = practiceTransfer.getText().toString().getBytes(Charset.defaultCharset());
                bluetoothDataTransferService.write(bytes);
            }
        });
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        // TODO: when youre having more than 1 connection here, will need to change the below to startServiceAsServer and move this to the VotingLobbyJoiner. And remember the name of the below function is wrong //
        bluetoothDataTransferService.startServiceAsClient(device,uuid);
    }

    public void setUpBluetooth(){
        if (bluetoothAdapter == null){
            Snackbar.make(constraintLayout, "Compra un fono nuevo abuelo. Lo necesita bluetooth.", Snackbar.LENGTH_LONG).show();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startAnActivity(homePage.class);
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

    private void startAnActivity(Class aClass) {
        Intent intent = new Intent(VotingLobbyJoiner.this, aClass);
        startActivity(intent);
    }

    private void startGettingDiscovered() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        //TODO: change ths duration below when app goes to production //
        // Also... pretty sure i found a bug? The duration time becomes the resultCode for onActivityResult. Super weird //
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
                            blueoothPermission = false;
                            Toast.makeText(context, "Cannot proceed without bluetooth/location permissions.", Toast.LENGTH_SHORT).show();
                            break;
                        } else {
                            blueoothPermission = true;
                            Toast.makeText(context, "Bluetooth/location permission granted.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        }
    }
}
