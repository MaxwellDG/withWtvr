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

public class CreateRoomDialog extends DialogFragment {

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
                .setMessage("Complete the fields below: ").setView(inflater.inflate(R.layout.lobby_dialog, null))
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (fieldCode == 1) {
                            EditText editRoom = getDialog().findViewById(R.id.dialogRoomName);
                            String roomName = editRoom.getText().toString();
                            EditText editPass = getDialog().findViewById(R.id.dialogRoomPass);
                            String roomPass = editRoom.getText().toString();
                            startCreateVotingLobby(roomName, roomPass);
                        } else {
                            // BUNCH OF HARD STUFF HAS TO GO HERE, AND MIGHT HAVE TO CHANGE THE ABOVE SECTION A LOT MORE THAN YOU THINK (WILL OBV HAVE TO CHANGE A BIT) AFTER YOU LEARN ABOUR SERVICES MORE //
                        }
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

    public void startCreateVotingLobby(String roomName, @Nullable String roomPass){
        Intent intent = new Intent(context, VotingLobby.class);
        intent.putExtra(ROOMNAME, roomName);
        if (roomPass != null){
            intent.putExtra(ROOMPASS, roomPass);
        }
        startActivity(intent);
    }
}
