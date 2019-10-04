package com.example.myapplication.the_profile;

import android.Manifest;
import android.content.Context;
import android.util.Log;
import android.widget.CompoundButton;

import androidx.core.app.ActivityCompat;

public class MyOnCheckedChangedListener implements CompoundButton.OnCheckedChangeListener {

    public static final String TAG = "TAG";
    public static final int LOCATION_PERMISSIONS_REQUEST_CODE = 9001;




    // TODO: OKAYYY dude. youre drunk. That's enough. But yea, you fuckin killed it AGIAN today. GOod job. now get back at it and figure out how to make a custom listener for your updateGPS thingi. It just needs to have request codes added because otherwise it triggers all the time //




    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        realOnCheckedChanged(buttonView.getContext(), buttonView, isChecked, LOCATION_PERMISSIONS_REQUEST_CODE);
    }

    public void realOnCheckedChanged(Context context, CompoundButton buttonView, boolean isChecked, int requestCode){
        switch (requestCode){
            case 3001:
                Log.d(TAG, "realOnCheckedChanged: Still, don't do nuffin.");
            case 3002:
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: YOU TOUCHED THE BUTTON.");
                        if (buttonView.isChecked()){
                            Log.d(TAG, "run: YOU MADE IT POSITIVE.");
                            String[] permissionsList = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                           // ActivityCompat.requestPermissions(context, permissionsList, LOCATION_PERMISSIONS_REQUEST_CODE);

                        } else if (!buttonView.isChecked()){
                            Log.d(TAG, "run: YOU MADE IT NEGATIVE.");
                            // TODO: this is the only possible scenario where you haven't figured out what to do yet. How do you ActivityCompat TURN OFF a permission? //
                        }
                    }
                };
                new Thread(runnable).start();
        }
        }
    }
