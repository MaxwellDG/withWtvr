package com.example.myapplication.rooms_and_voting;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private final int[] row_index = {-1};

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
        ((DeviceViewHolder) holder).bindView(position, deviceArrayList, listener);
    /*    holder.row_linearlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                row_index[0] = position;
                notifyDataSetChanged();
            }
        });
        if(row_index[0] ==position){
            holder.constraintLayout.setBackgroundColor(Color.parseColor("#567845"));
            holder.tv1.setTextColor(Color.parseColor("#ffffff"));
        }
        else
        {
            holder.row_linearlayout.setBackgroundColor(Color.parseColor("#ffffff"));
            holder.tv1.setTextColor(Color.parseColor("#000000"));
        } */

    }

    @Override
    public int getItemCount() {
        return deviceArrayList.size();
    }
}

    class DeviceViewHolder extends RecyclerView.ViewHolder {

        public TextView deviceName;
        public TextView deviceAddress;
        public ConstraintLayout constraintLayout;

        DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.aDeviceName);
            deviceAddress = itemView.findViewById(R.id.aDeviceAddress);
            constraintLayout = itemView.findViewById(R.id.aDeviceConLay);
        }

        void bindView(int position, ArrayList<BluetoothDevice> deviceArrayList, onDeviceClickedListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    listener.touchedDevice(position);
                }
            });
            if (deviceArrayList.get(position).getName() == null) {
                deviceName.setText("Unknown");
            } else {
                deviceName.setText(deviceArrayList.get(position).getName());
            }
            deviceAddress.setText(deviceArrayList.get(position).getAddress());
        }
    }
