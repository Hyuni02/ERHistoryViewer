package com.example.erhistoryviewer;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class thd_RequestFreeCharacter extends Thread{
    final String thdname;
    act_lobby act_lobby;
    Requester requester;
    Converter converter;


    public thd_RequestFreeCharacter(String thdname, act_lobby act_lobby){
        this.thdname = thdname;
        this.act_lobby = act_lobby;
        requester = new Requester();
        converter = new Converter();
    }

    public void run(){
        Log.i("시작된 스레드", thdname);
        try{
            Request_FreeCharacter();
        }
        catch (Exception ex){
            Log.e("error",ex.toString());
        }

    }



    private void Request_FreeCharacter(){
        Log.d("Request", "Free Character");
        String response_Season = requester.Get("https://open-api.bser.io/v1/freeCharacters/2");
        RE_FreeCharacter freeCharacter = converter.Convert_FreeCharacter(response_Season);
        StringBuilder stringBuilder = new StringBuilder();
        //todo 로비에 표시하기
        for( int i=0;i<freeCharacter.freeCharacters.size();i++){
            frg_freeCharacter frg_freeCharacter = new frg_freeCharacter();
            Bundle bundle = new Bundle();
            int imgcode = act_lobby.dic_charactercoderesourceid.get(freeCharacter.freeCharacters.get(i));
            bundle.putInt("img",imgcode);
            frg_freeCharacter.setArguments(bundle);
            act_lobby.fragmentTransaction.add(act_lobby.layout_freeCharacter.getId(), frg_freeCharacter);
        }
        act_lobby.fragmentTransaction.commit();
        for (int i = 0; i < freeCharacter.freeCharacters.size(); i++) {
            stringBuilder.append("(" + freeCharacter.freeCharacters.get(i) + ")\t" + act_lobby.CharacterCodetoName(freeCharacter.freeCharacters.get(i)) + "\n");
        }
        Log.d("Free Character", stringBuilder.toString());
    }

}
