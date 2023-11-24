package com.example.erhistoryviewer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class LobbyActivity extends AppCompatActivity {

    EditText edt_userName;
    LinearLayout lin_userHistory;
    static RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_lobby);
        // 뷰 찾기
        edt_userName = findViewById(R.id.edt_userName);
        lin_userHistory = findViewById(R.id.lin_userHistory);
        ImageButton btn_search = findViewById(R.id.btn_search);
        
        // 버튼 기능 할당
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeRequest();
            }
        });


        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }


        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    }

    public void makeRequest() {
        String userName = edt_userName.getText().toString();

        StringRequest request = new StringRequest(
                Request.Method.GET,
                "https://open-api.bser.io/v1/user/nickname?query="+userName,
                response -> {processResponse(response);},
                error -> {println(error.toString()); Log.e("UserNum", error.toString());}
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

    public void println(String data) {
        Toast toast = Toast.makeText(this, data, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void processResponse(String response) {

        Gson gson = new Gson();
        RE_UserNum re_userNum = gson.fromJson(response, RE_UserNum.class);

        Log.d("Response.json",response);

        if(re_userNum.code == 200){
//            Log.d("UserNum", userNum.user.userNum);

            //todo userNum을 act_user로 넘기기
            Intent intent = new Intent(this, UserActivity.class);
            intent.putExtra("userNum", re_userNum.user.userNum);
            startActivity(intent);
            finish();
        }
        else{
            Log.d("UserNum", re_userNum.message);
            println("해당 이름을 가진 플레이어가 없습니다.");
        }
    }

}
