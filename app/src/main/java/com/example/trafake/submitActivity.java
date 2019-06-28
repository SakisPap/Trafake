package com.example.trafake;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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



// This Activity is the target url submission page
// Users provide the url of the target website here

public class submitActivity extends AppCompatActivity {


    // Trafake Server url
    static String URL = "http://trafake.ddns.net:5555";

    static String name;
    static String pass;
    TextView urlText;
    Button submit;
    OkHttpClient cl;
    Request rq;
    TextView status;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_submit);
        status = findViewById(R.id.listtext);

        urlText = findViewById(R.id.urlText);
        submit = findViewById(R.id.submitButton);


        // Buton press for submit url action
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    // Get users credentials from previous activity.
                    // Those are needed in order to be able to authenticate on the API
                    final Intent i = getIntent();
                    name = i.getStringExtra("username");
                    pass = i.getStringExtra("password");

                    // Create new Intent to pass credentials to next activity along with the target url
                    final Intent newIntent = new Intent(submitActivity.this, stressActivity.class);
                    newIntent.putExtra("username", name);
                    newIntent.putExtra("password", pass);
                    newIntent.putExtra("url", urlText.getText().toString());

                    JSONObject ob = new JSONObject();
                    cl = new OkHttpClient.Builder()
                            .build();

                    // Create request payload for target url submission
                    ob.put("username", name);
                    ob.put("password", pass);
                    ob.put("url", urlText.getText().toString());

                    MediaType json = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(json,ob.toString());


                    // Request for submit url endpoint
                    rq = new Request.Builder()
                            .url(URL+"/submiturl")
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

                            // Log with tag to debug if submission successful
                            Log.d("OUTY", response.body().string());


                            if (response.isSuccessful()){
                                try{
                                    startActivity(newIntent);
                                    JSONObject ob = new JSONObject(response.body().string());

                                    // Check submission status and inform user
                                    if(ob.get("status").equals("submissionSuccess")){

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                status.setText("Login Success");

                                                // Move to next Activity
                                                startActivity(newIntent);
                                            }
                                        });

                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                status.setText("Submission Fail");
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
}}