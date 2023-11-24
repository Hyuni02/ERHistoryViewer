package com.example.erhistoryviewer;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserActivity extends AppCompatActivity {

    EditText edt_userName;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_user);

        ImageButton btn_tolobby = findViewById(R.id.btn_tolobby);
        ImageButton btn_search = findViewById(R.id.btn_search);
        edt_userName = findViewById(R.id.edt_userName);
        ImageView img_mostcharacter = findViewById(R.id.img_mostcharacter);
        ImageView img_tier = findViewById(R.id.img_tier);
        TextView txt_level = findViewById(R.id.txt_level);
        TextView txt_nickname = findViewById(R.id.txt_nickname);

        txt_nickname.setText(getIntent().getStringExtra("nickName"));

    }
}
