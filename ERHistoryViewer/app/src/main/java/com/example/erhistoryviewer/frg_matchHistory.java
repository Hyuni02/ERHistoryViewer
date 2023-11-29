package com.example.erhistoryviewer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class frg_matchHistory extends Fragment {
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    frg_rank_his frg_rank_his;
    frg_casual_his frg_casual_his;
    frg_cobalt_his frg_cobalt_his;

    UserActivity.Selected_Match selected_match;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentManager = getActivity().getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        frg_rank_his = new frg_rank_his();
        frg_casual_his = new frg_casual_his();
        frg_cobalt_his = new frg_cobalt_his();

        fragmentTransaction.add(R.id.content_matchhistory,frg_rank_his).commit();

        return inflater.inflate(R.layout.frg_matchhistory, container, false);
    }

    public void ChangeFrag(UserActivity.Selected_Match match){
        fragmentManager = getActivity().getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        switch (match){
            case rank:
                fragmentTransaction.replace(R.id.content_matchhistory,frg_rank_his).commit();
                break;
            case casual:
                fragmentTransaction.replace(R.id.content_matchhistory,frg_casual_his).commit();
                break;
            case cobalt:
                fragmentTransaction.replace(R.id.content_matchhistory, frg_cobalt_his).commit();
                break;
        }
    }
}
