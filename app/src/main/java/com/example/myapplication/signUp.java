package com.example.myapplication;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.profile_database.Database;

public class signUp extends AppCompatActivity{

    private EditText userText;
    private EditText passText;
    private EditText emailText;
    private EditText passText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        TextView createText = findViewById(R.id.createText);
        TextView createButton = findViewById(R.id.createButton);
        TextPaint paint = createButton.getPaint();
        float width = paint.measureText("WithWtvr");

        Shader textShader = new LinearGradient(0, 0, width, createText.getTextSize(),
                new int[]{
                        Color.parseColor("#eff225"),
                        Color.parseColor("#e2e3d3"),
                }, null, Shader.TileMode.CLAMP);
        createText.getPaint().setShader(textShader);

        userText = findViewById(R.id.signUser);
        passText = findViewById(R.id.signPass);
        passText2 = findViewById(R.id.signPass2);
        emailText = findViewById(R.id.signEmail);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passText.getText().toString().equals(passText2.getText().toString())) {
                    UserInfo theirProfileInfo = new UserInfo(userText.getText().toString(),
                            passText.getText().toString(), emailText.getText().toString(),
                            7, false, 10, 2, 15);

                    Database database = Database.getWithWtvrDatabase(getApplicationContext());

                    RunnableInput runnable = new RunnableInput(theirProfileInfo, database);
                    new Thread(runnable).start();

                    Toast.makeText(signUp.this, "Account created!", Toast.LENGTH_SHORT).show();
                    startMain();
                } else{
                    passText.setText("");
                    passText2.setText("");
                    Toast.makeText(signUp.this, "Your password entries did not match.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void startMain(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USERNAME_INPUT", userText.getText().toString());
        intent.putExtra("PASSWORD_INPUT", passText.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    class RunnableInput implements java.lang.Runnable{

        private Database database;
        private UserInfo userInfo;

        RunnableInput(UserInfo userInfo, Database database) {
            this.database = database;
            this.userInfo = userInfo;
        }

        @Override
        public void run() {
            database.clearAllTables();
            database.getDAO_UserInfo().insertUserInfo(this.userInfo);;
        }
    }
}


