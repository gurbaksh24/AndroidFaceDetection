package com.tavisca.taviscavisitor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
                        LoginClient loginClient = new LoginClient();
                        loginClient.execute(empIdData, empPassData);
                        if (loginClient.doInBackground(empIdData, empPassData).equals("true")) {
                            Intent faceDetectIntent = new Intent(MainActivity.this, FaceDetectorService.class);
                            startActivity(faceDetectIntent);
                        } else {
                            errorMessage=(TextView)findViewById(R.id.errorMessage);
                            errorMessage.setVisibility(v.VISIBLE);
                        }
                    }
                }
        );
    }
}
