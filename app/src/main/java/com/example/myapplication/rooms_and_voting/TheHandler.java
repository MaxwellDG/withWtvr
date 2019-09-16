package com.example.myapplication.rooms_and_voting;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class TheHandler extends Handler {

    private static final String TAG = "TAG";
    private AddToListListener listener;

    public TheHandler(Context context){
        listener = (AddToListListener) context;
    }

    @Override
    public void handleMessage(Message msg) {
        listener.addToList(msg.obj.toString());
    }

    public interface AddToListListener {
        void addToList(String newDestination);
    }
}