package com.example.myapplication.the_profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.profile_database.Database;
import com.example.myapplication.UserInfo;

public class ProfilePage extends AppCompatActivity implements ProfileChangeDialog.ProfileChangeDialogListener {

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

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

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
                            profileGPS.setChecked(userInfo.isGPS());

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
                profileChangeDialog.show(getSupportFragmentManager(), "passChange");
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

        profileGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // research this section a little bit before you jump on it //
                // alternatively, just use the normal onClickListener //
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

