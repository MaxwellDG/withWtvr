package com.example.myapplication.rooms_and_voting;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.Random;

public class DestinationAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<String> destinationList;

    public DestinationAdapter(Context context, ArrayList<String> destinationList) {
        this.context = context;
        this.destinationList = destinationList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adestination_segment, parent, false);
        return new DestinationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((DestinationViewHolder) holder).onBind(position, destinationList);
    }

    @Override
    public int getItemCount() {
        return destinationList.size();
    }

    class DestinationViewHolder extends RecyclerView.ViewHolder{

        private TextView theText;
        private ConstraintLayout constraintLayout;

        DestinationViewHolder(@NonNull View itemView) {
            super(itemView);
            this.theText = itemView.findViewById(R.id.aDestinationText);
            this.constraintLayout = itemView.findViewById(R.id.aDestinationConLay);
        }

        void onBind(int position, ArrayList<String> destinationList){
            Random random = new Random();
            int theTextColour = Color.argb(random.nextInt(255), random.nextInt(255),
                    random.nextInt(255), random.nextInt(255));
            theText.setText(destinationList.get(position));
            theText.setTextColor(theTextColour);
            /* TODO: set each of the ARGB values individually and make the scope something high. Then, set
                the next set of numbers really low. And if shit gets really similar looking still, you could randomize
                between a 0 and 1 value which would then dictate which value goes to which of the two fields
             */
        }
    }
}
