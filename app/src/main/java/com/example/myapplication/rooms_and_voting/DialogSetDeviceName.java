package com.example.myapplication.rooms_and_voting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.R;

public class DialogSetDeviceName extends DialogFragment {

    private Context context;
    private BluetoothAdapter adapter;
    private TextView deviceNameDisplay;

    public DialogSetDeviceName(Context context, BluetoothAdapter adapter, TextView textView) {
        this.context = context;
        this.adapter = adapter;
        this.deviceNameDisplay = textView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_set_device_name, null))
            .setMessage("Set Device Nickname:").setPositiveButton("Set name", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editText = getDialog().findViewById(R.id.deviceNewName);
                if(!editText.getText().toString().isEmpty()) {
                    adapter.setName(editText.getText().toString());
                    deviceNameDisplay.setText(editText.getText().toString());
                } else {
                    dismiss();
                }
            }
        }).setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        return builder.create();
    }
}
