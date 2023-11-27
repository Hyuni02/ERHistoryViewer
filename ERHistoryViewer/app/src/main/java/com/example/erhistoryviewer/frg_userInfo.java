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
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        fragmentManager = getActivity().getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        frg_rank = new frg_rank();

        fragmentTransaction.add(R.id.content_userinfo_rank,frg_rank).commit();

        return inflater.inflate(R.layout.frg_userinfo, container, false);
    }
}
