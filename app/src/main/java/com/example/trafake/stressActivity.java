package com.example.trafake;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class stressActivity extends AppCompatActivity {


    Handler handler = new Handler();
    TextView statusV;
    TextView pingV;
    TextView cliV;
    TextView reqV;
    int threads=1;
    long requests=0;
    long time=1;
    OkHttpClient cl;
    OkHttpClient cl2;
    Request rq;
    Request rq2;
    ArrayList<String> targets = new ArrayList<String>();
    static String name;
    static String pass;
    Button stopButton;

    static String URL = "http://trafake.ddns.net:5555";
    static String url;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stress);
        statusV=findViewById(R.id.statusView);
        pingV=findViewById(R.id.pingView);
        cliV=findViewById(R.id.clientsView);
        reqV=findViewById(R.id.reqsView);
        stopButton = findViewById(R.id.stopTestButton);




        Intent intentacles = getIntent();
        name = intentacles.getStringExtra("username");
        pass = intentacles.getStringExtra("password");
        url = intentacles.getStringExtra("url");


        Log.d("OUTY", pass);


        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    JSONObject ob = new JSONObject();
                    cl2 = new OkHttpClient.Builder()
                            .build();



                    ob.put("username", name);
                    ob.put("password", pass);

                    MediaType json = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(json,ob.toString());



                    rq2 = new Request.Builder()
                            .url(URL+"/resetPool")
                            .post(body)
                            .build();


                    cl2.newCall(rq2).enqueue(new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {


                            Log.d("OUTY", response.body().string());
                            finish();
                            System.exit(0);

                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });






        try {
            PThread pthread = new PThread();
            pthread.start();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try {
            RThread rthread = new RThread();
            rthread.start();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try {
            QThread qthread = new QThread();
            qthread.start();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        for (int i = 0; i < threads; i++) {
            MThread mthread = null;
            try {
                mthread = new MThread();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mthread.start();

        }

    }

    class RThread extends Thread{
        private AtomicBoolean running = new AtomicBoolean(true);
        @Override
        public void run() {
            while (running.get()) {
                try {
                    req();
                } catch (Exception e) {
                }
            }
        }
        public void req(){
            time++;
            try {
                Thread.sleep(1000);
            }
            catch (Exception e){

            }
            handler.post(new Runnable() {
                public void run() {
                    reqV.setText("Requests per second: " + requests/time);
                }
            });
        }
    }

    class PThread extends  Thread {
        private AtomicBoolean running = new AtomicBoolean(true);

        @Override
        public void run() {
            while (running.get()) {
                try {
                    ping();
                } catch (Exception e) {
                }
            }
        }

        public void ping() throws Exception {

            String res = "";
            String cmd = "ping -c 1 "+url.replace("https://", "").replace("http://", "".replace("www.", ""));
            try {

                Process p;
                p = Runtime.getRuntime().exec(cmd);

                if(p.waitFor()!=0) {
                    handler.post(new Runnable() {
                        public void run() {
                            statusV.setText("Status: Offline");
                        }
                    });
                }
                else{
                    handler.post(new Runnable() {
                        public void run() {
                            statusV.setText("Status: Online");
                        }
                    });
                }
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

                String s;

                while ((s = stdInput.readLine()) != null) {
                    res += s + "\n";
                }
                p.destroy();
                System.out.println(res);
            } catch (Exception e) {
                e.printStackTrace();
            }
            final String fres = res;
            handler.post(new Runnable() {
                public void run() {
                    pingV.setText(fres.split(" time ")[1]);
                }
            });

        }
    }

    class MThread extends Thread{
        private AtomicBoolean running = new AtomicBoolean(true);



        @Override
        public void run() {
            while (running.get()) {
                try {
                    main();
                } catch (Exception e) {

                }


            }
        }
        public void main() throws Exception{

            for(int i=0;i<=targets.size();i++){
                URL url = new URL(targets.get(i));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.connect();
                Log.e(targets.get(i), String.valueOf(connection.getResponseCode()));
                requests++;
            }

        }
    }

    class QThread extends Thread {

        private AtomicBoolean running = new AtomicBoolean(true);



        @Override
        public void run() {
            while (running.get()) {
                try {
                    query();
                } catch (Exception e) {

                }


            }
        }


        public void query() throws Exception {
            JSONObject ob = new JSONObject();
            cl = new OkHttpClient.Builder()
                    .build();




            ob.put("username", name);
            ob.put("password", pass);

            MediaType json = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(json,ob.toString());



            rq = new Request.Builder()
                    .url("http://trafake.ddns.net:5555"+"/getPool")
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

                            targets.clear();

                            final JSONObject ob = new JSONObject(response.body().string());
                            JSONArray jsonArray = (JSONArray)ob.get("pool");

                            if (jsonArray != null) {
                                int len = jsonArray.length();
                                for (int i=0;i<len;i++){
                                    targets.add(jsonArray.get(i).toString());
                                }
                            }

                            Log.d("OUTY", targets.toString());


                            handler.post(new Runnable() {
                                public void run() {
                                    cliV.setText("Clients: " + targets.size());
                                }
                            });

                        }catch (Exception e){
                            System.out.println(e);
                        }
                    }
                }
            });



            Thread.sleep(10000);
        }
    }

}
