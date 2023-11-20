package com.example.mp_week11;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    EditText editText;

    static RequestQueue requestQueue;

    RecyclerView recyclerView;
    MovieAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeRequest();
            }
        });

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MovieAdapter();
        recyclerView.setAdapter(adapter);

    }



    public void makeRequest() {
        String url = editText.getText().toString();

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {processResponse(response);}, // 응답 시의 동작 || actions when response
                error -> println("error") // 적절한 에러처리(정답은 따로 없음, 로그출력용) || error handling, freely make it!
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String,String>();

                return params;
            }
        };

        request.setShouldCache(false);
        requestQueue.add(request);
        println("요청 보냄.");
    }

    public void println(String data) {
        Log.d("MainActivity", data);
    }


    public void processResponse(String response) {
        Gson gson = new Gson(); // 변수 선언 || variable declaration
        MovieList movieList = gson.fromJson(response, MovieList.class); // 응답받은 JSON 문자열을 특정 타입의 클래스로 변환 || transforming specific class type from received JSON String

        println("영화정보의 수 : " + movieList.boxOfficeResult.dailyBoxOfficeList.size());

        for (int i = 0; i < movieList.boxOfficeResult.dailyBoxOfficeList.size(); i++) {
            Movie movie = movieList.boxOfficeResult.dailyBoxOfficeList.get(i);

            adapter.addItem(movie);
        }

        adapter.notifyDataSetChanged();
    }

}
