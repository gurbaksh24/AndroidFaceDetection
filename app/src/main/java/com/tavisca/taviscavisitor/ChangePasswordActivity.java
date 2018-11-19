package com.tavisca.taviscavisitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePasswordActivity extends AppCompatActivity {
    static String PREFERENCE = "GuardSessionPref";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String guardId;
    EditText oldPassword, newPassword, confirmNewPassword;
    Button submit;

    private void initializeItems() {
        oldPassword = (EditText) findViewById(R.id.change_password_old_password);
        newPassword = (EditText) findViewById(R.id.change_password_new_password);
        confirmNewPassword = (EditText) findViewById(R.id.change_password_confirm_new_password);
        submit = (Button) findViewById(R.id.change_password_submit);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#161731")));
        setContentView(R.layout.activity_change_password);
        checkSessionValidity();
        initializeItems();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (newPassword.getText().toString().equals(confirmNewPassword.getText().toString())) {
                    LoginApiCaller loginApiCaller = new LoginApiCaller();
                    String validUserResponse = null;
                    try {
                        validUserResponse = loginApiCaller.execute(guardId, oldPassword.getText().toString()).get();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    if (validUserResponse.equals("true")) {
                        ChangePasswordApiCaller changePasswordApiCaller = new ChangePasswordApiCaller();
                        String changePasswordResponse = null;
                        try {
                            changePasswordResponse = changePasswordApiCaller.execute(guardId, newPassword.getText().toString()).get();
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        if (changePasswordResponse.equals("\"Password changed successfully\"")) {
                            Toast.makeText(ChangePasswordActivity.this, "Password Changed Successfully. Please Login Again.", Toast.LENGTH_SHORT).show();
                            logout();
                        }
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "Wrong Old Password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Password Mismatch", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
