package com.example.myapplication.rooms_and_voting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.homePage;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;



public class VotingLobby extends AppCompatActivity{

    private static final int REQUEST_ENABLE_BT = 1001;
    private static final int REQUEST_ENABLE_BT_DISCOVERY = 1002;
    private static final int REQUEST_ENABLE_DT_CONNECT = 1003;
    private static final int DISCOVERY_DURATION = 300;
    private static final UUID UUID = java.util.UUID.fromString("238c71d5-924d-4f72-af44-89b9e2cc9582");
    public static final String TAG = "TAG";

    private Context context;
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    private ArrayList<BluetoothDevice> deviceDisplayList = new ArrayList<>();
    private BluetoothDevice workingDevice;
    private CardView cardViewProgress;
    private ImageView menu;
    private BluetoothAdapter bluetoothAdapter;

    // To anyone reading this, I went overkill on all of these receivers for ongoing learning purposes. The majority of it doesn't really have a practical function. //
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
                        Log.d(TAG, "onReceive: Puede descubrir y conectar.");
                        Snackbar.make(constraintLayout, "Device is now discoverable and searching. (Press refresh if necessary)", Snackbar.LENGTH_LONG);
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
                        Log.d(TAG, "onReceive: Devices have successfully paired.");
                        Snackbar.make(constraintLayout, "Successfully paired with " + someDevice.getName(), Snackbar.LENGTH_LONG).show();
                        workingDevice = someDevice;
                        bluetoothAdapter.startDiscovery();
                        updateDeviceDisplay();
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.d(TAG, "onReceive: Devices attempting to pair...");
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Snackbar.make(constraintLayout, "Something went wrong...", Snackbar.LENGTH_LONG).show();
                        deviceDisplayList.remove(deviceDisplayList.size() - 1);
                        updateDeviceDisplay();
                        bluetoothAdapter.startDiscovery();
                        Log.d(TAG, "onReceive: Something went wrong");
                        break;
                }
            }
        }
    };

    private ConstraintLayout constraintLayout;
    private DeviceListAdapter deviceListAdapter;
    private DeviceDisplayAdapter deviceDisplayAdapter;
    private RecyclerView allTheDeviceDisplays;
    private Set<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting_lobby);

        constraintLayout = findViewById(R.id.votingConLay);
        context = this;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        cardViewProgress = findViewById(R.id.progressCard);
        menu = findViewById(R.id.lobbyMenu);

        pairedDevices = bluetoothAdapter.getBondedDevices();

        onDeviceClickedListener listener = new onDeviceClickedListener() {
            @Override
            public void touchedDevice(int position) {
                if (!pairedDevices.contains(deviceList.get(position))) {
                    Snackbar.make(constraintLayout, "Connecting to " + deviceList.get(position).getName() + "...", Snackbar.LENGTH_INDEFINITE).show();
                    bluetoothAdapter.cancelDiscovery();
                    deviceList.get(position).createBond();
                } else {
                    Snackbar.make(constraintLayout, "Successfully paired with " + deviceList.get(position).getName(), Snackbar.LENGTH_LONG).show();
                    updateDeviceDisplay();
                }
                workingDevice = deviceList.get(position);
                deviceDisplayList.add(deviceList.get(position));
            }
        };

        deviceListAdapter = new DeviceListAdapter(context, deviceList, listener);
        deviceDisplayAdapter = new DeviceDisplayAdapter(deviceDisplayList, context);

        setUpBluetooth();

        IntentFilter bondingFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(bluetoothBondingReceiver, bondingFilter);

        ImageView refreshButton = findViewById(R.id.hostRefreshList);
        RecyclerView allTheDevices = findViewById(R.id.hostRecyclerAllDevices);
        allTheDeviceDisplays = findViewById(R.id.hostRecylerDevicesConnected);
        allTheDeviceDisplays.setAdapter(deviceDisplayAdapter);
        allTheDeviceDisplays.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        allTheDevices.setAdapter(deviceListAdapter);
        allTheDevices.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        ImageView allowOthers = findViewById(R.id.allowOthersButton);
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

        Button startDestination = findViewById(R.id.startDestinationsButton);
        startDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deviceDisplayList.size() != 0) {
                    Intent intent = new Intent(context, CreateDestinations.class);
                    intent.putParcelableArrayListExtra("CONNECTED_DEVICES", deviceDisplayList);
                    intent.putExtra("IS_HOST", true);
                    // intent.putExtra("ROOMNAME", roomName);
                    startActivity(intent);
                } else {
                    Snackbar.make(constraintLayout, "Must pair with at least 1 device.", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        ImageView createConnection = findViewById(R.id.createConnectionButton);
        createConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (workingDevice == null){
                    Snackbar.make(constraintLayout, "Must first pair with at least 1 device.", Snackbar.LENGTH_LONG).show();
                } else if (deviceDisplayList.contains(workingDevice)) {
                    Snackbar.make(constraintLayout, "You're not doing this connection part here anymore.", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(VotingLobby.this, menu);
                getMenuInflater().inflate(R.menu.lobby_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.lobbyMenuHelp:
                                Log.d(TAG, "onMenuItemClick: poop");
                                break;
                            case R.id.lobbyMenuDeviceName:
                                Log.d(TAG, "onMenuItemClick: "+ bluetoothAdapter.getName());
                                //TODO: you can setName? wtf lol //
                                Log.d(TAG, "onMenuItemClick: " + Build.MODEL);
                                Log.d(TAG, "onMenuItemClick: " + bluetoothAdapter.getAddress());
                                break;
                            case R.id.lobbyMenuExit:
                                onBackPressed();
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

    }


    private void updateDeviceDisplay(){
        allTheDeviceDisplays.setAdapter(deviceDisplayAdapter);
        deviceDisplayAdapter.notifyDataSetChanged();
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

    public void checkPermissionBluetoothDiscover(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ENABLE_DT_CONNECT);
        } else {
            Log.d(TAG, "checkPermissionBluetoothDiscover: Ya aparato esta listo.");
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
                if(resultCode == RESULT_CANCELED) {
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
        if (requestCode == REQUEST_ENABLE_DT_CONNECT) {
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
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
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
