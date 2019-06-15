package com.example.trafake;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    TextView status;
    TextView username;
    TextView password;
    Button login;
    Button register;
    Request rq;
    OkHttpClient cl;

    static String URL = "http://trafake.ddns.net:5555";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username=findViewById(R.id.usernameInput);
        password=findViewById(R.id.passwordInput);
        login=findViewById(R.id.loginButton);
        register=findViewById(R.id.registerButton);
        status=findViewById(R.id.statusText);



        cl = new OkHttpClient.Builder()
                .build();



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = username.getText().toString();
                String pass = password.getText().toString();

                JSONObject ob = new JSONObject();


                try {

                    ob.put("username", name);
                    ob.put("password", pass);

                    MediaType json = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(json,ob.toString());



                    rq = new Request.Builder()
                            .url(URL+"/login")
                            .post(body)
                            .build();


                    cl.newCall(rq).enqueue(new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            if (response.isSuccessful()){

                                try{

                                    JSONObject ob = new JSONObject(response.body().string());
                                    if(ob.get("status").equals("loginSuccess")){

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                status.setText("Login Success");
                                                startActivity(new Intent(MainActivity.this, submitActivity.class));
                                            }
                                        });

                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                status.setText("Login Fail");
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






        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = username.getText().toString();
                String pass = password.getText().toString();


                JSONObject ob = new JSONObject();

                try {

                    ob.put("username", name);
                    ob.put("password", pass);
                    MediaType json = MediaType.parse("application/json");
                    RequestBody bod = RequestBody.create(json,ob.toString());


                    rq = new Request.Builder()
                            .url(URL+"/register")
                            .post(bod)
                            .build();


                    cl.newCall(rq).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()){

                                try{

                                    JSONObject ob = new JSONObject(response.body().string());
                                    if(ob.get("status").equals("registrationSuccessful")){

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                status.setText("Account created, please login.");
                                            }
                                        });

                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                status.setText("User already exists");
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
        });

    }


});}}
