package com.example.myapplication.the_near_me;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class LocationService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        // stuff you want to exist goes here. Like... in their example, the MediaPlayer is instantiated here //
        // what do you need to make here? Maybe like... a fuckton of stuff for accessing the GoogleMaps API? //
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
