package com.example.erhistoryviewer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class act_lobby extends AppCompatActivity {

    ImageButton btn_search;
    EditText edt_userName;
    LinearLayout lin_userHistory;
    ImageView img_logo;
    static RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_lobby);
        // 뷰 찾기
        edt_userName = findViewById(R.id.edt_userName);
        lin_userHistory = findViewById(R.id.lin_userHistory);
        btn_search = findViewById(R.id.btn_search);
        img_logo = findViewById(R.id.img_logo);
        
        // 버튼 기능 할당
        SetOnClick();

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
    }

    private void SetOnClick(){
        btn_search.setOnClickListener(v -> Request_UserNum());

        findViewById(R.id.img_logo).setOnClickListener(v -> ClickLogo());
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
