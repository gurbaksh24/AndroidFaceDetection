package com.tavisca.taviscavisitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity {

    EditText empId, password;
    Button loginBtn;
    TextView errorMessage;
    static String PREFERENCE = "GuardSessionPref";
    String empIdData, empPassData;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private void initializeItems() {
        empId = (EditText) findViewById(R.id.empId);
        password = (EditText) findViewById(R.id.password);
        loginBtn = (Button) findViewById(R.id.loginBtn);
        errorMessage = (TextView) findViewById(R.id.errorMessage);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorActionBar)));
        setContentView(R.layout.activity_main);

        checkSessionValidity();
        initializeItems();
        loginBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        readDataFromUI();
                        if (!empIdData.equals("") && !empPassData.equals("")) {
                            LoginApiCaller loginApiCaller = new LoginApiCaller();
                            String response = null;
                            try {
                                response = loginApiCaller.execute(empIdData, empPassData).get();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (response.equals("true")) {
                                setSession(empIdData);
                                openApplicationChooserActivity();
                                finish();                                                                //Finish Current Activity
                            } else {
                                errorMessage.setVisibility(v.VISIBLE);
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Emp Id and Password can not be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void readDataFromUI() {
        empIdData = empId.getText().toString();
        empPassData = password.getText().toString();
    }

    private void checkSessionValidity() {
        sharedPreferences = getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if (!sharedPreferences.getString("sessionId", "default").equals("default")) {
            Intent faceDetectIntent = new Intent(LoginActivity.this, ApplicationChooserActivity.class);
            faceDetectIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(faceDetectIntent);
        }
    }

    private void setSession(String empIdData) {
        editor.putString("sessionId", empIdData);
        editor.commit();
    }

    private void openApplicationChooserActivity() {
        Intent faceDetectIntent = new Intent(LoginActivity.this, ApplicationChooserActivity.class);
        startActivity(faceDetectIntent);
    }
}
