package com.example.erhistoryviewer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_user);

        btn_tolobby = findViewById(R.id.btn_tolobby);
        btn_search = findViewById(R.id.btn_search);
        edt_userName = findViewById(R.id.edt_userName);
        img_mostcharacter = findViewById(R.id.img_mostcharacter);
        img_tier = findViewById(R.id.img_tier);
        txt_level = findViewById(R.id.txt_level);
        txt_nickname = findViewById(R.id.txt_nickname);

        Log.d("userNum", getIntent().getStringExtra("userNum"));

        SetOnClick();

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        Request_DataSeason();
    }

    private void SetOnClick() {
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btn_tolobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
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
                "https://open-api.bser.io/v1/user/stats/" + userNum + "/" + Integer.toString(seasonId),
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

    private void Response_UserStats(String response) {

        Gson gson = new Gson();
        RE_userstats re = gson.fromJson(response, RE_userstats.class);

        Log.d("Response_UserStats", response);

        if (re.code == 200) {
            re_userstats = re;
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
