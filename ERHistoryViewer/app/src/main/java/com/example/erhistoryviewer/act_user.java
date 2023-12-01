package com.example.erhistoryviewer;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class act_user extends AppCompatActivity {


    String apikey = "AcUmvv9Rtp2aOoVKiDnqP4gdVzeqiTVYahP9Xi6U";
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
    RE_UserStats re_userstats = null;



    TabLayout tabLayout_info;
    TabLayout tabLayout_match;

    List<charIndex> CharacterIndex = new ArrayList<>();
    List<String> lst_Season = new ArrayList<>();

    int selected_seasonId = -1;
    String userNum = "";
    public enum Selected_Info {userinfo, matchhistory;}

    public Selected_Info selected_info = Selected_Info.userinfo;

    public enum Selected_Match {rank, casual, cobalt;}

    public Selected_Match selected_match = Selected_Match.rank;

    //스크롤 뷰
    ScrollView scv_info_rank;
    ScrollView scv_info_normal;
    ScrollView scv_info_cobalt;
    ScrollView scv_history_rank;
    ScrollView scv_history_normal;
    ScrollView scv_history_cobalt;

    //컨텐츠
    LinearLayout content_info_rank;
    LinearLayout content_info_normal;
    LinearLayout content_info_cobalt;
    LinearLayout content_history_rank;
    LinearLayout content_history_normal;
    LinearLayout content_history_cobalt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_user);

        //todo 로딩창 구현

        Init();

        SetViews();

        Log.d("userNum", userNum);

        SetOnClick();

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        //스레드 시작
        thd_Request thd_request = new thd_Request("API Request Thread", this);
        thd_request.start();

    }

    private void Init() {
        userNum = getIntent().getStringExtra("userNum");

        //캐릭터ID 파일 읽기
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

    private void SetViews(){
        btn_tolobby = findViewById(R.id.btn_tolobby);
        btn_search = findViewById(R.id.btn_search);
        edt_userName = findViewById(R.id.edt_userName);
        img_mostcharacter = findViewById(R.id.img_mostcharacter);
        img_tier = findViewById(R.id.img_refresh);
        txt_level = findViewById(R.id.txt_level);
        txt_nickname = findViewById(R.id.txt_nickname);
        tabLayout_info = findViewById(R.id.tablayout_info);
        tabLayout_match = findViewById(R.id.tablayout_match);
        spn_seasons = findViewById(R.id.spn_season);
        //스크롤뷰
        scv_info_rank = findViewById(R.id.scv_info_rank);
        scv_info_normal = findViewById(R.id.scv_info_normal);
        //scv_info_cobalt = findViewById(R.id.scv_info_cobalt); //todo 코발트 UI추가
        scv_history_rank = findViewById(R.id.scv_history_rank);
        scv_history_normal = findViewById(R.id.scv_history_normal);
        //scv_history_cobalt = findViewById(R.id.scv_history_cobalt);
        //컨텐츠
        content_info_rank = findViewById(R.id.content_info_rank);
        content_info_normal = findViewById(R.id.content_info_normal);
        //content_info_cobalt = findViewById(R.id.content_info_cobalt);
        content_history_rank = findViewById(R.id.content_history_rank);
        content_history_normal = findViewById(R.id.content_history_normal);
        //content_history_cobalt = findViewById(R.id.content_history_cobalt);
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



    public String CharacterCodetoName(int code) {
        return CharacterIndex.get(code - 1).name_E;
    }

    private void SetOnClick() {
        btn_search.setOnClickListener(v -> {
            Request_UserNum();
        });

        btn_tolobby.setOnClickListener(v -> {
            Intent intent = new Intent(this, act_lobby.class);
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
        ChangeScrollView();
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
        ChangeScrollView();
    }

    private void ChangeScrollView(){
        scv_info_rank.setVisibility(View.GONE);
        scv_info_normal.setVisibility(View.GONE);
        //scv_info_cobalt.setVisibility(View.GONE);
        scv_history_rank.setVisibility(View.GONE);
        scv_history_normal.setVisibility(View.GONE);
        //scv_history_cobalt.setVisibility(View.GONE);
        switch (selected_info){
            case userinfo:
                switch (selected_match){
                    case rank:
                        scv_info_rank.setVisibility(View.VISIBLE);
                        break;
                    case casual:
                        scv_info_normal.setVisibility(View.VISIBLE);
                        break;
                    case cobalt:
                        //scv_info_cobalt.setVisibility(View.VISIBLE);
                        break;
                }
                break;
            case matchhistory:
                switch (selected_match){
                    case rank:
                        scv_history_rank.setVisibility(View.VISIBLE);
                        break;
                    case casual:
                        scv_history_normal.setVisibility(View.VISIBLE);
                        break;
                    case cobalt:
                        //scv_history_cobalt.setVisibility(View.VISIBLE);
                        break;
                }
                break;
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
                header.put("x-api-key", apikey);
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
            Intent intent = new Intent(this, act_user.class);
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
