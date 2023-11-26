package com.example.erhistoryviewer;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    static RequestQueue requestQueue;

    RE_Season re_season = null;
    RE_userstats re_userstats = null;

    Bitmap bitmap;

    List<charIndex> CharacterIndex = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_user);

        Init();

        btn_tolobby = findViewById(R.id.btn_tolobby);
        btn_search = findViewById(R.id.btn_search);
        edt_userName = findViewById(R.id.edt_userName);
        img_mostcharacter = findViewById(R.id.img_mostcharacter);
        img_tier = findViewById(R.id.img_tier);
        txt_level = findViewById(R.id.txt_level);
        txt_nickname = findViewById(R.id.txt_nickname);

        Log.d("userNum", Objects.requireNonNull(getIntent().getStringExtra("userNum")));

        SetOnClick();

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        Request_DataSeason();


    }

    private void SetUserStats() {
        txt_nickname.setText(re_userstats.userStats.get(0).nickname);
        //todo 레벨 표시
        //todo 티어 표시
        SetUserStats_MostCharacter();
    }

    private void SetUserStats_MostCharacter() {
        //todo 모스트 캐릭터 사진 표시

        // 닥지지에서 이미지 받아오기
        String charName = CharacterCodetoName(re_userstats.userStats.get(0).characterStats.get(0).characterCode);
        String skinCode = "S000"; //todo 가장 많이 사용한 스킨 찾기 구현
        String url_dak = String.format("https://cdn.dak.gg/assets/er/game-assets/1.9.0/CharResult_%s_%s.png", charName, skinCode);
        Thread thr_GetImage = new Thread(() -> {
            try {
                // 이미지 URL 경로
                URL url = new URL(url_dak);

                // web에서 이미지를 가져와 ImageView에 저장할 Bitmap을 만든다.
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

        });

        btn_tolobby.setOnClickListener(v -> {
            Intent intent = new Intent(this, LobbyActivity.class);
            startActivity(intent);
        });
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

    private void println(String data) {
        Toast toast = Toast.makeText(this, data, Toast.LENGTH_SHORT);
        toast.show();
    }
}
