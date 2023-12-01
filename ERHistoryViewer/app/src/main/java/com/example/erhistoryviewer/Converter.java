package com.example.erhistoryviewer;

import com.google.gson.Gson;

public class Converter {
    public RE_UserNum Convert_UserNum(String response){
        Gson gson = new Gson();
        RE_UserNum re = gson.fromJson(response, RE_UserNum.class);
        return re;
    }

    public RE_Season Convert_Season(String response){
        Gson gson = new Gson();
        RE_Season re = gson.fromJson(response, RE_Season.class);
        return re;
    }

    public RE_UserGame Convert_UserGame(String response){
        Gson gson = new Gson();
        RE_UserGame re = gson.fromJson(response, RE_UserGame.class);
        return re;
    }

    public RE_UserStats Convert_UserStats(String response){
        Gson gson = new Gson();
        RE_UserStats re = gson.fromJson(response, RE_UserStats.class);
        return re;
    }
}
