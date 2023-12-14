package com.example.erhistoryviewer;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class frg_freeCharacter extends Fragment {
    ImageButton btn_freeCharacter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frg_freecharacter, container, false);
        btn_freeCharacter = view.findViewById(R.id.img_freecharacter);

        btn_freeCharacter.setImageResource(getArguments().getInt("img"));

        return view;
    }
}
