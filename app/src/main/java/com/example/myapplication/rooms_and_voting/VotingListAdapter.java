package com.example.myapplication.rooms_and_voting;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import java.util.ArrayList;

public class VotingListAdapter extends RecyclerView.Adapter {

    private ArrayList<String> optionsList;
    private ArrayList<String> optionsVoted = new ArrayList<>();
    private Context context;
    private int maxVotes;

    public VotingListAdapter(ArrayList<String> optionsList, Context context, int maxVotes) {
        this.optionsList = optionsList;
        this.context = context;
        this.maxVotes = maxVotes;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.aoptions_segment, parent, false);
        return new OptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((OptionViewHolder) holder).onBind(position, optionsList);

        ((OptionViewHolder) holder).optionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedItem = ((OptionViewHolder) holder).optionText.getText().toString();
                if (!optionsVoted.contains(selectedItem)){
                    optionsVoted.add(((OptionViewHolder) holder).optionText.getText().toString());
                    if (optionsVoted.size() > (maxVotes)){
                        optionsVoted.remove(0);
                    }
            } else {
                    optionsVoted.remove(selectedItem);
                }
                notifyDataSetChanged();
            }
        });
        // setting the appropriate highlights for options touched / not touched //
        if (optionsVoted.contains(((OptionViewHolder) holder).optionText.getText().toString())) {
            ((OptionViewHolder) holder).optionLayout.setBackgroundColor(Color.parseColor("#FFCCB826"));
            ((OptionViewHolder) holder).optionText.setTextColor(Color.WHITE);
        } else {
            ((OptionViewHolder) holder).optionLayout.setBackgroundColor(Color.WHITE);
            ((OptionViewHolder) holder).optionText.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return optionsList.size();
    }

    ArrayList<String> getItemsVoted(){
        return optionsVoted;
    }
}

    class OptionViewHolder extends RecyclerView.ViewHolder {

        TextView optionText;
        LinearLayout optionLayout;

        OptionViewHolder(@NonNull View itemView) {
            super(itemView);
            optionText = itemView.findViewById(R.id.aOptionText);
            optionLayout = itemView.findViewById(R.id.aOptionLayout);
        }

        void onBind(int position, ArrayList<String> optionsList) {
            if (optionsList.get(position) != null) {
                optionText.setText(optionsList.get(position));
            }
        }
    }
