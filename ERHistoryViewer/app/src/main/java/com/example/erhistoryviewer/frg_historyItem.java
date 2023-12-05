package com.example.erhistoryviewer;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.HashMap;

public class frg_historyItem extends Fragment {
    public LinearLayout content_detail;
    Button btn_close;

    Requester requester = new Requester();
    Converter converter = new Converter();
    FragmentManager fragmentManager;

    int gameId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frg_historyitem, container, false);

        ImageView img_playCharacter = view.findViewById(R.id.img_playCharacter);
        TextView txt_gameRank = view.findViewById(R.id.txt_gameRank);
        TextView txt_kda = view.findViewById(R.id.txt_kda);
        TextView txt_date = view.findViewById(R.id.txt_date);

        String matchmode = this.getArguments().getString("matchmode");
        img_playCharacter.setImageResource(getArguments().getInt("img"));
        gameId = getArguments().getInt("gameId");
        switch (matchmode) {
            case "rank":
            case "normal":
                String rank = getArguments().getString("rank");
                if (rank == Integer.toString(1)) {
                    txt_gameRank.setTextColor(Color.GREEN);
                } else if (rank == Integer.toString(2) || rank == Integer.toString(3)) {
                    txt_gameRank.setTextColor(Color.BLUE);
                }

                txt_gameRank.setText("# " + rank);
                txt_date.setText(getArguments().getString("date"));
                txt_kda.setText("K(TK)/D/A " + getArguments().getString("kda"));
                break;
            case "cobalt":
                String winlose = "";
                if (getArguments().getString("rank") == Integer.toString(1)) {
                    winlose = "승리";
                    txt_gameRank.setTextColor(Color.BLUE);
                } else {
                    winlose = "패배";
                    txt_gameRank.setTextColor(Color.RED);
                }
                txt_gameRank.setText(winlose);
                txt_date.setText(getArguments().getString("date"));
                txt_kda.setText(getArguments().getString("kda"));
                break;
        }

        fragmentManager = getActivity().getSupportFragmentManager();
        img_playCharacter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDetail();
            }
        });

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseDetail();
            }
        });
        return view;
    }

    private void OpenDetail() {
        content_detail.setVisibility(View.VISIBLE);
        btn_close.setVisibility(View.VISIBLE);

        //todo 리스트에 넣기
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<Integer, RE_GameDetail> item : ((act_user) getActivity()).lst_GameDetail.entrySet()) {
            if (item.getKey() == gameId) {
                sb.append("Selected GameID : " + gameId + "\n");
                for (UserGame game : item.getValue().userGames) {
                    //todo 게임 상세 정보 보기 구현 (팝업창 하나 만들어 두고 선택한 게임에 대한 정보 보이기)-scv_GameDetail
                    sb.append(game.nickname + "\n");
                }
            }
        }
        Log.d("Open Detail", sb.toString());
    }


    private void CloseDetail() {
        content_detail.setVisibility(View.GONE);
        btn_close.setVisibility(View.GONE);
        content_detail.removeAllViews();
    }

}
