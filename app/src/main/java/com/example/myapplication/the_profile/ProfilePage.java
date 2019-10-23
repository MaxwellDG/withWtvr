package com.example.myapplication.the_profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.profile_database.Database;
import com.example.myapplication.UserInfo;

public class ProfilePage extends AppCompatActivity implements ProfileChangeDialog.ProfileChangeDialogListener {

    public static final int NEW_AVATAR_REQUEST = 1002;
    public static final int LOCATION_PERMISSIONS_REQUEST_CODE = 9001;

    private static final String TAG = "TAG";
    private String username;
    private String password;
    private String email;
    private Database database;
    private Context context = this;

    private TextView profileTheirDisplayName;
    private TextView profileTheirPassword;
    private TextView profileTheirEmail;
    private TextView profileAccountName;
    private Switch profileGPS;
    private Switch profileTheme;
    private Switch profileInvitesAllowed;
    private ImageView profileAvatar;
    private TextView profileAvatarName;
    private ImageView profileChangePassword;
    private ImageView profileChangeEmail;
    private UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        this.profileTheirDisplayName = findViewById(R.id.profileTheirDisplayName);
        this.profileTheirPassword = findViewById(R.id.profileTheirPassword);
        this.profileTheirEmail = findViewById(R.id.profileTheirEmail);
        this.profileAccountName = findViewById(R.id.profileAccountName);
        this.profileGPS = findViewById(R.id.switchGPS);
        this.profileTheme = findViewById(R.id.switchTheme);
        this.profileInvitesAllowed = findViewById(R.id.switchInvites);
        this.profileAvatar = findViewById(R.id.theirAvatarPhoto);
        this.profileAvatarName = findViewById(R.id.theirAvatarName);
        this.profileChangePassword = findViewById(R.id.profileChangePassword);
        this.profileChangeEmail = findViewById(R.id.profileChangeEmail);

        this.username = getIntent().getStringExtra("USERNAME");
        this.database = Database.getWithWtvrDatabase(getApplicationContext());



        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                userInfo = database.getDAO_UserInfo().profileUpload(username);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (userInfo != null) {
                            profileTheirDisplayName.setText(userInfo.getUsername());
                            profileTheirEmail.setText(userInfo.getEmail());
                            email = userInfo.getEmail();
                            profileAccountName.setText(userInfo.getUsername());
                            profileTheirPassword.setText(userInfo.getPassword());
                            password = userInfo.getPassword();
                            doGPSSetup(isLocationPermissionGranted(), userInfo.getGPS());

                            AvatarSelector avatarSelector = new AvatarSelector(userInfo.getAvatarId());
                            profileAvatar.setImageResource(avatarSelector.getAvatarPhoto());
                            profileAvatarName.setText(avatarSelector.getAvatarName());
                        }
                    }
                });
            }
        };
        new Thread(runnable).start();



        profileChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileChangeDialog profileChangeDialog =
                        new ProfileChangeDialog(password, username, getApplicationContext(), 1);
                profileChangeDialog.show((getSupportFragmentManager()), "passChange");
            }
        });

        profileChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileChangeDialog profileChangeDialog =
                        new ProfileChangeDialog(email, username, getApplicationContext(), 2);
                profileChangeDialog.show(getSupportFragmentManager(), "emailChange");
            }
        });

        profileAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePage.this, PickNewAvatar.class);
            startActivityForResult(intent, NEW_AVATAR_REQUEST);
        });

         profileGPS.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (profileGPS.isChecked()){
                     String[] permissionsList = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                     ActivityCompat.requestPermissions((Activity) context, permissionsList, LOCATION_PERMISSIONS_REQUEST_CODE);
                 } else {
                     Toast.makeText(context, "Must turn off in phone settings.", Toast.LENGTH_SHORT).show();
                     profileGPS.setChecked(true);
                 }
             }
         });
    }

    public boolean isLocationPermissionGranted(){
        return PermissionChecker.checkSelfPermission
                (context, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
                && PermissionChecker.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PermissionChecker.PERMISSION_GRANTED;
    }

    private void doGPSSetup(boolean isSystemGPSEnabled, boolean isProfileGPSEnabled){
        if(isSystemGPSEnabled) {
            if (!isProfileGPSEnabled) {
                updateDatabaseGPS(true);
            }
            profileGPS.setChecked(true);
        } else {
            if (isProfileGPSEnabled){
                updateDatabaseGPS(false);
            }
            profileGPS.setChecked(false);
        }
    }

    private void updateDatabaseGPS(boolean isTrue){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                database.getDAO_UserInfo().updateGPS(isTrue, username);
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainbar_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutButton:
                Intent intent2 = new Intent(this, MainActivity.class);
                startActivity(intent2);
                break;
            case R.id.buttonforhelp:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == NEW_AVATAR_REQUEST) {
            Log.d(TAG, "onActivityResult: RequestCode");
            if (resultCode == RESULT_OK && data != null) {
                Log.d(TAG, "onActivityResult: ResultCode Okay");
                int newAvatarCode = data.getIntExtra("AVATAR_SELECTED", 0);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        database.getDAO_UserInfo().updateAvatar(newAvatarCode, profileAccountName.getText().toString());
                    }
                };
                new Thread(runnable).start();
                AvatarSelector avatarSelector = new AvatarSelector(newAvatarCode);
                profileAvatar.setImageResource(avatarSelector.getAvatarPhoto());
                profileAvatarName.setText(avatarSelector.getAvatarName());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0){
                    for(int i : grantResults) {
                        try {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                profileGPS.setChecked(false);
                                updateDatabaseGPS(false);
                                break;
                            }
                            profileGPS.setChecked(true);
                            updateDatabaseGPS(true);
                            break;
                    } catch (RuntimeException e){
                        profileGPS.setChecked(false);
                        updateDatabaseGPS(false);
                        e.printStackTrace();
                    }
                    }
                }
        }
    }

    @Override
    public void applyTextChange(String changedText, int resourceFieldCode) {
        switch (resourceFieldCode){
            case 1:
                Log.d(TAG, "applyTextChange: Activity has received info.");
                profileTheirPassword.setText(changedText);
                break;
            case 2:
                profileTheirEmail.setText(changedText);
                break;
            case 3:
                // changing display name //
        }
    }


}

