package com.example.erhistoryviewer;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.os.Handler;
import android.view.View;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class thd_Request extends Thread {
    private final String thdName;
    int sleeptime = 40;
    private int next = 0;
    private int level = 0;
    private int mostCharacter = 0;
    private String userName = "";
    private int currentSeasonId = -1;
    String userNum = "";
    Converter converter;
    Requester requester;
    RE_Season re_season;
    act_user act_user;
    Handler handler = new Handler();
    RE_UserStats userStat_rank;
    userStats userStat_normal;
    userStats userStat_cobalt;
    List<GraphPoint> points = new ArrayList<>();
    List<GraphPoint> points_pred = new ArrayList<>();
    ArrayList<UserGame> lst_UserGames_rank = new ArrayList<>();
    ArrayList<UserGame> lst_UserGames_normal = new ArrayList<>();
    ArrayList<UserGame> lst_UserGames_cobalt = new ArrayList<>();
    String folder_name;
    String filename;
    String serverURL = "http://49.50.164.182:8080";

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
            Thread.sleep(sleeptime);

            //시즌 데이터 받아오기
            re_season = Request_Season();
            //현재 시즌 찾기
            for (data_Season data : re_season.data) {
                if (data.isCurrent == 1) {
                    currentSeasonId = data.seasonID;
                    Log.d("Current Season", data.seasonName + "(" + currentSeasonId + ")");
                    break;
                }
            }
            Thread.sleep(sleeptime);

            //유저 게임 기록 많이 받아오기
            for (int i = 0; i < 20; i++) {
                Thread.sleep(sleeptime);
                int seasonIdfromGame = Request_UserGame();
                Log.d("next", Integer.toString(next));
                if (next == 0) {
                    Log.d("Break", "No More Games");
                    break;
                }
                if (seasonIdfromGame < currentSeasonId) {
                    Log.d("Break", "Not Current Season Data");
                    break;
                }
            }

            //현 시즌 유저 정보 받아오기(랭크)
            Request_UserStats(currentSeasonId);
            Thread.sleep(sleeptime);
            //유저의 일반 정보 받아오기
            Request_UserStats(0);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    PrintMMRS(currentSeasonId);
                    SetInfo_main(currentSeasonId);
                    SetInfo_Match();
                }
            });

            //대전기록 표시
            SetHistoryItem(lst_UserGames_rank, R.id.content_history_rank, "rank");
            SetHistoryItem(lst_UserGames_normal, R.id.content_history_normal, "normal");
            SetHistoryItem(lst_UserGames_cobalt, R.id.content_history_cobalt, "cobalt");

            handler.post(new Runnable() {
                @Override
                public void run() {
                    act_user.layout_loading.setVisibility(View.GONE);
                }
            });

            folder_name = act_user.getApplicationContext().getFilesDir().getPath().toString();
            filename = folder_name + "/mmrRaw.csv";

            MakeCSVFile();

            boolean printlog = false;
            if(printlog) {
                StringBuilder sb;

                //랭크게임 목록
                sb = new StringBuilder();
                for (UserGame userGame : lst_UserGames_rank) {
                    sb.append(userGame.gameId + " (" + userGame.seasonId + ") [" + userGame.matchingMode + "] " + userGame.startDtm + "\n");
                }
                Log.d("Games_rank", sb.toString());

                //일반게임 목록
                sb = new StringBuilder();
                for (UserGame userGame : lst_UserGames_normal) {
                    sb.append(userGame.gameId + " (" + userGame.seasonId + ") [" + userGame.matchingMode + "] " + userGame.startDtm + "\n");
                }
                Log.d("Games_normal", sb.toString());

                //코발트게임 목록
                sb = new StringBuilder();
                for (UserGame userGame : lst_UserGames_cobalt) {
                    sb.append(userGame.gameId + " (" + userGame.seasonId + ") [" + userGame.matchingMode + "] " + userGame.startDtm + "\n");
                }
                Log.d("Games_cobalt", sb.toString());

                //(시즌id, 날짜, mmr) 목록
                sb = new StringBuilder();
                for (GraphPoint point : points) {
                    sb.append(point.getDate() + " : " + point.getMMR() + "\n");
                }
                Log.d("MMR", sb.toString());
            }


            Log.d("done", "done");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("종료된 스레드", thdName);
    }

    private Drawable Get_MostCharacterImage(int characterCode) {
        String charName = act_user.CharacterCodetoName(characterCode).toLowerCase();
        String skinCode = "S000"; //todo 가장 많이 사용한 스킨 찾기 구현
        int drawableResourceId = act_user.getResources().getIdentifier(charName, "drawable", act_user.getPackageName());
        Drawable img = act_user.getResources().getDrawable(drawableResourceId);
        return img;
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

        for (int i = 0; i < userGame.userGames.size(); i++) {
            UserGame game = userGame.userGames.get(i);
            switch (game.matchingMode) {
                //랭크게임
                case 3:
                    if (game.seasonId != currentSeasonId) {
                        continue;
                    }
                    //날짜/시즌별 mmr획득량 기록 <시즌, 날짜, mmr>
                    LocalDate date = LocalDate.parse(game.startDtm.split("T")[0]);
                    GraphPoint sameDate = hasDate(date);
                    if (sameDate == null) {
                        GraphPoint point = new GraphPoint(game.seasonId, date, game.mmrAfter);
                        points.add(point);
                        Log.d("Add MMR", date + " : " + game.mmrAfter);
                    }
                    lst_UserGames_rank.add(game);
                    break;
                //일반게임
                case 2:
                    //날짜를 이용해서 시즌 계산 seasonId 수정
                    game.seasonId = GetNormalSeasonId(game.startDtm);
                    if (game.seasonId != currentSeasonId) {
                        continue;
                    }
                    lst_UserGames_normal.add(game);
                    break;
                //코발트
                case 6:
                    game.seasonId = GetNormalSeasonId(game.startDtm);
                    if (game.seasonId != currentSeasonId) {
                        continue;
                    }
                    lst_UserGames_cobalt.add(game);
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
        if (userName == "") {
            userName = userGame.userGames.get(0).nickname;
        }
        return userGame.userGames.get(0).seasonId;
    }

    private void SetHistoryItem(ArrayList<UserGame> lst, int containerId, String type) {
        act_user.fragmentTransaction = act_user.fragmentManager.beginTransaction();
        for (UserGame game : lst) {
            frg_historyItem frg_historyitem = new frg_historyItem();

            Bundle bundle = new Bundle();
            bundle.putString("matchmode", type);

            int drawableResourceId = act_user.dic_charactercoderesourceid.get(game.characterNum);
            bundle.putInt("img", drawableResourceId);
            bundle.putInt("code", game.characterNum);
            bundle.putInt("gameId", game.gameId);
            bundle.putString("rank", Integer.toString(game.gameRank));
            bundle.putString("date", game.startDtm.split("T")[0]);
            int teamKill = game.teamKill;
            int playerKill = game.playerKill;
            int playerDeaths = game.playerDeaths;
            int playerAssistant = game.playerAssistant;
            bundle.putString("kda", playerKill + "(" + teamKill + ") / " + playerDeaths + " / " + playerAssistant);
            bundle.putString("dmg", Integer.toString(game.damageToPlayer));

            frg_historyitem.setArguments(bundle);
            act_user.fragmentTransaction.add(containerId, frg_historyitem);
//            Log.d("Fragment", game.gameId + " : " + game.gameRank);
        }
        act_user.fragmentTransaction.commit();
    }

    private GraphPoint hasDate(LocalDate date) {
        for (GraphPoint point : points) {
            if (point.getDate().isEqual(date)) {
                return point;
            }
        }
        return null;
    }

    private void MakeCSVFile() {
        File newFolder = new File(folder_name);
        try {
            newFolder.mkdir();
            Log.d("폴더생성", "폴더생성 성공");
        } catch (Exception e) {
            Log.d("폴더생성", "폴더생성이 이미 되어있거나 실패");
        }

        //전송할 데이터가 담긴 파일 생성
        File file = new File(filename);
        try {
            Log.d("파일생성 : ", filename);
            FileOutputStream fos = new FileOutputStream(file);
            StringBuilder data = new StringBuilder();
            //1번째 줄에는 0, 시즌종료일-첫게임날짜 = 시즌 종료까지 남은 날짜
            LocalDate fin = LocalDate.parse(re_season.data.get(currentSeasonId).seasonEnd.split(" ")[0]); //시즌종료일
            LocalDate start = points.get(points.size() - 1).date; //첫 게임 날짜
            long datediff = ChronoUnit.DAYS.between(start, fin);
            data.append("0," + datediff);
            //2번째 줄 부터는 첫게임 날짜-게임날짜, mmrAfter
            for (UserGame game : lst_UserGames_rank) {
                fin = LocalDate.parse(game.startDtm.split("T")[0]);
                datediff = ChronoUnit.DAYS.between(start, fin);
                data.append("\n" + datediff + "," + game.mmrAfter);
            }
//            Log.d("Graph Points", data.toString());
            fos.write(data.toString().getBytes());
            fos.close(); //스트림 닫기
            UploadFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    RE_xy xy;

    private void UploadFile() {
        Log.d("Upload file", "try file upload");
        try {
            File uploadFile = new File(filename);
            String boundary = Long.toHexString(System.currentTimeMillis());
            String CRLF = "\r\n";
            HttpURLConnection connection = (HttpURLConnection) new URL(serverURL + "/upload").openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream output = connection.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + uploadFile.getName() + "\"").append(CRLF);
                writer.append("Content-Type: text/csv").append(CRLF);
                writer.append(CRLF).flush();

                try (FileInputStream inputStream = new FileInputStream(uploadFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                    output.flush();
                }

                writer.append(CRLF).flush();
                writer.append("--" + boundary + "--").append(CRLF);
            }


            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    Log.d("file upload", response.toString());
                    if(response.toString().contentEquals("Need More then 10 Games".toString())){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                act_user.txt_nopred.setVisibility(View.VISIBLE);
                                act_user.txt_nopred.setText("경쟁전 기록이 부족합니다.\n 10건 이상의 기록이 필요합니다.");
                                act_user.mmrGraph_pred.setVisibility(View.GONE);
                                Log.d("Error", response.toString());
                            }
                        });
                    }else{
                        xy = converter.Convert_xy(response.toString());
                        XtoDate();
                    }
                }
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        act_user.txt_nopred.setVisibility(View.VISIBLE);
                        act_user.txt_nopred.setText("기록 분석 서버에 연결 실패하였습니다.\n재시도 하려면 새로고침 하싶시오.");
                        act_user.mmrGraph_pred.setVisibility(View.GONE);
                        Log.e("file upload fail", Integer.toString(responseCode));
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("file upload fail", e.toString());
            UploadFile();
        }
    }

    private void XtoDate() {
        LocalDate start = points.get(points.size() - 1).date;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < xy.x.size(); i++) {
            LocalDate date = start.plusDays(xy.x.get(i));
            GraphPoint ppoint = new GraphPoint(21, date, xy.y.get(i));
            points_pred.add(ppoint);
            stringBuilder.append(ppoint.getDate() + " : " + ppoint.getMMR() + "\n");
        }
