package com.tavisca.taviscavisitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class OTPVerificationActivity extends AppCompatActivity {

    static String PREFERENCE = "GuardSessionPref";
    EditText otp;
    Button submit;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Intent intent;
    String otpEnteredByVisitor;
    String otpGenerated;
    String name, contact, govtId, comingFrom, whomToMeet, purposeOfVisit, guardId;

    private void initializeItems() {
        otp = (EditText) findViewById(R.id.otp_verification_otp);
        submit = (Button) findViewById(R.id.otp_verification_submit_btn);
        intent = getIntent();
        otpGenerated = intent.getStringExtra("otpGenerated");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#161731")));
        setContentView(R.layout.activity_otpverification);
        checkSessionValidity();
        initializeItems();
        Toast.makeText(this, otpGenerated, Toast.LENGTH_SHORT).show();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otpEnteredByVisitor = otp.getText().toString();
                if (otpEnteredByVisitor.equals(otpGenerated)) {
                    saveNewEntry();
                    Toast.makeText(OTPVerificationActivity.this, "Visitor Entry Saved", Toast.LENGTH_LONG).show();
                    /*Intent intentToWelcomeActivity = new Intent(OTPVerificationActivity.this, WelcomeVisitorActivity.class);
                    intentToWelcomeActivity.putExtra("Visitor Name", name);
                    startActivity(intentToWelcomeActivity);*/
                    Intent intent = new Intent(OTPVerificationActivity.this, ThankYouActivity.class);
                    startActivity(intent);
                    finish();
                } else
                    Toast.makeText(OTPVerificationActivity.this, "Wrong OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDataFromIntent() {
        name = intent.getStringExtra("name");
        contact = intent.getStringExtra("contact");
        govtId = intent.getStringExtra("govtId");
        comingFrom = intent.getStringExtra("comingFrom");
        whomToMeet = intent.getStringExtra("whomToMeet");
        purposeOfVisit = intent.getStringExtra("purposeOfVisit");
        guardId = intent.getStringExtra("guardId");
    }

    private void saveNewEntry() {
        getDataFromIntent();
        NewVisitorEntryApiCaller newVisitorEntryApiCaller = new NewVisitorEntryApiCaller();
        String response = null;
        try {
            response = newVisitorEntryApiCaller.execute(name, contact, govtId, comingFrom, whomToMeet, purposeOfVisit, guardId).get();
        } catch (Exception exception) {
            Toast.makeText(this, "Employee Not Found! Please Check the Employee Name", Toast.LENGTH_SHORT).show();
            exception.printStackTrace();
            finish();
        }
        if (!response.equals("false"))
            Toast.makeText(OTPVerificationActivity.this, "Welcome " + response.toUpperCase(), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(OTPVerificationActivity.this, "Something went wrong while adding entry", Toast.LENGTH_SHORT).show();
    }

    private void checkSessionValidity() {
        sharedPreferences = getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if (sharedPreferences.getString("sessionId", "default").equals("default")) {
            Intent faceDetectIntent = new Intent(OTPVerificationActivity.this, LoginActivity.class);
            faceDetectIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(faceDetectIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.change_password) {
            Toast.makeText(this, "Change Password", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.logout) {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        new LogoutApiCaller().execute(sharedPreferences.getString("sessionId", "default"));
        editor.putString("sessionId", "default");
        editor.commit();
        Intent intent = new Intent(OTPVerificationActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
