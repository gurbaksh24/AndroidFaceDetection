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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewVisitorEntry extends AppCompatActivity {

    static String PREFERENCE = "GuardSessionPref";
    EditText name, contact, govtId, comingFrom, whomToMeet, purposeOfVisit;
    Button submit;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    AutoCompleteTextView autoCompleteTextView;

    private void initializeItems() {
        name = (EditText) findViewById(R.id.new_visitor_name);
        contact = (EditText) findViewById(R.id.new_visitor_contact);
        govtId = (EditText) findViewById(R.id.new_visitor_govt_id);
        comingFrom = (EditText) findViewById(R.id.new_visitor_location);
        whomToMeet = (EditText) findViewById(R.id.new_visitor_meeting_person);
        purposeOfVisit = (EditText) findViewById(R.id.new_visitor_purpose);
        submit = (Button) findViewById(R.id.new_visitor_submit_btn);
        //autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.new_visitor_autocomplete);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#161731")));
        setContentView(R.layout.activity_new_visitor_entry);
        checkSessionValidity();
        initializeItems();
/*
        whomToMeet.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        FindEmployeesApiCaller findEmployeesApiCaller = new FindEmployeesApiCaller();
                        String response=null;
                        List<String> matchedElements = new ArrayList<>();
                        try {
                            response = findEmployeesApiCaller.execute(s.toString()).get();


                            JSONArray jsonArray = new JSONArray(response);
                            for(int index=0;index<jsonArray.length();index++){
                                JSONObject jsonElement = jsonArray.getJSONObject(index);
                                String element = jsonElement.getString("matchingResult");
                                matchedElements.add(element);
                            }
                            String[] matchedData = matchedElements.toArray(new String[0]);

                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(NewVisitorEntry.this, android.R.layout.select_dialog_item, matchedData);
                            autoCompleteTextView.setThreshold(1);
                            autoCompleteTextView.setBackgroundColor(Color.BLACK);
                            autoCompleteTextView.setTextColor(Color.RED);
                            autoCompleteTextView.setAdapter(arrayAdapter);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                }
        );*/

        submit.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OTPApiCaller otpApiCaller = new OTPApiCaller();
                        String otpResponse = null;
                        try {
                            otpResponse = otpApiCaller.execute(contact.getText().toString()).get();
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        openOtpVerificationActivity(otpResponse);
                    }
                }
        );
    }

    private void openOtpVerificationActivity(String otpResponse) {
        Intent intent = new Intent(NewVisitorEntry.this, OTPVerification.class);
        intent.putExtra("name", name.getText().toString());
        intent.putExtra("contact", contact.getText().toString());
        intent.putExtra("govtId", govtId.getText().toString());
        intent.putExtra("comingFrom", comingFrom.getText().toString());
        intent.putExtra("whomToMeet", whomToMeet.getText().toString());
        intent.putExtra("purposeOfVisit", purposeOfVisit.getText().toString());
        intent.putExtra("guardId", sharedPreferences.getString("sessionId", "default"));
        if (otpResponse == null)
            Toast.makeText(NewVisitorEntry.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        else {
            intent.putExtra("otpGenerated", otpResponse);
            startActivity(intent);
            finish();
        }
    }

    private void checkSessionValidity() {
        sharedPreferences = getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if (sharedPreferences.getString("sessionId", "default").equals("default")) {
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
        if (item.getItemId() == R.id.change_password)
            Toast.makeText(this, "Change Password", Toast.LENGTH_SHORT).show();
        else if (item.getItemId() == R.id.logout)
            logout();
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
}
