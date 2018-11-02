package com.tavisca.taviscavisitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class ThankYouActivity extends AppCompatActivity {
    static String PREFERENCE = "GuardSessionPref";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String guardId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thank_you);
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#161731")));
        checkSessionValidity();
        expireActivityAfterFiveSeconds();
    }

    private void checkSessionValidity() {
        sharedPreferences = getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        guardId = sharedPreferences.getString("sessionId", "default");
        if (guardId.equals("default")) {
            Intent faceDetectIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(faceDetectIntent);
            finish();
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
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void expireActivityAfterFiveSeconds() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 5000);
    }
}
