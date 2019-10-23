package com.example.myapplication.the_profile;

import android.annotation.SuppressLint;
import android.util.SparseArray;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AvatarSelector {

    private int avatarNumber;
    private String avatarName;

    public AvatarSelector() {
    }

    public AvatarSelector(int avatarNumber){
        this.avatarNumber = avatarNumber;
    }

    public int getAvatarPhoto(){
        switch (avatarNumber){
            case 1:
                avatarName = "a Giant Baby.";
                return R.drawable.agiantbaby;
            case 2:
                avatarName = "ALWAYS POSITIVE!";
                return R.drawable.alwayspositive;
            case 3:
                avatarName = "a Total Tramp.";
                return R.drawable.atotaltramp;
            case 4:
                avatarName = "probably gonna end up in jail.";
                return R.drawable.endupinjail;
            case 5:
                avatarName = "always getting lost.";
                return R.drawable.gettinglost;
            case 6:
                avatarName = "gotta rally the troops!";
                return R.drawable.gottarallythetroops;
            case 7:
                avatarName = "only here to dance.";
                return R.drawable.heretodance;
            case 8:
                avatarName = "only here for late night cheeseburgers";
                return R.drawable.onlyhereforlatenighteats;
            case 9:
                avatarName = "taken too much.";
                return R.drawable.takentoomuch;
            case 10:
                avatarName = "too old for this crap";
                return R.drawable.toooldforthiscrap;
            case 11:
                avatarName = "always with their partner.";
                return R.drawable.withtheirpartner;
            case 12:
                avatarName = "WRESTLE MANIA 9000";
                return R.drawable.wrestle9000;
        } return R.drawable.ic_account_box_black_24dp;
    }

    String getAvatarName() {
        return avatarName;
    }

    public void setAvatarName(String avatarName) {
        this.avatarName = avatarName;
    }

    public int getAvatarNumber() {
        return avatarNumber;
    }

    public void setAvatarNumber(int avatarNumber) {
        this.avatarNumber = avatarNumber;
    }

    HashMap<Integer, Integer> getAllAvatarResources(){
        @SuppressLint("UseSparseArrays") HashMap<Integer, Integer> allAvatarsInfo = new HashMap<>();
        allAvatarsInfo.put(1, R.drawable.agiantbaby);
        allAvatarsInfo.put(2, R.drawable.alwayspositive);
        allAvatarsInfo.put(3, R.drawable.atotaltramp);
        allAvatarsInfo.put(4, R.drawable.endupinjail);
        allAvatarsInfo.put(5, R.drawable.gettinglost);
        allAvatarsInfo.put(6, R.drawable.gottarallythetroops);
        allAvatarsInfo.put(7, R.drawable.heretodance);
        allAvatarsInfo.put(8, R.drawable.onlyhereforlatenighteats);
        allAvatarsInfo.put(9, R.drawable.takentoomuch);
        allAvatarsInfo.put(10, R.drawable.toooldforthiscrap);
        allAvatarsInfo.put(11, R.drawable.withtheirpartner);
        allAvatarsInfo.put(12, R.drawable.wrestle9000);
        return allAvatarsInfo;
    }
}
