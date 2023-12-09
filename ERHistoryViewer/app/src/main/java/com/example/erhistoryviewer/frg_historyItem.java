package com.example.erhistoryviewer;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class frg_historyItem extends Fragment {
    Requester requester = new Requester();
    Converter converter = new Converter();
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
                txt_kda.setText(getArguments().getString("kda"));
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

        img_playCharacter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDetail();
            }
        });

        return view;
    }

    private void OpenDetail() {
        ((act_user) getActivity()).scv_gameDetail.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request_GameDetail(gameId);
            }
        }).start();

        Log.d("Open Detail", "Selected GameID : " + gameId);
    }

    RE_GameDetail gameDetail;

    public void Request_GameDetail(int gameId) {
        act_user act_user = (act_user) getActivity();

        Log.d("Request", "Game Detail : " + gameId);
        String response_GameDetail = requester.Get("https://open-api.bser.io/v1/games/" + gameId);
        gameDetail = converter.Convert_GameDetail(response_GameDetail);

        StringBuilder stringBuilder = new StringBuilder();
        Collections.sort(gameDetail.userGames, new Comparator<UserGame>() {
            @Override
            public int compare(UserGame o1, UserGame o2) {
                return o1.gameRank - o2.gameRank;
            }
        });
        ArrayList<UserGame> lst_user = new ArrayList<>();
        for (UserGame u : gameDetail.userGames) {
            lst_user.add(u);
//            Log.d("Add user", u.gameRank + " : " + u.nickname);
        }
        Collections.sort(lst_user, ((o1, o2) -> o1.gameRank - o2.gameRank));
        act_user.fragmentTransaction = act_user.fragmentManager.beginTransaction();
        for (UserGame user : lst_user) {
            frg_userinfo frg_userinfo = new frg_userinfo();
            Bundle bundle = new Bundle();
            bundle.putString("username", user.nickname);
            bundle.putInt("userrank", user.gameRank);
            int teamKill = user.teamKill;
            int playerKill = user.playerKill;
            int playerDeaths = user.playerDeaths;
            int playerAssistant = user.playerAssistant;
            bundle.putString("kda", playerKill + "(" + teamKill + ") / " + playerDeaths + " / " + playerAssistant);
            int drawableResourceId = act_user.dic_charactercoderesourceid.get(user.characterNum);
            bundle.putInt("resourceid", drawableResourceId);

            frg_userinfo.setArguments(bundle);
            ((act_user) getActivity()).fragmentTransaction.add(R.id.layout_detail, frg_userinfo);
            stringBuilder.append(user.nickname + "\n");
        }
        ((act_user) getActivity()).fragmentTransaction.commit();


    }
}
