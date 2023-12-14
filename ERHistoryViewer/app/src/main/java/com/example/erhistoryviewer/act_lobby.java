package com.example.erhistoryviewer;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class act_lobby extends AppCompatActivity {

    ImageButton btn_search;
    EditText edt_userName;
    LinearLayout lin_userHistory;
    ImageView img_logo;
    LinearLayout layout_freeCharacter;
    static RequestQueue requestQueue;

    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    Map<Integer, Integer> dic_charactercoderesourceid = new HashMap<>();

    List<charIndex> CharacterIndex = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_lobby);
        // 뷰 찾기
        edt_userName = findViewById(R.id.edt_userName);
        lin_userHistory = findViewById(R.id.lin_userHistory);
        btn_search = findViewById(R.id.btn_search);
        img_logo = findViewById(R.id.img_logo);
        layout_freeCharacter = findViewById(R.id.layout_freeCharacter);

        ReadCharacterIndex();
        // 버튼 기능 할당
        SetOnClick();

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        thd_RequestFreeCharacter thd_request = new thd_RequestFreeCharacter("API Request Thread", this);
        thd_request.start();
    }

    private void SetOnClick(){
        btn_search.setOnClickListener(v -> Request_UserNum());

        findViewById(R.id.img_logo).setOnClickListener(v -> ClickLogo());
    }
    private void ReadCharacterIndex(){
        //캐릭터ID 파일 읽기
        AssetManager assetManager = this.getAssets();
        try {
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
        for (charIndex character : CharacterIndex) {
            dic_charactercoderesourceid.put(Integer.parseInt(character.code), CharacterCodetoResourceId(Integer.parseInt(character.code)));
        }
    }
    public int CharacterCodetoResourceId(int code) {
        return getResources().getIdentifier(CharacterCodetoName(code).toLowerCase(), "drawable", getPackageName());
    }
    public String CharacterCodetoName(int code) {
        return CharacterIndex.get(code - 1).name_E;
    }

    private void ClickLogo(){
        img_logo.setImageResource(R.drawable.hartshocked);
    }

    private void Request_UserNum() {
        Log.d("Request", "Request UserNum");
        String userName = edt_userName.getText().toString();

        StringRequest request = new StringRequest(
                Request.Method.GET,
                "https://open-api.bser.io/v1/user/nickname?query="+userName,
                this::Response_UserNum,
                error -> {
                    println(error.toString());
                    Log.e("UserNum", error.toString());
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();
                return params;
            }
            @Override
            public  Map<String, String> getHeaders() throws AuthFailureError{
                Map<String, String> header = new HashMap<>();
                header.put("x-api-key","AcUmvv9Rtp2aOoVKiDnqP4gdVzeqiTVYahP9Xi6U");
                return header;
            }
        };

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void Response_UserNum(String response) {

        Gson gson = new Gson();
        RE_UserNum re = gson.fromJson(response, RE_UserNum.class);

        Log.d("Response_UserNum",response);

        if(re.code == 200){
            Intent intent = new Intent(this, act_user.class);
            intent.putExtra("userNum", re.user.userNum);
            startActivity(intent);
            finish();
        }
        else{
            Log.d("UserNum", re.message);
            println("해당 이름을 가진 플레이어가 없거나 90일 이내 플레이 기록이 없습니다.");
        }
    }

    private void println(String data) {
        Toast toast = Toast.makeText(this, data, Toast.LENGTH_SHORT);
        toast.show();
    }

}
