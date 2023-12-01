package com.example.erhistoryviewer;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.os.Handler;
import android.view.GestureDetector;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class thd_Request extends Thread {
    private String thdName = "";

    private int next = 0;
    private int level = 0;
    private int mostCharacter = 0;
    private String userName;
    private int lastPlaySeasonId = -1;
    String userNum = "";
    Converter converter;
    Requester requester;

    RE_Season re_season;

    act_user act_user;
    Handler handler = new Handler();

    public thd_Request(String thdname, act_user act_user) {
        // 초기화 작업
        this.thdName = thdname;
        requester = new Requester();
        converter = new Converter();
        this.act_user = act_user;
        Init();
    }

    private void Init() {
        userNum = act_user.userNum;
    }

    public void run() {
        Log.i("시작된 스레드", thdName);
        // 스레드에게 수행시킬 동작들 구현
        try {
            Thread.sleep(1000);

            re_season = Request_Season();
            Thread.sleep(1000);

            //유저 게임 기록 1개 불러오기
            lastPlaySeasonId = Request_UserGame();
            if (lastPlaySeasonId == 0) {
                //todo 일반전일 경우 날짜를 계산해서 seasonId 변경

            }
            Thread.sleep(1000);
            Log.d("next", Integer.toString(next));

            //첫번째 게임 기록의 시즌을 가져와서 스탯 검색
            Request_UserStats();
            Log.d("Last Play Season", Integer.toString(lastPlaySeasonId));
            Log.d("Level", Integer.toString(level));
            Log.d("NickName", userName);
            Log.d("Most Character", Integer.toString(mostCharacter));
            Thread.sleep(1000);
            Request_MostCharacterImage();
            //UI스래드 접근
            handler.post(new Runnable() {
                @Override
                public void run() {
                    act_user.txt_level.setText("LV " + level);
                    act_user.txt_nickname.setText(userName);
                    act_user.img_mostcharacter.setImageBitmap(bitmap);
                }
            });

            //next가 있으면 5회 || next가 없을 때까지 대전기록 가져오기
            if (next != 0) {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(1000);
                    Request_UserGame();
                    Log.d("next", Integer.toString(next));
                    if (next == 0) {
                        Log.d("Break", "No More Games");
                        break;
                    }
                }
            }


            Log.d("done", "done");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.i("종료된 스레드", thdName);
    }

    Bitmap bitmap;

    private void Request_MostCharacterImage() {
        Log.d("Request", "MostCharacterImage");
        String charName = act_user.CharacterCodetoName(mostCharacter);
        String skinCode = "S000"; //todo 가장 많이 사용한 스킨 찾기 구현
        String url_dak = String.format("https://cdn.dak.gg/assets/er/game-assets/1.9.0/CharResult_%s_%s.png", charName, skinCode);
        try {
            URL url = new URL(url_dak);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // 서버로부터 응답 수신
            conn.connect(); //연결된 곳에 접속할 때 (connect() 호출해야 실제 통신 가능함)
            InputStream is = conn.getInputStream(); //inputStream 값 가져오기
            bitmap = BitmapFactory.decodeStream(is); // Bitmap으로 변환
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private RE_Season Request_Season() {
        Log.d("Request", "Season");
        String response_Season = requester.Get("https://open-api.bser.io/v2/data/Season");
        RE_Season season = converter.Convert_Season(response_Season);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < season.data.size(); i++) {
            stringBuilder.append("(" + season.data.get(i).seasonID + ")\t" + season.data.get(i).seasonName + "\n");
        }
        Log.d("Season", stringBuilder.toString());
        return season;
    }

    private int Request_UserGame() {
        Log.d("Request", "UserGame");
        String response_UserGame = requester.Get(next == 0 ?
                "https://open-api.bser.io/v1/user/games/" + userNum
                : "https://open-api.bser.io/v1/user/games/" + userNum + "?next=" + next);
        RE_UserGame userGame = converter.Convert_UserGame(response_UserGame);
        next = userGame.next;

        //todo 대전 기록 표시하기
        for (int i = 0; i < userGame.userGames.size(); i++) {
            UserGame game = userGame.userGames.get(i);
            switch (game.matchingMode) {
                //랭크게임
                case 3:
                    //대전기록-랭크 패널에 대전기록 추가 (선택한 시즌에 맞춰 visible/gone)

                    //mmr획득량을 딕셔너리에 기록 <날짜, mmr>

                    //mmr획득량을 그래프로 표시
                    break;
                //일반게임
                case 2:
                    //대전기록-일반 패널에 대전기록 추가

                    //날짜를 이용해서 시즌 계산 seasonId 수정
                    game.seasonId = GetNormalSeasonId(game.startDtm);

                    //(선택한 시즌에 맞춰 visible/gone)
                    break;
                //코발트
                case 6:
                    //todo 코발트 대전기록 표시
                    break;
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < userGame.userGames.size(); i++) {
            stringBuilder.append(userGame.userGames.get(i).gameId + "\n");
        }
        Log.d("UserGame", stringBuilder.toString());
        level = Math.max(userGame.userGames.get(0).accountLevel, level);
        return userGame.userGames.get(0).seasonId;
    }

    private int GetNormalSeasonId(String date) {
        for (int i = re_season.data.size() - 1; i >= 0; i--) {
            data_Season season = re_season.data.get(i);
            LocalDate date_seasonEnd = LocalDate.parse(season.seasonEnd.split(" ")[0]);
            LocalDate date_seasonStart = LocalDate.parse(season.seasonStart.split(" ")[0]);
            LocalDate date_game = LocalDate.parse(date.split("T")[0]);
            if(date_seasonEnd.isAfter(date_game)){
                if(date_seasonStart.isBefore(date_game)){
                    Log.d("SeasonId Found", date.split("T")[0] + "의 seasonId : " + season.seasonID);
                    return season.seasonID;
                }
            }
        }
        Log.e("SeasonId Not Found",date + "의 seasonId를 찾을 수 없음");
        return -1;
    }

    private void Request_UserStats() {
        Log.d("Request", "UserStats " + lastPlaySeasonId);
        String response_UserStat = requester.Get("https://open-api.bser.io/v1/user/stats/" + userNum + "/" + lastPlaySeasonId);
        RE_UserStats userStats = converter.Convert_UserStats(response_UserStat);
        if(userStats.code == 404){
            lastPlaySeasonId--;
            Request_UserStats();
            return;
        }
        userName = userStats.userStats.get(0).nickname;
        mostCharacter = userStats.userStats.get(0).characterStats.get(0).characterCode;
    }

}