package com.example.erhistoryviewer;

import android.media.MediaMetadataRetriever;

import java.time.LocalDate;

public class GraphPoint {
    int seasonId;
    LocalDate date;
    int mmr;

    public GraphPoint(Integer seasonId, LocalDate date, Integer mmr){
        this.seasonId = seasonId;
        this.date = date;
        this.mmr = mmr;
    }

    public LocalDate getDate(){
        return date;
    }
    public Integer getSeasoonId(){
        return seasonId;
    }
    public Integer getMMR(){
        return mmr;
    }
    public void setMMR(Integer newMMR){
        mmr = newMMR;
    }
}
