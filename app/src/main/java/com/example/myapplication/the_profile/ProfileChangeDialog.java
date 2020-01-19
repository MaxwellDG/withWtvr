package com.example.myapplication.the_profile;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.R;
import com.example.myapplication.profile_database.Database;

public class ProfileChangeDialog extends DialogFragment {

    private static final int DIALOG_SUCCESS = 1;
    private static final int DIALOG_INCORRECT_INITIAL = 2;
    private static final int DIALOG_INCORRECT_PAIRING = 3;
    private static final int DIALOG_CANCELLED = 4;
    public static final int DIALOG_INCORRECT_LIMIT = 5;

    private String previousInfo;
    private Context context;
    private String name;
    private EditText previousPassConfirm;
    private EditText newInput;
    private EditText newInput2;
    private int fieldChangeCode;
    private Runnable runnable;

    private ProfileChangeDialogListener listener;

    ProfileChangeDialog(String previousInfo, String name, Context context, int fieldChangeCode) {
        this.previousInfo = previousInfo;
        this.context = context;
        this.name = name;
        this.fieldChangeCode = fieldChangeCode;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setMessage("Complete fields below.")
                .setTitle("Change profile info: ")
                .setIcon(R.drawable.ic_account_box_black_24dp)
                .setPositiveButton("Change info", null)
                .setNegativeButton("Cancel", null)
                .create();
                builder.setView(setViewAndHints(fieldChangeCode));

        builder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button button2 = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                runnable = () -> {
                    Database database = Database.getWithWtvrDatabase(context);
                    switch (fieldChangeCode) {
                        case 1:
                            database.getDAO_UserInfo().updatePassword(newInput.getText().toString(), name);
                            break;
                        case 2:
                            database.getDAO_UserInfo().updateEmail(newInput.getText().toString(), name);
                            break;
                        case 3:
                            database.getDAO_UserInfo().updateMaxDestinations(newInput.getText().toString(), name);
                            break;
                        case 4:
                            database.getDAO_UserInfo().updateMaxVotes(newInput.getText().toString(), name);
                            break;
                        case 5:
                            database.getDAO_UserInfo().updateTimerLength(newInput.getText().toString(), name);
                            break;
                    }
                };

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        newInput = getDialog().findViewById(R.id.dialogRoomFirstField);
                        int newInt = 0;
                        switch(fieldChangeCode) {
                            case 1:
                                previousPassConfirm = getDialog().findViewById(R.id.deviceNewName);
                                newInput2 = getDialog().findViewById(R.id.dialogChangeSecondField);
                                if (newInput.getText().toString().equals(newInput2.getText().toString()) &&
                                        previousInfo.equals(previousPassConfirm.getText().toString())) {
                                    showToast(DIALOG_SUCCESS);
                                    new Thread(runnable).start();
                                    listener.applyTextChange(newInput.getText().toString(), fieldChangeCode);
                                    dialog.dismiss();
                                    break;
                                } else if (!previousInfo.equals(previousPassConfirm.getText().toString())) {
                                    newInput.setText("");
                                    newInput2.setText("");
                                    previousPassConfirm.setText("");
                                    showToast(DIALOG_INCORRECT_INITIAL);
                                    break;
                                } else {
                                    incorrectNewInputs();
                                    break;
                                }
                            case 2:
                                newInput2 = getDialog().findViewById(R.id.dialogChangeSecondField);
                                if (!newInput.getText().toString().equals(newInput2.getText().toString())){
                                incorrectNewInputs();
                                break;
                            } else {
                                    showToast(DIALOG_SUCCESS);
                                    new Thread(runnable).start();
                                    listener.applyTextChange(newInput.getText().toString(), fieldChangeCode);
                                    dialog.dismiss();
                                    break;
                                }
                            case 3:
                                try {
                                    newInt = Integer.parseInt(newInput.getText().toString());
                                } catch (IllegalArgumentException e){
                                    showToast(DIALOG_INCORRECT_LIMIT);
                                    incorrectNewInputs();
                                    break;
                                }
                                responseToInput((newInt >= 2 && newInt <= 10), getDialog());
                                break;
                            case 4:
                                try {
                                    newInt = Integer.parseInt(newInput.getText().toString());
                                } catch (IllegalArgumentException e){
                                    showToast(DIALOG_INCORRECT_LIMIT);
                                    incorrectNewInputs();
                                    break;
                                }
                                responseToInput((newInt >= 1 && newInt <= 5), getDialog());
                                break;
                            case 5:
                                try {
                                    newInt = Integer.parseInt(newInput.getText().toString());
                                } catch (IllegalArgumentException e){
                                    showToast(DIALOG_INCORRECT_LIMIT);
                                    incorrectNewInputs();
                                    break;
                                }
                                responseToInput((newInt >= 5 && newInt <= 30), getDialog());
                                break;
                        }
                    }
                });

                button2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showToast(DIALOG_CANCELLED);
                        dialog.dismiss();
                    }
                });
            }
        });
        return builder;
    }

    private void incorrectNewInputs(){
        newInput.setText("");
        try{
            newInput2.setText("");
        } catch(NullPointerException e){
            e.printStackTrace();
        }
        showToast(DIALOG_INCORRECT_LIMIT);
    }

    private void responseToInput(boolean bool, Dialog dialog){
        if(bool){
            new Thread(runnable).start();
            listener.applyTextChange(newInput.getText().toString(), fieldChangeCode);
            showToast(DIALOG_SUCCESS);
            dialog.dismiss();
        } else {
            showToast(DIALOG_INCORRECT_LIMIT);
            incorrectNewInputs();
        }
    }

    private View setViewAndHints(int fieldCode){
        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        View theView = inflater.inflate(R.layout.dialog_profile_change_general, null);
        EditText editText = theView.findViewById(R.id.dialogRoomFirstField);
        switch (fieldCode){
            case 1:
                return inflater.inflate(R.layout.dialog_profile_change_password, null);
            case 2:
                return inflater.inflate(R.layout.dialog_profile_change_email, null);
            case 4:
                editText.setHint("Max votes per person: <=5");
                return theView;
            case 5:
                editText.setHint("Length of voting time: <=30");
                return theView;
            default:
                editText.setHint("Max destination options: <=11");
                // fieldCode 3 //
                return theView;
        }
    }

    private void showToast(int requestCode) {
        switch (requestCode) {
            case (DIALOG_SUCCESS):
                Toast.makeText(context, "Information changed", Toast.LENGTH_SHORT).show();
                break;
            case (DIALOG_INCORRECT_INITIAL):
                Toast.makeText(context, "Incorrect previous password", Toast.LENGTH_SHORT).show();
                break;
            case (DIALOG_INCORRECT_PAIRING):
                Toast.makeText(context, "New inputs do not match", Toast.LENGTH_SHORT).show();
                break;
            case (DIALOG_CANCELLED):
                Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show();
                break;
            case (DIALOG_INCORRECT_LIMIT):
                Toast.makeText(context, "Incorrect input.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ProfileChangeDialogListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    public interface ProfileChangeDialogListener{
        void applyTextChange(String changedText, int resourceCodeField);
    }
}

