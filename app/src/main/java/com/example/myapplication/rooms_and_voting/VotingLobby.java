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
import java.util.List;
import java.util.UUID;


public class VotingLobby extends AppCompatActivity{

    private static final int REQUEST_ENABLE_BT = 1001;
    private static final int REQUEST_ENABLE_BT_DISCOVERY = 1002;
    private static final int REQUEST_ENABLE_DT_CONNECT = 1003;
    private static final int DISCOVERY_DURATION = 100;
    private static final UUID UUID = java.util.UUID.fromString("238c71d5-924d-4f72-af44-89b9e2cc9582");
    public static final String TAG = "TAG";

    private Context context;
    private boolean blueoothPermission;
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    private ArrayList<BluetoothDevice> deviceDisplayList = new ArrayList<>();
    private BluetoothDevice workingDevice;
    private BluetoothDataTransferService bluetoothDataTransferService;
    // will be used for a transition to the next page //
    private List<String> finalList;

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
                        updateDeviceDisplay();
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        // TODO: below line is just for testing without a 2nd phone purposes //
                        updateDeviceDisplay();
                        Log.d(TAG, "onReceive: Aparatos estan haciendo amigos...");
                        break;
                    case BluetoothDevice.BOND_NONE:
                        deviceDisplayList.remove(deviceDisplayList.size() - 1);
                        updateDeviceDisplay();
                        Log.d(TAG, "onReceive: una cosa no esta trabajando correctamente");
                        break;
                }
            }
        }
    };

    private ConstraintLayout constraintLayout;
    private onDeviceClickedListener listener;
    private DeviceListAdapter deviceListAdapter;
    private DeviceDisplayAdapter deviceDisplayAdapter;
    private RecyclerView alltheDeviceDisplays;









    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting_lobby);

        String roomName = getIntent().getStringExtra("ROOMNAME");
        TextView theRoomName = findViewById(R.id.roomName);
        theRoomName.setText(roomName);

        constraintLayout = findViewById(R.id.votingConLay);
        context = this;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        listener = new onDeviceClickedListener() {
            @Override
            public void touchedDevice(int position) {
                bluetoothDataTransferService = new BluetoothDataTransferService(context);
                // TODO: If already paired, still add this to the list, and start the whole serviceConnection process as well. //
                Snackbar.make(constraintLayout, "Connecting to " + deviceList.get(position).getName() + "...", Snackbar.LENGTH_LONG);
                // TODO: make it so it only cancels discovery if the devices are NOT already paired. If they're paired, no need to waste time turning this off and on again //
                bluetoothAdapter.cancelDiscovery();
                deviceList.get(position).createBond();
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
        alltheDeviceDisplays = findViewById(R.id.hostRecylerDevicesConnected);
        alltheDeviceDisplays.setAdapter(deviceDisplayAdapter);
        alltheDeviceDisplays.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        // TODO: all of this stuff below could be made UI/UX friendly by just putting it through a service. You wouldn't even need the refresh button if it was constantly updating //

        Button allowOthers = findViewById(R.id.allowOthersButton);
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

        Button startDestination = findViewById(R.id.startDestinationsButton);
        startDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CreateDestinations.class);
                intent.putParcelableArrayListExtra("CONNECTED_DEVICES", deviceDisplayList);
                startAnActivity(intent);
            }
        });

        Button createConnection = findViewById(R.id.createConnectionButton);
        createConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBTConnection(workingDevice, UUID);
            }
        });

        EditText practiceTransfer = findViewById(R.id.workingTextView);
        Button practiceSend = findViewById(R.id.practiceSend);
        practiceSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bytes = practiceTransfer.getText().toString().getBytes(Charset.defaultCharset());
                bluetoothDataTransferService.write(bytes);
            }
        });

        /* TODO: The service here will then regularly in the background be looping through constantly
            to add any new options to the list. All this stuff below can be re-used on the actual
            posting of destinations on the next page.
         */

        /* final AddOptionThread addOptionThread = new AddOptionThread(this);
        addOptionThread.setName("Add Destinations");
        addOptionThread.start();

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
                startAnActivity(ActualVoting.class);
            }
        });
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
    } */
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        // TODO: when youre having more than 1 connection here, will need to change the below to startServiceAsServer and move this to the VotingLobbyJoiner //
        bluetoothDataTransferService.startServiceAsClient(device,uuid);
    }

    private void updateDeviceDisplay(){
        alltheDeviceDisplays.setAdapter(deviceDisplayAdapter);
        deviceDisplayAdapter.notifyDataSetChanged();
    }

    private void startAnActivity(Intent intent) {
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

    public void setUpBluetooth(){
        if (bluetoothAdapter == null){
            Snackbar.make(constraintLayout, "Compra un fono nuevo abuelo. Lo necesita bluetooth.", Snackbar.LENGTH_LONG).show();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(context, homePage.class);
            startAnActivity(intent);
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

    @Override
    protected void onDestroy() {
        unregisterReceiver(bluetoothReceiver);
        unregisterReceiver(bluetoothDiscoverReceiver);
        unregisterReceiver(bluetoothFindOthersReceiver);
        unregisterReceiver(bluetoothBondingReceiver);
        super.onDestroy();
    }
}
