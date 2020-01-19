package com.example.myapplication.the_profile;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import java.util.HashMap;

public class AvatarAdapter extends RecyclerView.Adapter {

    private HashMap<Integer, Integer> avatarResourcesHash;
    private Context context;
    private int row_index = -1;
    private onAvatarClickedListener listener;
    public static final String TAG = "TAG";

    public AvatarAdapter(Context context, HashMap<Integer, Integer> avatarResourcesHash, onAvatarClickedListener listener) {
        this.avatarResourcesHash = avatarResourcesHash;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.aavatar_segment, parent, false);
        return new AvatarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((AvatarViewHolder) holder).avatarImage.setImageResource(avatarResourcesHash.get(position + 1));
        ((AvatarViewHolder) holder).cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.clickedAvatar(position);
                row_index = position;
                notifyDataSetChanged();
            }
        });

        if (row_index == position) {
            ((AvatarViewHolder) holder).cardView.setBackgroundColor(Color.parseColor("#FFCCB826"));
            ((AvatarViewHolder) holder).cardView.setElevation(6);
            ((AvatarViewHolder) holder).checkBox.setChecked(true);
        } else {
            ((AvatarViewHolder) holder).cardView.setBackgroundColor(Color.WHITE);
            ((AvatarViewHolder) holder).cardView.setElevation(2);
            ((AvatarViewHolder) holder).checkBox.setChecked(false);
        }
    }


    @Override
    public int getItemCount() {
        return avatarResourcesHash.size();
    }
}

    class AvatarViewHolder extends RecyclerView.ViewHolder{

        ImageView avatarImage;
        CardView cardView;
        CheckBox checkBox;

        AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.aAvatarImage);
            cardView = itemView.findViewById(R.id.aAvatarCardView);
            checkBox = itemView.findViewById(R.id.aAvatarCheckBox);
        }

    }
