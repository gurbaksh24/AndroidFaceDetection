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
import android.widget.TextView;
import android.widget.Toast;

public class WelcomeVisitorActivity extends AppCompatActivity {

    static String PREFERENCE = "GuardSessionPref";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    TextView welcomeMessage;
    EditText comingFrom, whomToMeet, purposeOfVisit;
    Button submit;
    String location, meetingPerson, purpose;
    String[] visitorNameAndId;
    String visitorName, visitorId, guardId;

    private void initializeItems() {
        welcomeMessage = (TextView) findViewById(R.id.welcome_welcomeMessage);
        comingFrom = (EditText) findViewById(R.id.welcome_comingFrom);
        whomToMeet = (EditText) findViewById(R.id.welcome_whomToMeet);
        purposeOfVisit = (EditText) findViewById(R.id.welcome_purposeOfVisit);
        submit = (Button) findViewById(R.id.welcome_submit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#161731")));
        checkSessionValidity();
        initializeItems();
        Intent intent = getIntent();
        visitorNameAndId = intent.getStringExtra("Visitor Name").substring(1, intent.getStringExtra("Visitor Name").length() - 1).split(":");
        visitorName = visitorNameAndId[0];
        visitorId = visitorNameAndId[1];
        welcomeMessage.setText("Welcome " + visitorName);
        submit.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveInfoOfVisitor();
                    }
                }
        );
    }

    private void getDataFromInput() {
        location = comingFrom.getText().toString();
        meetingPerson = whomToMeet.getText().toString();
        purpose = purposeOfVisit.getText().toString();
    }

    private void saveInfoOfVisitor() {
        getDataFromInput();
        String response = null;
        try {
            response = new ExistingVisitorOperationApiCaller().execute(visitorId, location, meetingPerson, purpose, guardId).get();
        } catch (Exception exception) {
            exception.printStackTrace();
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        if (response.equals("true")) {
            Intent intent = new Intent(WelcomeVisitorActivity.this, ThankYouActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Employee Not Found! Please Check the Employee Name", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkSessionValidity() {
        sharedPreferences = getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        guardId = sharedPreferences.getString("sessionId", "default");
        if (guardId.equals("default")) {
            Intent faceDetectIntent = new Intent(getApplicationContext(), LoginActivity.class);
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
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
