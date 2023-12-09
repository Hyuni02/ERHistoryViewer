package com.example.erhistoryviewer;

import android.annotation.SuppressLint;
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

public class frg_userinfo extends Fragment {

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frg_userinfo, container, false);
        TextView txt_userName = view.findViewById(R.id.txt_username);
        TextView txt_rank = view.findViewById(R.id.txt_userrank);
        TextView txt_kda = view.findViewById(R.id.txt_userkda);
        ImageView img_character = view.findViewById(R.id.img_usercharacter);

        txt_userName.setText("# " + getArguments().getString("username"));
        txt_kda.setText(getArguments().getString("kda"));
        txt_rank.setText(Integer.toString(getArguments().getInt("userrank")));
        try {
            img_character.setImageResource(getArguments().getInt("resourceid"));
        }
        catch (Exception ex){
            Log.e("resource not found", Integer.toString(getArguments().getInt("resourceid")));
        }
        img_character.setOnClickListener(v -> ((act_user)getActivity()).Request_UserNum(getArguments().getString("username")));

        return view;
    }
}
