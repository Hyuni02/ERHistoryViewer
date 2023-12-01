package com.example.erhistoryviewer;


import android.util.Log;
import android.os.Handler;

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

    private void Init(){
        userNum = act_user.userNum;
    }

    public void run() {
        Log.i("시작된 스레드", thdName);
        // 스레드에게 수행시킬 동작들 구현
        try {
            Thread.sleep(1000);

            Request_Season();
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

            handler.post(new Runnable() {
                @Override
                public void run() {
                    act_user.txt_level.setText("LV " + level);
                    act_user.txt_nickname.setText(userName);
                }
            });


            //next가 있으면 5회 || next가 없을 때까지 대전기록 가져오기
            if(next != 0) {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(1000);
                    Request_UserGame();
                    Log.d("next", Integer.toString(next));
                    if(next == 0){
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

    private void Request_Season() {
        Log.d("Request", "Season");
        String response_Season = requester.Get("https://open-api.bser.io/v2/data/Season");
        RE_Season season = converter.Convert_Season(response_Season);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < season.data.size(); i++) {
            stringBuilder.append("(" + season.data.get(i).seasonID + ")\t" + season.data.get(i).seasonName + "\n");
        }
        Log.d("Season", stringBuilder.toString());
    }

    private int Request_UserGame() {
        Log.d("Request", "UserGame");
        String response_UserGame = requester.Get(next == 0 ?
                "https://open-api.bser.io/v1/user/games/" + userNum
                : "https://open-api.bser.io/v1/user/games/" + userNum + "?next=" + next);
        RE_UserGame userGame = converter.Convert_UserGame(response_UserGame);
        next = userGame.next;
        //todo 대전 기록 표시하기
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < userGame.userGames.size(); i++) {
            stringBuilder.append(userGame.userGames.get(i).gameId + "\n");
        }
        Log.d("UserGame", stringBuilder.toString());
        level = Math.max(userGame.userGames.get(0).accountLevel, level);
        return userGame.userGames.get(0).seasonId;
    }

    private void Request_UserStats() {
        Log.d("Request", "UserStats");
        String response_UserStat = requester.Get("https://open-api.bser.io/v1/user/stats/" + userNum + "/" + lastPlaySeasonId);
        RE_UserStats userStats = converter.Convert_UserStats(response_UserStat);
        userName = userStats.userStats.get(0).nickname;
        mostCharacter = userStats.userStats.get(0).characterStats.get(0).characterCode;
        //todo 이름, 레벨, 모스트 캐릭터 사진 넣기
    }

}