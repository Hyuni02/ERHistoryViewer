package com.example.erhistoryviewer;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class frg_historyItem extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.frg_historyitem, container, false);

        ImageView img_playCharacter = view.findViewById(R.id.img_playCharacter);
        TextView txt_gameRank = view.findViewById(R.id.txt_gameRank);
        TextView txt_kda = view.findViewById(R.id.txt_kda);
        TextView txt_date = view.findViewById(R.id.txt_date);

        String matchmode = this.getArguments().getString("matchmode");
        img_playCharacter.setImageResource(getArguments().getInt("img"));

        switch (matchmode){
            case "rank":
            case "normal":
                String rank = getArguments().getString("rank");
                if(rank == Integer.toString(1)){
                    txt_gameRank.setTextColor(Color.GREEN);
                }
                else if(rank == Integer.toString(2) || rank == Integer.toString(3)){
                    txt_gameRank.setTextColor(Color.BLUE);
                }

                txt_gameRank.setText("# " + rank);
                txt_date.setText(getArguments().getString("date"));
                txt_kda.setText("K(TK)/D/A " + getArguments().getString("kda"));
                break;
            case "cobalt":
                String winlose="";
                if(getArguments().getString("rank") == Integer.toString(1)){
                    winlose = "승리";
                    txt_gameRank.setTextColor(Color.BLUE);
                }
                else{
                    winlose = "패배";
                    txt_gameRank.setTextColor(Color.RED);
                }
                txt_gameRank.setText(winlose);
                txt_date.setText(getArguments().getString("date"));
                txt_kda.setText(getArguments().getString("kda"));
                break;
        }

        return view;
    }
}