//        Log.d("predicted points", stringBuilder.toString());
        PrintMMRS_pred(currentSeasonId);
    }

    private void PrintMMRS_pred(int seasonId){
        LineData lineData = new LineData();
        ArrayList<Entry> chart = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<String> Dates = new ArrayList<>();

        for (int i = 0; i < points_pred.size(); i++) {
            if (points_pred.get(i).getSeasoonId() == seasonId) {
                chart.add(new Entry(i, points_pred.get(i).getMMR()));
                Dates.add(points_pred.get(i).getDate().format(DateTimeFormatter.ofPattern("yy/MM/dd")));
                stringBuilder.append(i + " : " + points_pred.get(i).getDate() + "(" + points_pred.get(i).getSeasoonId() + ") : " + points_pred.get(i).getMMR() + "\n");
            }
        }

        LineDataSet lineDataSet = new LineDataSet(chart, "Predicted MMR");
        lineDataSet.setColor(Color.GREEN);
        lineData.addDataSet(lineDataSet);

        //그래프 설정
        XAxis x = act_user.mmrGraph_pred.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        act_user.mmrGraph_pred.getAxisRight().setEnabled(false);
        act_user.mmrGraph_pred.getDescription().setEnabled(false);
        act_user.mmrGraph_pred.setTouchEnabled(true);
        act_user.mmrGraph_pred.setDragXEnabled(true);
        act_user.mmrGraph_pred.setVisibleXRange(1, 5);
        act_user.mmrGraph_pred.setVisibleXRangeMaximum(5);
        act_user.mmrGraph_pred.moveViewToX(lineDataSet.getEntryCount());
        //그래프 적용
        act_user.mmrGraph_pred.setData(lineData);
        x.setValueFormatter(new IndexAxisValueFormatter(Dates));
        act_user.mmrGraph_pred.invalidate();
        Log.d("MMRS_pred", stringBuilder.toString());
    }

    private void PrintMMRS(int seasonId) {
        LineData lineData = new LineData();
        ArrayList<Entry> chart = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<String> Dates = new ArrayList<>();

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
        act_user.mmrGraph.setVisibleXRange(1, 5);
        act_user.mmrGraph.setVisibleXRangeMaximum(5);
        act_user.mmrGraph.moveViewToX(lineDataSet.getEntryCount());
        //그래프 적용
        act_user.mmrGraph.setData(lineData);
        x.setValueFormatter(new IndexAxisValueFormatter(Dates));
        act_user.mmrGraph.invalidate();
        Log.d("MMRS", stringBuilder.toString());
    }

    private void SetInfo_main(int seasonId) {
        act_user.txt_level.setText("LV " + level);
        act_user.txt_nickname.setText(userName);
        if (userStat_rank.code == 404) {
            act_user.img_mostcharacter.setImageDrawable(act_user.getResources().getDrawable(R.drawable.mostcharacter));
            Log.d("Set Info Main", "No data" + SeasonIdtoName(seasonId) + "(" + seasonId + ")");
        } else {
            act_user.img_mostcharacter.setImageDrawable(Get_MostCharacterImage(userStat_rank.userStats.get(0).characterStats.get(0).characterCode));
            Log.d("Set Info Main", "Lv " + level + "\nNickname : " + userName + "\nMostCharacter : " + act_user.CharacterCodetoName(userStat_rank.userStats.get(0).characterStats.get(0).characterCode) + "(" + userStat_rank.userStats.get(0).characterStats.get(0).characterCode + ")");
        }
    }

    private void SetInfo_Match() {
        //랭크
        if (userStat_rank.code != 404) {
            int mmr = userStat_rank.userStats.get(0).mmr;
            int rank = userStat_rank.userStats.get(0).rank;
            Log.d("MMR Rank", "MMR : " + mmr + " Rank : " + rank);
            rankInfo rankInfo = MMRtoTier(mmr, rank);
            act_user.img_tier.setImageDrawable(rankInfo.image);
            act_user.txt_mmr.setText(userStat_rank.userStats.get(0).mmr + "RP");
            act_user.txt_tier.setText("티어 - " + rankInfo.name);
            act_user.txt_rank.setText("순위 " + userStat_rank.userStats.get(0).rank + "위");
        } else {
            act_user.img_tier.setImageDrawable(act_user.getResources().getDrawable(R.drawable.img_unrank));
            act_user.txt_mmr.setText("정보 없음");
            act_user.txt_tier.setText("언랭크");
            act_user.txt_rank.setText("");
        }
        //일반
        act_user.txt_gameCount.setText("게임 수 " + lst_UserGames_normal.size());
        if (lst_UserGames_normal.size() == 0) {
            act_user.txt_avgRank.setText("데이터 없음");
            act_user.txt_winRate.setText("");
        } else {
            act_user.txt_avgRank.setText("평균 순위 " + String.format("%.1f", GetAvgRank(lst_UserGames_normal)) + "위");
            act_user.txt_winRate.setText("승률 " + String.format("%.1f", GetWinRate(lst_UserGames_normal)) + "%");
        }
        //코발트
        act_user.txt_gameCount_cobalt.setText("게임 수 " + lst_UserGames_cobalt.size());
        if (lst_UserGames_cobalt.size() == 0) {
            act_user.txt_avgDmg.setText("데이터 없음");
            act_user.txt_winRate_cobalt.setText("");
        } else {
            act_user.txt_avgDmg.setText("평균 딜량 " + GetAvgDmg(lst_UserGames_cobalt));
            act_user.txt_winRate_cobalt.setText("승률 " + GetWinRate(lst_UserGames_cobalt) + "%");
        }
    }

    private float GetAvgRank(ArrayList<UserGame> lst) {
        float total = 0;
        for (UserGame game : lst) {
            total += game.gameRank;
        }
        return total / lst.size();
    }

    private float GetWinRate(ArrayList<UserGame> lst) {
        float total = 0;
        for (UserGame game : lst) {
            if (game.gameRank == 1) {
                total++;
            }
        }
        return total / lst.size() * 100;
    }

    private int GetAvgDmg(ArrayList<UserGame> lst) {
        int total = 0;
        for (UserGame game : lst) {
            total += game.damageToPlayer;
        }
        return total / lst.size();
    }

    private rankInfo MMRtoTier(int mmr, int rank) {
        for (tierIndex tier : act_user.TierIndex) {
            if (tier.top >= mmr && tier.bottom <= mmr) {
                rankInfo rankInfo = new rankInfo();
                rankInfo.name = tier.tierName;
                switch (tier.tierName) {
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
                if (rank <= 700) {
                    rankInfo.name = "데미갓";
                    rankInfo.image = act_user.getResources().getDrawable(R.drawable.img_demigod);
                }
                if (rank <= 200) {
                    rankInfo.name = "이터니티";
                    rankInfo.image = act_user.getResources().getDrawable(R.drawable.img_eternity);
                }
                return rankInfo;
            }
        }
        Log.d("Error", "Can't Find Tier " + mmr);
        return null;
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

    private void Request_UserStats(int seasonId) {
        Log.d("Request", "UserStats " + seasonId);
        String response_UserStat = requester.Get("https://open-api.bser.io/v1/user/stats/" + userNum + "/" + seasonId);
        RE_UserStats userStats = converter.Convert_UserStats(response_UserStat);
        if (userStats.code == 404) {
            RE_UserStats nodata = new RE_UserStats();
            nodata.code = 404;
            nodata.message = "Not Found";
            com.example.erhistoryviewer.userStats nulluserstats = new userStats();
            nulluserstats.seasonId = seasonId;
            nodata.userStats.add(nulluserstats);
            userStat_rank = nodata;
        } else {
            if (seasonId != 0) {
                userStat_rank = userStats;
            } else {
                try {
                    userStat_normal = userStats.userStats.get(2);
                }
                catch (Exception ex){
                    Log.e("No Normal Data","");
                }
                try {
                    userStat_cobalt = userStats.userStats.get(3);
                }
                catch (Exception ex){
                    Log.e("No Cobalt Data","");
                }
            }
        }
    }

}