package com.example.trafake;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



// This is the final activity
// Here the traffic generation for items in the Pool is performed
// Users here can see real time statistics regarding their target website's behaviour under load
// Traffic generation can be stopped at any time

public class stressActivity extends AppCompatActivity {


    Handler handler = new Handler();
    TextView statusV;
    TextView pingV;
    TextView cliV;
    TextView reqV;
    int threads=1;
    long requests=1;
    long time=1;
    OkHttpClient cl;
    OkHttpClient cl2;
    Request rq;
    Request rq2;
    ArrayList<String> targets = new ArrayList<>();
    static String name;
    static String pass;
    Button stopButton;

    // Trafake server url
    static String URL = "http://trafake.ddns.net:5555";

    // Target website variable init
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



        // Get user credentials and target url from Intent
        Intent intentPrev = getIntent();
        name = intentPrev.getStringExtra("username");
        pass = intentPrev.getStringExtra("password");
        url = intentPrev.getStringExtra("url");


        // Stop Traffic button press
        stopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {

                    // Fresh request client
                    JSONObject ob = new JSONObject();
                    cl2 = new OkHttpClient.Builder()
                            .build();


                    // Build stop traffic request payload
                    ob.put("username", name);
                    ob.put("password", pass);

                    MediaType json = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(json,ob.toString());


                    // Stop traffic request
                    rq2 = new Request.Builder()
                            .url(URL+"/resetPool")
                            .post(body)
                            .build();

                    // Perform request
                    cl2.newCall(rq2).enqueue(new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {


                            Log.d("OUTY", response.body().string());

                            // Exit to previous Activity
                            finish();
                            System.exit(0);

                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });


        // Initialize threads to be used for traffic generation / status information
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

    // Rate of requests performed towards target website calculation thread
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


    // Target website responsiveness calculation thread using ping - ICMP packets
    // Target website availability (online/offline) gets determined here
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
                            statusV.setText("Target Website Status: Offline");
                        }
                    });
                }
                else{
                    handler.post(new Runnable() {
                        public void run() {
                            statusV.setText("Target Website Status: Online");
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
                    pingV.setText("Website responsiveness: " + fres.split(" time ")[1]);
                }
            });

        }
    }


    // Traffic generation thread. This thread goes through all of the urls pulled from the API Pool
    // and performs GET requests with a 5 sec request timeout limit (in case a website goes offline)
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


    // This thread "pulls" the URL Pool from the API in set intervals
    // Number of traffic generating clients gets calculated here
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

            // Build request payload
            ob.put("username", name);
            ob.put("password", pass);

            MediaType json = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(json,ob.toString());

            // Get urls from Pool request
            rq = new Request.Builder()
                    .url(URL+"/getPool")
                    .post(body)
                    .build();

            // Perform request
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

                            // Debug
                            Log.d("OUTY", targets.toString());

                            handler.post(new Runnable() {
                                public void run() {
                                    cliV.setText("Number of Traffic Generating Clients: " + targets.size());
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
