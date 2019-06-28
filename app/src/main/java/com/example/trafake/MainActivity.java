package com.example.trafake;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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


// This Activity is the Login/Register screen.
// Users can Authenticate by Logging in providing a username/password cobination
// Users can also create an account from here.

public class MainActivity extends AppCompatActivity {

    TextView status;
    TextView username;
    TextView password;
    Button login;
    Button register;
    Request rq;
    OkHttpClient cl;

    // Trafake server url
    static String URL = "http://trafake.ddns.net:5555";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_main);

        username=findViewById(R.id.usernameInput);
        password=findViewById(R.id.passwordInput);
        login=findViewById(R.id.loginButton);
        register=findViewById(R.id.registerButton);
        status=findViewById(R.id.statusText);


        // Http request client initialization
        cl = new OkHttpClient.Builder()
                .build();


        // Button press for Login action
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = username.getText().toString();
                String pass = password.getText().toString();

                JSONObject ob = new JSONObject();

                // Create Intent to transfer username and password to next activity for user to be able to authenticate again.
                // The new Activity gets specified here
                final Intent intent = new Intent(MainActivity.this, submitActivity.class);
                intent.putExtra("username", name);
                intent.putExtra("password", pass);


                try {

                    // Create Json Payload for the Login request, this payload contains the credentials of the user
                    ob.put("username", name);
                    ob.put("password", pass);

                    MediaType json = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(json,ob.toString());


                    // Request for the login endpoint of the Trafake Server API
                    rq = new Request.Builder()
                            .url(URL+"/login")
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

                                    JSONObject ob = new JSONObject(response.body().string());

                                    // Check response for successful login and inform user
                                    if(ob.get("status").equals("loginSuccess")){

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                status.setText("Login Success");

                                                // Move to next activity
                                                startActivity(intent);
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



        // Button press for Register action
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = username.getText().toString();
                String pass = password.getText().toString();


                JSONObject ob = new JSONObject();

                try {

                    // Create payload for Registration request
                    ob.put("username", name);
                    ob.put("password", pass);
                    MediaType json = MediaType.parse("application/json");
                    RequestBody bod = RequestBody.create(json,ob.toString());

                    // Request for the register endpoint of the Trafake Server API
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

                                    // Check response for successful registration and inform user
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
