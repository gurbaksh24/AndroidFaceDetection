package com.tavisca.taviscavisitor;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.loopj.android.http.*;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    EditText empId, password;
    Button loginBtn;
    TextView errorMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        empId=(EditText)findViewById(R.id.empId);
        password=(EditText)findViewById(R.id.password);
        loginBtn=(Button)findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String empIdData = empId.getText().toString();
                        final String empPassData = password.getText().toString();

                        new LoginClient().execute(empIdData,empPassData);
                        Log.d("abab","I'm Here");
                        Log.d("abab2","I'm Here"+new LoginClient().doInBackground());
                        Intent faceDetectIntent = new Intent(MainActivity.this,FaceDetectorService.class);
                        startActivity(faceDetectIntent);
                        /*
                        DatabaseConnector databaseConnector = new DatabaseConnector();
                        String response = databaseConnector.loginGuard(empIdData,empPassData);
                        if(response.equals("true")){
                            Intent intent = new Intent(MainActivity.this,FaceDetectorService.class);
                            startActivity(intent);
                        }
                        else
                        {
                            errorMessage=(TextView)findViewById(R.id.errorMessage);
                            errorMessage.setVisibility(v.VISIBLE);
                        }
                        */
                    }
                }
        );
    }
}
