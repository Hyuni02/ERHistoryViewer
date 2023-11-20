package com.example.erhistoryviewer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
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
                response -> {processResponse(response);},
                error -> println(error.toString())
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
        println("요청 보냄.");
    }

    public void println(String data) {
        Log.d("MainActivity", data);
        Toast toast = Toast.makeText(this, data, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void processResponse(String response) {
//        adapter.items.clear();

        Gson gson = new Gson(); // 변수 선언 || variable declaration
//        MovieList movieList = gson.fromJson(response, MovieList.class);
        Log.d("Response.json",response);
//        println("영화정보의 수 : " + movieList.boxOfficeResult.dailyBoxOfficeList.size());

//        for (int i = 0; i < movieList.boxOfficeResult.dailyBoxOfficeList.size()/2; i++) {
//            Movie movie = movieList.boxOfficeResult.dailyBoxOfficeList.get(i);
//
//            adapter.addItem(movie);
//        }

//        adapter.notifyDataSetChanged();
    }

}
