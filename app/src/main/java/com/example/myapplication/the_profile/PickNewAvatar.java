package com.example.myapplication.the_profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.R;

import java.util.HashMap;

public class PickNewAvatar extends AppCompatActivity {

    public static final int NEW_AVATAR_REQUEST = 1002;

    private RecyclerView recyclerView;
    private Context context = this;
    private AvatarAdapter avatarAdapter;
    @SuppressLint("UseSparseArrays")
    private int whichAvatarSelected;
    public static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_new_avatar);

        recyclerView = findViewById(R.id.pickNewRecycler);
        AvatarSelector avatarSelector = new AvatarSelector();
        avatarAdapter = new AvatarAdapter(context, avatarSelector.getAllAvatarResources(), new onAvatarClickedListener() {
            @Override
            public void clickedAvatar(int position) {
                whichAvatarSelected = position;
            }
        });
        recyclerView.setAdapter(avatarAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL));

        Button selectAvatarButton = findViewById(R.id.selectAvatarButton);
        selectAvatarButton.setOnClickListener(v ->{
            Log.d(TAG, "onCreate: Listener activated in PICKNEWAVATAR");
           Intent intent = new Intent(context, ProfilePage.class);
           intent.putExtra("AVATAR_SELECTED", (whichAvatarSelected + 1));
           setResult(RESULT_OK, intent);
           finish();
        });

    }
}
