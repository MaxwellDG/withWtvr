package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class HelpDialogFragment extends DialogFragment {

    private int requestCode;

    public HelpDialogFragment(int requestCode) {
        this.requestCode = requestCode;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        if(requestCode == 1){
            builder.setView(inflater.inflate(R.layout.dialog_homepage_info, null));
        } else {
            builder.setView(inflater.inflate(R.layout.dialog_voting_lobby, null));
        }

        builder.setTitle("Information:")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        return builder.create();
    }
}
