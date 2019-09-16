package com.example.myapplication.rooms_and_voting;

import android.content.Context;
import android.os.Looper;

public class AddOptionThread extends Thread {

    public TheHandler handler;
    private Context context;

    public AddOptionThread(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new TheHandler(context);
        Looper.loop();
    }
}
