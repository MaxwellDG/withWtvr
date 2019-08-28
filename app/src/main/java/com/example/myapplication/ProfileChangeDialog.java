package com.example.myapplication;

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
import com.example.myapplication.The_Database.Database;

public class ProfileChangeDialog extends DialogFragment {

    private static final int DIALOG_SUCCESS = 1;
    private static final int DIALOG_INCORRECT_INITIAL = 2;
    private static final int DIALOG_INCORRECT_PAIRING = 3;
    private static final int DIALOG_CANCELLED = 4;
    private static final String TAG = "TAG";

    private String previousInfo;
    private Context context;
    private String name;
    private EditText previousPassConfirm;
    private EditText newInput;
    private EditText newInput2;
    private int fieldChangeCode;

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
        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setMessage("Complete fields below.")
                .setTitle("Change profile info: ")
                .setIcon(R.drawable.ic_account_box_black_24dp)
                .setPositiveButton("Change info", null)
                .setNegativeButton("Cancel", null)
                .create();

        if (fieldChangeCode == 1) {
            builder.setView(inflater.inflate(R.layout.profile_change_dialog_password, null));
        } else if (fieldChangeCode == 2){
            builder.setView(inflater.inflate(R.layout.profile_change_dialog_email, null));
        }
        builder.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {
                Button button2 = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "onClick: You clicked the positive button");

                        if (fieldChangeCode == 1) {
                            previousPassConfirm = getDialog().findViewById(R.id.dialogChangePrevPass);
                            newInput = getDialog().findViewById(R.id.dialogChangeNewEmail);
                            newInput2 = getDialog().findViewById(R.id.dialogChangeNewEmail2);
                        } else if (fieldChangeCode == 2){
                            newInput = getDialog().findViewById(R.id.dialogChangeNewEmail);
                            newInput2 = getDialog().findViewById(R.id.dialogChangeNewEmail2);

                            Log.d(TAG, "onClick: PreNull");
                            previousPassConfirm = null;
                            Log.d(TAG, "onClick: PostNull");
                        }

                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "run: It's running... " + newInput.getText().toString());
                                Database database = Database.getWithWtvrDatabase(context);
                                switch (fieldChangeCode) {
                                    case 1:
                                        database.getDAO_UserInfo().updatePassword(newInput.getText().toString(), name);
                                        break;
                                    case 2:
                                        database.getDAO_UserInfo().updateEmail(newInput.getText().toString(), name);
                                        break;
                                }
                            }
                        };

                        if (previousPassConfirm == null){
                            if (!newInput.getText().toString().equals(newInput2.getText().toString())){
                                incorrectNewInputs();
                            } else {
                                switch (fieldChangeCode){
                                    case 2:
                                        showToast(DIALOG_SUCCESS);
                                        Log.d(TAG, "onClick: Correct info");
                                        new Thread(runnable).start();
                                        listener.applyTextChange(newInput.getText().toString(), 2);
                                        break;
                                    case 3:
                                        // changing display name will go here later //
                                        break;
                                }
                                dialog.dismiss();
                            }
                        } else {
                            if (newInput.getText().toString().equals(newInput2.getText().toString()) &&
                                    previousInfo.equals(previousPassConfirm.getText().toString())) {
                                showToast(DIALOG_SUCCESS);
                                Log.d(TAG, "onClick: Correct info");
                                new Thread(runnable).start();
                                listener.applyTextChange(newInput.getText().toString(), 1);
                                Log.d(TAG, "run: Listener has been touched.");
                                dialog.dismiss();
                                } else if (!previousInfo.equals(previousPassConfirm.getText().toString())) {
                                    Log.d(TAG, "onClick: Incorrect previous.");
                                    newInput.setText("");
                                    newInput2.setText("");
                                    previousPassConfirm.setText("");
                                    showToast(DIALOG_INCORRECT_INITIAL);
                                } else {
                                    incorrectNewInputs();
                                }
                        }
                    }
                });
                button2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "onClick: YOu clicked the negative button.");
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
        newInput2.setText("");
        Log.d(TAG, "onClick: else (incorrect new inputs)");
        showToast(DIALOG_INCORRECT_PAIRING);
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
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            Log.d(TAG, "onAttach: Listener attached.");
            listener = (ProfileChangeDialogListener) context;
        } catch (ClassCastException e){
            Log.d(TAG, "onAttach: Listener NOT attached.");
            e.printStackTrace();
        }
    }

    public interface ProfileChangeDialogListener{
        void applyTextChange(String changedText, int resourceCodeField);
    }
}

