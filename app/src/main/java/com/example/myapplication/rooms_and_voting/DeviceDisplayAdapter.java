package com.example.myapplication.rooms_and_voting;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;

public class DeviceDisplayAdapter extends RecyclerView.Adapter {

    private ArrayList<BluetoothDevice> deviceDisplayArrayList;
    private Context context;
    // some onTouch listener for devices //


    public DeviceDisplayAdapter(ArrayList<BluetoothDevice> deviceDisplayArrayList, Context context) {
        this.deviceDisplayArrayList = deviceDisplayArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adevice_display_segment, parent, false);
        return new DisplayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((DisplayViewHolder) holder).onBind(position, deviceDisplayArrayList);
    }

    @Override
    public int getItemCount() {
        return deviceDisplayArrayList.size();
    }
}

    class DisplayViewHolder extends RecyclerView.ViewHolder {

        private TextView deviceName;
        private ImageView deviceImageForRemoval;

        DisplayViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.deviceDisplayName);
            deviceImageForRemoval = itemView.findViewById(R.id.deviceDisplayImage);
        }

        void onBind(int position, ArrayList<BluetoothDevice> connectedDevicesArrayList) {
            if (connectedDevicesArrayList.get(position).getName() != null) {
                deviceName.setText(connectedDevicesArrayList.get(position).getName());
            } else {
                deviceName.setText("Unknown");
            }
        }
    }
