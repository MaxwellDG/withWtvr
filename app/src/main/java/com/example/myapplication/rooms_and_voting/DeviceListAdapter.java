package com.example.myapplication.rooms_and_voting;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;

public class DeviceListAdapter extends RecyclerView.Adapter{

    private Context context;
    private ArrayList<BluetoothDevice> deviceArrayList;
    private final onDeviceClickedListener listener;
    private static final String TAG = "TAG";
    private int row_index = -1;

    DeviceListAdapter(Context context, ArrayList<BluetoothDevice> deviceArrayList, onDeviceClickedListener listener) {
        this.listener = listener;
        this.context = context;
        this.deviceArrayList = deviceArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adevice_segment, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (deviceArrayList.get(position).getName() == null) {
            ((DeviceViewHolder) holder).deviceName.setText("Unknown");
        } else {
            ((DeviceViewHolder) holder).deviceName.setText(deviceArrayList.get(position).getName());
        }
        ((DeviceViewHolder) holder).deviceAddress.setText(deviceArrayList.get(position).getAddress());

      ((DeviceViewHolder) holder).constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((DeviceViewHolder) holder).attemptPairingButton.setVisibility(View.VISIBLE);
                row_index = position;
                notifyDataSetChanged();
            }
        });
        if(row_index == position){
            ((DeviceViewHolder) holder).attemptPairingButton.setVisibility(View.VISIBLE);
            ((DeviceViewHolder) holder).attemptPairingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.touchedDevice(position);
                }
            });
            ((DeviceViewHolder) holder).constraintLayout.setBackgroundColor(Color.parseColor("#FFCCB826"));
            ((DeviceViewHolder) holder).deviceName.setTextColor(Color.parseColor("#ffffff"));
        }
        else {
            ((DeviceViewHolder) holder).attemptPairingButton.setVisibility(View.GONE);
            ((DeviceViewHolder) holder).constraintLayout.setBackgroundColor(Color.parseColor("#ffffff"));
            ((DeviceViewHolder) holder).deviceName.setTextColor(Color.parseColor("#000000"));
        }
    }

    @Override
    public int getItemCount() {
        return deviceArrayList.size();
    }
}

    class DeviceViewHolder extends RecyclerView.ViewHolder {

        TextView deviceName;
        TextView deviceAddress;
        ConstraintLayout constraintLayout;
        Button attemptPairingButton;

        DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.aDeviceName);
            deviceAddress = itemView.findViewById(R.id.aDeviceAddress);
            constraintLayout = itemView.findViewById(R.id.aDeviceConLay);
            attemptPairingButton = itemView.findViewById(R.id.aDevicePairButton);
        }
    }
