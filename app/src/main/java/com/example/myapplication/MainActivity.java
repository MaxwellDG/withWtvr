package com.example.myapplication;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.profile_database.Database;


public class MainActivity extends AppCompatActivity {

    public static final String USERNAME = "USERNAME";
    public static final int REQUEST_CODE_SIGNUP = 1001;

    private EditText usernameInput;
    private EditText passwordInput;
    private String userEntered;
    private String passEntered;
    private Context context = this;

    private Handler mainHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);

        TextView withWtvr = findViewById(R.id.withWtvrText);
        TextView loginButton = findViewById(R.id.loginButton);

        TextPaint paint = withWtvr.getPaint();
        float width = paint.measureText("WithWtvr");

        Shader textShader = new LinearGradient(0, 0, width, withWtvr.getTextSize(),
                new int[]{
                        Color.parseColor("#eff225"),
                        Color.parseColor("#e2e3d3"),
                }, null, Shader.TileMode.CLAMP);
        withWtvr.getPaint().setShader(textShader);
        loginButton.getPaint().setShader(textShader);


        TextView signupButton = findViewById(R.id.signUpButton);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, signUp.class);
                startActivityForResult(intent, REQUEST_CODE_SIGNUP);
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userEntered = usernameInput.getText().toString();
                passEntered = passwordInput.getText().toString();

                final Database database = Database.getWithWtvrDatabase(getApplicationContext());
                RunnableCheck runnableCheck = new RunnableCheck(userEntered, passEntered, database);
                new Thread(runnableCheck).start();
            }
        });
    }

    public void startWithWtvr() {
        Intent intent = new Intent(this, homePage.class);
        intent.putExtra(USERNAME, userEntered);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGNUP) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    usernameInput.setText(data.getStringExtra("USERNAME_INPUT"));
                    passwordInput.setText(data.getStringExtra("PASSWORD_INPUT"));
                }
            } else if (resultCode == RESULT_CANCELED){
                Toast.makeText(context, "Signup cancelled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class RunnableCheck implements Runnable {

        private Database database;
        private String nameEntered;
        private String passwordEntered;

        public RunnableCheck(String nameEntered, String passwordEntered, Database database) {
            this.database = database;
            this.nameEntered = nameEntered;
            this.passwordEntered = passwordEntered;
        }

        @Override
        public void run() {
            UserInfo loginAttempt = database.getDAO_UserInfo().loginAttempt(nameEntered, passwordEntered);
            if (loginAttempt != null) {
                startWithWtvr();
                finish();
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        usernameInput.setText("");
                        passwordInput.setText("");
                        Toast.makeText(MainActivity.this, "Login entry incorrect.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}



