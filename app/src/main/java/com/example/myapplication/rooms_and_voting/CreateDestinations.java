package com.example.myapplication.rooms_and_voting;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.myapplication.R;

import java.util.ArrayList;

public class CreateDestinations extends AppCompatActivity {

    public static final String TAG = "TAG";
    private ArrayList<BluetoothDevice> connectedDevicesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_destinations);

        Intent intent = getIntent();
        if (intent != null){
            try {
                connectedDevicesList = intent.getParcelableArrayListExtra("CONNECTED_DEVICES");
                Log.d(TAG, "onCreate: " + connectedDevicesList.toString());
            } catch (NullPointerException npe){
                npe.printStackTrace();
            }
        }


    }
}
