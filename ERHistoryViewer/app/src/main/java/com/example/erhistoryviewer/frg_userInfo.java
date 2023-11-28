package com.example.erhistoryviewer;

import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class frg_userInfo extends Fragment {
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    frg_rank frg_rank;
    frg_casual frg_casual;
    frg_cobalt frg_cobalt;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        fragmentManager = getActivity().getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        frg_rank = new frg_rank();
        frg_casual = new frg_casual();
        frg_cobalt = new frg_cobalt();

        fragmentTransaction.add(R.id.content_userinfo,frg_rank).commit();

        return inflater.inflate(R.layout.frg_userinfo, container, false);
    }

    public void ChangeFrag(UserActivity.Selected_Match match){
        fragmentManager = getActivity().getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        switch (match){
            case rank:
                fragmentTransaction.replace(R.id.content_userinfo,frg_rank).commit();
                break;
            case casual:
                fragmentTransaction.replace(R.id.content_userinfo,frg_casual).commit();
                break;
            case cobalt:
                fragmentTransaction.replace(R.id.content_userinfo, frg_cobalt).commit();
                break;
        }
    }
}
