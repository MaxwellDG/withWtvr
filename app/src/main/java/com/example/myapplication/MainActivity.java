package com.example.myapplication;

import android.content.Intent;
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

import com.example.myapplication.The_Database.Database;


public class MainActivity extends AppCompatActivity {

    public static final String USERNAME = "USERNAME";

    private EditText userText;
    private EditText passText;
    private String userEntered;
    private String passEntered;

    private UserInfo loginAccount;
    private Handler mainHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userText = findViewById(R.id.usernameInput);
        passText = findViewById(R.id.passwordInput);

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
                // TODO: this could be done nicer with an ActivityOnResult function //
                startSignUp();
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userEntered = userText.getText().toString();
                passEntered = passText.getText().toString();

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

    public void startSignUp() {
        Intent intent = new Intent(this, signUp.class);
        startActivity(intent);
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
            loginAccount = loginAttempt;
            if (loginAttempt != null) {
                startWithWtvr();
                finish();
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        userText.setText("");
                        passText.setText("");
                        Toast.makeText(MainActivity.this, "Login entry incorrect.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}



