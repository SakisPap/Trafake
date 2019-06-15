package com.example.trafake;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class submitActivity extends AppCompatActivity {



    static String URL = "http://trafake.ddns.net:5555";
    OkHttpClient cl;
    Request rq;

    TextView status;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);
        status = findViewById(R.id.listtext);




        try {

            JSONObject ob = new JSONObject();
            cl = new OkHttpClient.Builder()
                    .build();


            ob.put("username", "sakis");
            ob.put("password", "1234");

            MediaType json = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(json,ob.toString());





            rq = new Request.Builder()
                    .url(URL+"/getPool")
                    .build();


            cl.newCall(rq).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {


                    Log.d("OUTY", response.body().string());


                    if (response.isSuccessful()){

                        try{

                            final JSONObject ob = new JSONObject(response.body().string());

                            Log.d("OUTY", ob.toString());
                            if(ob.get("status").equals("notInSession")){

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        status.setText("Not in session");
                                    }
                                });

                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        status.setText(ob.toString());
                                    }
                                });
                            }

                        }catch (Exception e){
                            System.out.println(e);
                        }
                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}