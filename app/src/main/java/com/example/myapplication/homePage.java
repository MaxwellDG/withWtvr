package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.example.myapplication.profile_database.Database;
import com.example.myapplication.rooms_and_voting.VotingLobby;
import com.example.myapplication.rooms_and_voting.VotingLobbyJoiner;
import com.example.myapplication.the_near_me.MapsActivity;
import com.example.myapplication.the_profile.ProfilePage;


public class homePage extends AppCompatActivity {

    public static final int INFORMATION_DIALOG_REQUESTCODE = 1;
    public static final String KEY_USERNAME = "USERNAME";

    private String username;
    private int[] compassImageArray = {R.drawable.ww_compass_clean_white_cropped, R.drawable.ww_compass_clean_white_cropped_yellowpop};
    private Context context = this;

    @Override
    protected void onResume() {
        super.onResume();
        getOrSetUserPreferences();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        ImageView compassImage = findViewById(R.id.compassButton);

        View createRoomButton = findViewById(R.id.createRoomButton);
        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnActivity(VotingLobby.class);
            }
        });

        View joinRoomButton = findViewById(R.id.joinRoomButton);
        joinRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnActivity(VotingLobbyJoiner.class);
            }
        });


        compassImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnActivity(MapsActivity.class);
            }
        });

        ImageView profileButton = findViewById(R.id.homeProfileButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnActivity(ProfilePage.class);
            }
        });

        ImageView helpButton = findViewById(R.id.homeHelpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpDialogFragment helpDialogFragment = new HelpDialogFragment(INFORMATION_DIALOG_REQUESTCODE);
                helpDialogFragment.show(getSupportFragmentManager(), "HomePage Help Dialog");
            }
        });
        animateCompass(compassImage, compassImageArray, 0, true);
    }

    public void animateCompass(final ImageView imageView, final int[] images, final int imageIndex, final boolean forever){
        int fadeInDuration = 500;
        int timeBetween = 1500;
        int fadeOutDuration = 1000;

        imageView.setImageResource(images[imageIndex]);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(fadeInDuration);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartOffset(fadeInDuration + timeBetween);
        fadeOut.setDuration(fadeOutDuration);

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setRepeatCount(1);

        imageView.setAnimation(animation);

        Runnable runnable = () -> animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation1) {
                if (images.length - 1 > imageIndex) {
                    animateCompass(imageView, images, imageIndex + 1,forever);
                }
            else {
            if (forever){
                animateCompass(imageView, images, 0, forever);
            }
        }
            }
            public void onAnimationRepeat(Animation animation1) {
            }
            public void onAnimationStart(Animation animation1) {
            }
        });
        new Thread(runnable).start();
    }


    public void getOrSetUserPreferences(){
        String intentUser = getIntent().getStringExtra("USERNAME");
        if(intentUser != null){
            username = getIntent().getStringExtra("USERNAME");
            setPrefsFromDatabase(username);
        } else {
            SharedPreferences prefs = getSharedPreferences("PREFS_FILE", MODE_PRIVATE);
            username = prefs.getString(KEY_USERNAME, "userName");
        }
    }

    public void setPrefsFromDatabase(String user){
        final UserInfo[] userInfo = new UserInfo[1];
        SharedPreferences.Editor editor = getSharedPreferences("PREFS_FILE", MODE_PRIVATE).edit();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Database database = Database.getWithWtvrDatabase(context);
                userInfo[0] = database.getDAO_UserInfo().profileUpload(user);
                editor.putString(KEY_USERNAME, username);
                editor.putString("PASSWORD", userInfo[0].getPassword());
                editor.putString("EMAIL", userInfo[0].getEmail());
                editor.putBoolean("GPS", userInfo[0].getGPS());
                editor.putInt("MAXDEST", userInfo[0].getMaxDestinations());
                editor.putInt("MAXVOTES", userInfo[0].getMaxVotes());
                editor.putInt("TIMERLENGTH", userInfo[0].getTimerLength());
                editor.putInt("AVATAR", userInfo[0].getAvatarId());
                editor.apply();
            }
        };
        new Thread(runnable).start();
    }


    public void startAnActivity(Class aClass) {
        Intent intent = new Intent(this, aClass);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
        if(aClass == MapsActivity.class){
            overridePendingTransition(R.anim.trans_in_left, R.anim.trans_out_right);
        } else {
            overridePendingTransition(R.anim.trans_in_right, R.anim.trans_out_left);
        }
    }

    @Override
    public void onBackPressed() {
        // do nothing //
    }
}


