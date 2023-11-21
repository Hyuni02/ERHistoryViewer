package com.example.erhistoryviewer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class UserActivity extends AppCompatActivity {
    frg_userState frg_userstate;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frg_userstate);

        frg_userstate = new frg_userState();

        getSupportFragmentManager().beginTransaction().replace(R.id.frg_userState, frg_userstate).commit();
    }
}
