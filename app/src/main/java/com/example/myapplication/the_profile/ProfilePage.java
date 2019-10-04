package com.example.myapplication.the_profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.profile_database.Database;
import com.example.myapplication.UserInfo;

public class ProfilePage extends AppCompatActivity implements ProfileChangeDialog.ProfileChangeDialogListener {

    public static final int NEW_AVATAR_REQUEST = 1001;
    public static final int LOCATION_PERMISSIONS_REQUEST_CODE = 9001;

    private static final String TAG = "TAG";
    private String username;
    private String password;
    private String email;
    private Database database;
    private Context context;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        this.context = getApplicationContext();
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
                final UserInfo userInfo = database.getDAO_UserInfo().profileUpload(username);
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
                            //TODO: the below part is getting triggered on the start of the activity. Find someway to make a custom. SUPER CUSTOM onCheckChangedListener that will take requestCodes that will identify if the permissions thing needs to be triggered or not //
                            // profileGPS.setChecked(userInfo.isGPS());

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
                // TODO: Long emails in the layout for this look fucked //
                ProfileChangeDialog profileChangeDialog =
                        new ProfileChangeDialog(email, username, getApplicationContext(), 2);
                profileChangeDialog.show(getSupportFragmentManager(), "emailChange");
            }
        });

        profileAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfilePage.this, pickNewAvatar.class);
                startActivityForResult(intent, NEW_AVATAR_REQUEST);
                // TODO: Maybe this should be a fragment? ungh I don't want to do that. Maybe an activity is fine wtvr, just learn how to do that with forResult //
            }
        });

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
        // this will receive the data for which picture they want as their avatar //
        // something something remember that you've got AvatarSelector class setup. WAit. MAaybe.
        // Not. Hmmm... maybe that'll be. Okay yea. That'll just be something you need to do at the
        // VERY END of this function by adding some database input with the proper number. It's already set up to
        // reload the correct image for next time. //


        // this below part will update the database for future returns to this page.
        // Just remember that "SELECTED_AVATAR" needs to be put in the Intent data that will link back to this //
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                database.getDAO_UserInfo().updateAvatar(data.getIntExtra("SELECTED_AVATAR",
                        1), profileAccountName.getText().toString());
            }
        };
        new Thread(runnable).start();
        super.onActivityResult(requestCode, resultCode, data);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0){
                    for(int i : grantResults){
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            updateDatabaseGPS(false);
                            break;
                        }
                        updateDatabaseGPS(true);
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

