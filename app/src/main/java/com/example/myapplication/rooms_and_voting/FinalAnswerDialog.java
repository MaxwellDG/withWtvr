package com.example.myapplication.rooms_and_voting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.homePage;

public class FinalAnswerDialog extends DialogFragment {

    private String answer;
    private Context context;

    FinalAnswerDialog(String answer, Context context) {
        this.answer = answer;
        this.context = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("You're going to...")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
                Intent intent = new Intent(context, homePage.class);
                startActivity(intent);
                ((Activity) context).finish();
            }})
                .setMessage(answer);
        return builder.create();
    }
}
