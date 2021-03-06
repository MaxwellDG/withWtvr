package com.example.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.rooms_and_voting.VotingLobby;
import com.example.myapplication.rooms_and_voting.VotingLobbyJoiner;

public class CreateRoomDialog extends DialogFragment {



    // this class isn't used but might be incorporated later when lobbys will have their own identifiers //




    public static final String ROOMNAME = "ROOMNAME";
    public static final String ROOMPASS = "ROOMPASS";

    private Context context;
    private int fieldCode;

    public CreateRoomDialog(Context context, int fieldCode) {
        this.fieldCode = fieldCode;
        this.context = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        final AlertDialog.Builder theDialog = new AlertDialog.Builder(getActivity())
                .setMessage("Complete the fields below: ").setView(inflater.inflate(R.layout.dialog_homepage_room_creation, null))
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            EditText editRoom = getDialog().findViewById(R.id.deviceNewName);
                            String roomName = editRoom.getText().toString();
                            if (roomName.isEmpty()){
                                roomName = "WithWtvr";
                            }
                            EditText editPass = getDialog().findViewById(R.id.dialogRoomFirstField);
                            String roomPass = editRoom.getText().toString();
                            startALobby(roomName, roomPass);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setIcon(R.drawable.ic_account_box_black_24dp);

        if (fieldCode == 1) {
            theDialog.setTitle("Create Room");
        } else {
            theDialog.setTitle("Join Room");
        }
        return theDialog.create();
    }

    private void startALobby(String roomName, @Nullable String roomPass){
        Intent intent = null;
        if (fieldCode == 1) {
            intent = new Intent(context, VotingLobby.class);
        } else {
            intent = new Intent(context, VotingLobbyJoiner.class);
        }
        intent.putExtra(ROOMNAME, roomName);
        if (roomPass != null){
            intent.putExtra(ROOMPASS, roomPass);
        }
        startActivity(intent);
    }
}
