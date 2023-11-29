package com.example.erhistoryviewer;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserActivity extends AppCompatActivity {

    EditText edt_userName;
    ImageButton btn_tolobby;
    ImageButton btn_search;
    TextView txt_nickname;
    TextView txt_level;
    ImageView img_tier;
    ImageView img_mostcharacter;

    Spinner spn_seasons;
    static RequestQueue requestQueue;

    RE_Season re_season = null;
    RE_userstats re_userstats = null;

    Bitmap bitmap;

    frg_userInfo frg_userInfo;
    frg_matchHistory frg_matchHistory;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    TabLayout tabLayout_info;
    TabLayout tabLayout_match;

    List<charIndex> CharacterIndex = new ArrayList<>();
    List<String> lst_Season = new ArrayList<>();

    int selected_seasonId = -1;

    public enum Selected_Info {userinfo, matchhistory; }

    public Selected_Info selected_info = Selected_Info.userinfo;

    public enum Selected_Match {rank, casual, cobalt;}

    public Selected_Match selected_match = Selected_Match.rank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_user);

        //todo 로딩창 구현

        Init();

        btn_tolobby = findViewById(R.id.btn_tolobby);
        btn_search = findViewById(R.id.btn_search);
        edt_userName = findViewById(R.id.edt_userName);
        img_mostcharacter = findViewById(R.id.img_mostcharacter);
        img_tier = findViewById(R.id.img_tier);
        txt_level = findViewById(R.id.txt_level);
        txt_nickname = findViewById(R.id.txt_nickname);
        tabLayout_info = findViewById(R.id.tablayout_info);
        tabLayout_match = findViewById(R.id.tablayout_match);
        spn_seasons = findViewById(R.id.spn_season);

        Log.d("userNum", Objects.requireNonNull(getIntent().getStringExtra("userNum")));

        SetOnClick();

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        Request_DataSeason();

        fragmentManager = getSupportFragmentManager();
        frg_userInfo = new frg_userInfo();
        frg_matchHistory = new frg_matchHistory();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, frg_userInfo).commit();
    }

    private void SetSpnnierSeason() {
        for (int i = re_season.data.size() - 1; i >= 0; i--) {
            lst_Season.add(re_season.data.get(i).seasonName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getApplicationContext(),
                android.R.layout.simple_spinner_item,
                lst_Season);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_seasons.setAdapter(adapter);

        spn_seasons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                data_Season s = re_season.data.get(re_season.data.size() - 1 - position);
                selected_seasonId = s.seasonID;
                Log.d("Season Name", s.seasonName + "/" + s.seasonID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void SetUserStats() {
        Log.d("NickName", re_userstats.userStats.get(0).nickname);
        txt_nickname.setText(re_userstats.userStats.get(0).nickname);
        //todo 레벨 표시
        //todo 티어 표시
        SetUserStats_MostCharacter();
    }

    private void SetUserStats_MostCharacter() {
        // 닥지지에서 이미지 받아오기
        String charName = CharacterCodetoName(re_userstats.userStats.get(0).characterStats.get(0).characterCode);
        String skinCode = "S000"; //todo 가장 많이 사용한 스킨 찾기 구현
        String url_dak = String.format("https://cdn.dak.gg/assets/er/game-assets/1.9.0/CharResult_%s_%s.png", charName, skinCode);
        Thread thr_GetImage = new Thread(() -> {
            try {
                Log.d("Image from web", charName + "[" + skinCode + "]");
                URL url = new URL(url_dak);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // 서버로부터 응답 수신
                conn.connect(); //연결된 곳에 접속할 때 (connect() 호출해야 실제 통신 가능함)
                InputStream is = conn.getInputStream(); //inputStream 값 가져오기
                bitmap = BitmapFactory.decodeStream(is); // Bitmap으로 변환
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thr_GetImage.start(); // 작업 Thread 실행
        try {
            thr_GetImage.join();
            img_mostcharacter.setImageBitmap(bitmap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String CharacterCodetoName(int code) {
        return CharacterIndex.get(code - 1).name_E;
    }

    private void Init() {
        try {
            AssetManager assetManager = this.getAssets();
            InputStream inputStream = assetManager.open("characterindex.csv");
            CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream, "EUC-KR"));
            List<String[]> allContent = csvReader.readAll();
            for (String[] content : allContent) {
                Log.d("CharacterIndex", content[0] + "\t" + content[1] + "\t" + content[2]);
                CharacterIndex.add(new charIndex(content[0], content[1], content[2]));
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    private void SetOnClick() {
        btn_search.setOnClickListener(v -> {
            Request_UserNum();
        });

        btn_tolobby.setOnClickListener(v -> {
            Intent intent = new Intent(this, LobbyActivity.class);
            startActivity(intent);
        });

        tabLayout_info.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ChangeTab_Info(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tabLayout_match.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ChangeTab_Match(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void ChangeTab_Info(int index) {
        String tab = "";
        switch (index) {
            case 0:
                selected_info = Selected_Info.userinfo;
                tab = "유저정보";
                break;
            case 1:
                selected_info = Selected_Info.matchhistory;
                tab = "대전기록";
                break;
        }
        Log.d("Change Tab Info", "Change Tab to " + tab);
        ChangeTab();
    }

    private void ChangeTab(){
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        switch (selected_info){
            case userinfo:
                fragmentTransaction.replace(R.id.content, frg_userInfo).commit();
                break;
            case matchhistory:
                fragmentTransaction.replace(R.id.content, frg_matchHistory).commit();
                break;
        }
        //todo info탭 변경 시 현재 선택된 match탭으로 열리게 수정
    }

    private void ChangeTab_Match(int index) {
        String tab = "";
        switch (index) {
            case 0:
                selected_match = Selected_Match.rank;
                tab = "랭크";
                break;
            case 1:
                selected_match = Selected_Match.casual;
                tab = "일반";
                break;
            case 2:
                selected_match = Selected_Match.cobalt;
                tab = "코발트";
                break;
        }
        Log.d("Change Tab Match", "Change Tab to " + tab);
        switch (selected_info){
            case userinfo:
                frg_userInfo.ChangeFrag(selected_match);
                break;
            case matchhistory:
                frg_matchHistory.ChangeFrag(selected_match);
        }
    }

    private void Request_DataSeason() {
        Log.d("Request", "Request DataSeason");
        StringRequest request = new StringRequest(
                Request.Method.GET,
                "https://open-api.bser.io/v2/data/Season",
                response -> {
                    Response_DataSeason(response);
                    int currentSeason = 0;
                    for (int i = 0; i < re_season.data.size(); i++) {
                        if (re_season.data.get(i).isCurrent == 1) {
                            currentSeason = re_season.data.get(i).seasonID;
                            Log.d("Current Season", Integer.toString(currentSeason));
                            break;
                        }
                    }
                    Request_UserStats(getIntent().getStringExtra("userNum"), currentSeason);
                },
                error -> {
                    println(error.toString());
                    Log.e("Season", error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> header = new HashMap<>();
                header.put("x-api-key", "AcUmvv9Rtp2aOoVKiDnqP4gdVzeqiTVYahP9Xi6U");
                return header;
            }
        };

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void Response_DataSeason(String response) {

        Gson gson = new Gson();
        RE_Season re = gson.fromJson(response, RE_Season.class);

        Log.d("Response_DataSeason", response);

        if (re.code == 200) {
            re_season = re;
            SetSpnnierSeason();
        } else {
            Log.d("DataSeason", re.message);
            println("시즌 검색 오류" + re.message);
            re_userstats = null;
        }
    }

    private void Request_UserStats(String userNum, int seasonId) {
        Log.d("Request", "Request UserStats");
        StringRequest request = new StringRequest(
                Request.Method.GET,
                "https://open-api.bser.io/v1/user/stats/" + userNum + "/" + seasonId,
                response -> {
                    Response_UserStats(response);
                    txt_nickname.setText(re_userstats.userStats.get(0).nickname);
                },
                error -> {
                    println(error.toString());
                    Log.e("UserStats", error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> header = new HashMap<>();
                header.put("x-api-key", "AcUmvv9Rtp2aOoVKiDnqP4gdVzeqiTVYahP9Xi6U");
                return header;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    String utf8String = new String(response.data, "UTF-8");
                    return Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (Exception e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void Response_UserStats(String response) {

        Gson gson = new Gson();
        RE_userstats re = gson.fromJson(response, RE_userstats.class);

        Log.d("Response_UserStats", response);

        if (re.code == 200) {
            re_userstats = re;
            SetUserStats();
        } else {
            Log.d("UserStats", re.message);
            println("유저 검색 오류" + re.message);
            re_userstats = null;
        }
    }

    private void Request_UserNum() {
        Log.d("Request", "Request UserNum");
        String userName = edt_userName.getText().toString();

        StringRequest request = new StringRequest(
                Request.Method.GET,
                "https://open-api.bser.io/v1/user/nickname?query=" + userName,
                response -> {
                    Response_UserNum(response);
                },
                error -> {
                    println(error.toString());
                    Log.e("UserNum", error.toString());
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("x-api-key", "AcUmvv9Rtp2aOoVKiDnqP4gdVzeqiTVYahP9Xi6U");
                return header;
            }
        };

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void Response_UserNum(String response) {

        Gson gson = new Gson();
        RE_UserNum re = gson.fromJson(response, RE_UserNum.class);

        Log.d("Response_UserNum", response);

        if (re.code == 200) {
            Intent intent = new Intent(this, UserActivity.class);
            intent.putExtra("userNum", re.user.userNum);
            startActivity(intent);
            finish();
        } else {
            Log.d("UserNum", re.message);
            println("해당 이름을 가진 플레이어가 없습니다.");
        }
    }

    private void println(String data) {
        Toast toast = Toast.makeText(this, data, Toast.LENGTH_SHORT);
        toast.show();
    }
}
