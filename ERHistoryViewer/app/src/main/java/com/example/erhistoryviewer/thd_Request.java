package com.example.erhistoryviewer;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
            handler.post(new Runnable() {
                @Override
                public void run() {
                    SetSpnnierSeason();
                }
            });
            PrintMMRS(lastPlaySeasonId);
            SetInfo(lastPlaySeasonId);


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

    List<GraphPoint> points = new ArrayList<>();
    ArrayList<UserGame> lst_UserGames = new ArrayList<>();

    private int Request_UserGame() {
        Log.d("Request", "UserGame");
        String response_UserGame = requester.Get(next == 0 ?
                "https://open-api.bser.io/v1/user/games/" + userNum
                : "https://open-api.bser.io/v1/user/games/" + userNum + "?next=" + next);
        RE_UserGame userGame = converter.Convert_UserGame(response_UserGame);
        next = userGame.next;

        for (int i = 0; i < userGame.userGames.size(); i++) {
            UserGame game = userGame.userGames.get(i);
            lst_UserGames.add(game);
            switch (game.matchingMode) {
                //랭크게임
                case 3:
                    //todo 대전기록-랭크 패널에 대전기록 추가 (선택한 시즌에 맞춰 visible/gone)

                    //날짜/시즌별 mmr획득량 기록 <시즌, 날짜, mmr>
                    LocalDate date = LocalDate.parse(game.startDtm.split("T")[0]);
                    GraphPoint sameDate = hasDate(date);
                    if (sameDate == null) {
                        GraphPoint point = new GraphPoint(game.seasonId, date, game.mmrAfter);
                        points.add(point);
                        Log.d("Add MMR", date + " : " + game.mmrAfter);
                    }
                    break;
                //일반게임
                case 2:
                    //todo 대전기록-일반 패널에 대전기록 추가

                    //날짜를 이용해서 시즌 계산 seasonId 수정
                    game.seasonId = GetNormalSeasonId(game.startDtm);

                    //todo (선택한 시즌에 맞춰 visible/gone)

                    break;
                //코발트
                case 6:
                    //todo 코발트 대전기록 표시
                    game.seasonId = GetNormalSeasonId(game.startDtm);

                    break;
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < userGame.userGames.size(); i++) {
            stringBuilder.append(userGame.userGames.get(i).gameId
                    + " (" + userGame.userGames.get(i).matchingMode + ") "
                    + userGame.userGames.get(i).startDtm
                    + "\n");
        }
        Log.d("UserGame", stringBuilder.toString());
        level = Math.max(userGame.userGames.get(0).accountLevel, level);
        return userGame.userGames.get(0).seasonId;
    }

    private GraphPoint hasDate(LocalDate date) {
        for (GraphPoint point : points) {
            if (point.getDate().isEqual(date)) {
                return point;
            }
        }
        return null;
    }

    LineData lineData;
    ArrayList<String> Dates = new ArrayList<>();
    private void PrintMMRS(int seasonId) {
        lineData = new LineData();
        ArrayList<Entry> chart = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = points.size() - 1; i >= 0; i--) {
            if (points.get(i).getSeasoonId() == seasonId) {
                chart.add(new Entry((points.size() - points.indexOf(points.get(i)) - 1), points.get(i).getMMR()));
                Dates.add(points.get(i).getDate().format(DateTimeFormatter.ofPattern("yy/MM/dd")));
                stringBuilder.append((points.size() - points.indexOf(points.get(i)) - 1) + " : " + points.get(i).getDate() + "(" + points.get(i).getSeasoonId() + ") : " + points.get(i).getMMR() + "\n");
            }
        }

        LineDataSet lineDataSet = new LineDataSet(chart, "MMR");
        lineDataSet.setColor(Color.RED);
        lineData.addDataSet(lineDataSet);

        //그래프 설정
        XAxis x = act_user.mmrGraph.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        act_user.mmrGraph.getAxisRight().setEnabled(false);
        act_user.mmrGraph.getDescription().setEnabled(false);
        act_user.mmrGraph.setTouchEnabled(true);
        act_user.mmrGraph.setDragXEnabled(true);
        act_user.mmrGraph.setVisibleXRange(1,6);
        act_user.mmrGraph.moveViewToX(lineDataSet.getEntryCount());

        //그래프 적용
        act_user.mmrGraph.setData(lineData);
        x.setValueFormatter(new IndexAxisValueFormatter(Dates));
        act_user.mmrGraph.invalidate();
        Log.d("MMRS", stringBuilder.toString());
    }
    private void SetInfo(int seasonId){
//        act_user.img_tier.setImageDrawable(MMRtoTier());
    }
    private rankInfo MMRtoTier(int mmr, int rank){
        for(tierIndex tier : act_user.TierIndex){
            if(tier.top <= mmr && tier.bottom >= mmr){
                rankInfo rankInfo = new rankInfo();
                rankInfo.name = tier.tierName;
                switch (tier.tierName){
                    case "아이언IV":
                    case "아이언III":
                    case "아이언II":
                    case "아이언I":
                        rankInfo.image = act_user.getResources().getDrawable(R.drawable.img_iron);
                        break;
                    case "브론즈IV":
                    case "브론즈III":
                    case "브론즈II":
                    case "브론즈I":
                        rankInfo.image = act_user.getResources().getDrawable(R.drawable.img_bronze);
                        break;
                    case "실버IV":
                    case "실버III":
                    case "실버II":
                    case "실버I":
                        rankInfo.image = act_user.getResources().getDrawable(R.drawable.img_silver);
                        break;
                    case "골드IV":
                    case "골드III":
                    case "골드II":
                    case "골드I":
                        rankInfo.image = act_user.getResources().getDrawable(R.drawable.img_gold);
                        break;
                    case "플레티넘IV":
                    case "플레티넘III":
                    case "플레티넘II":
                    case "플레티넘I":
                        rankInfo.image = act_user.getResources().getDrawable(R.drawable.img_platinum);
                        break;
                    case "다이아몬드IV":
                    case "다이아몬드III":
                    case "다이아몬드II":
                    case "다이아몬드I":
                        rankInfo.image = act_user.getResources().getDrawable(R.drawable.img_diamond);
                        break;
                    case "미스릴":
                        rankInfo.image = act_user.getResources().getDrawable(R.drawable.img_mythril);
                        break;
                }
                if(rank <= 700){
                    rankInfo.image = act_user.getResources().getDrawable(R.drawable.img_demigod);
                }
                if(rank <= 200){
                    rankInfo.image = act_user.getResources().getDrawable(R.drawable.img_eternity);
                }
                return rankInfo;
            }
        }
        Log.d("Error", "Can't Find Tier " + mmr);
        return null;
    }

    List<String> lst_Season = new ArrayList<>();
    int selected_seasonId = -1;

    private void SetSpnnierSeason() {
        lst_Season.clear();
        //플레이 기록이 있는 시즌만 추가하기
        for (UserGame game : lst_UserGames) {
            if (!lst_Season.contains(SeasonIdtoName(game.seasonId))) {
                lst_Season.add(SeasonIdtoName(game.seasonId));
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                act_user.getApplicationContext(),
                android.R.layout.simple_spinner_item,
                lst_Season);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        act_user.spn_seasons.setAdapter(adapter);

        act_user.spn_seasons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                data_Season s = re_season.data.get(re_season.data.size() - 1 - position);
                selected_seasonId = s.seasonID;
                PrintMMRS(selected_seasonId);
                SetInfo(selected_seasonId);
                Log.d("Season Name", s.seasonName + "/" + s.seasonID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private String SeasonIdtoName(int seasonId) {
        for (data_Season season : re_season.data) {
            if (season.seasonID == seasonId) {
                return season.seasonName;
            }
        }
        Log.e("Error", "Can't find SeasonName " + seasonId);
        return null;
    }

    private int GetNormalSeasonId(String date) {
        for (int i = re_season.data.size() - 1; i >= 0; i--) {
            data_Season season = re_season.data.get(i);
            LocalDate date_seasonEnd = LocalDate.parse(season.seasonEnd.split(" ")[0]);
            LocalDate date_seasonStart = LocalDate.parse(season.seasonStart.split(" ")[0]);
            LocalDate date_game = LocalDate.parse(date.split("T")[0]);
            if (date_seasonEnd.isAfter(date_game) || date_seasonEnd.isEqual(date_game)) {
                if (date_seasonStart.isBefore(date_game)) {
                    Log.d("SeasonId Found", date.split("T")[0] + "의 seasonId : " + season.seasonID);
                    return season.seasonID;
                }
            }
        }
        Log.e("SeasonId Not Found", date + "의 seasonId를 찾을 수 없음");
        return -1;
    }

    private void Request_UserStats() {
        Log.d("Request", "UserStats " + lastPlaySeasonId);
        String response_UserStat = requester.Get("https://open-api.bser.io/v1/user/stats/" + userNum + "/" + lastPlaySeasonId);
        RE_UserStats userStats = converter.Convert_UserStats(response_UserStat);
        if (userStats.code == 404) {
            lastPlaySeasonId--;
            Request_UserStats();
            return;
        }
        userName = userStats.userStats.get(0).nickname;
        mostCharacter = userStats.userStats.get(0).characterStats.get(0).characterCode;
    }

}