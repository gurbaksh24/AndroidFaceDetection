package com.tavisca.taviscavisitor;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginClient extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... employeeData) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        URL githubEndpoint = null;
        try {
            githubEndpoint = new URL("http://172.16.14.84:58210/api/Guard/1/1");

            // Create\
            // ]connection
            HttpURLConnection myConnection = (HttpURLConnection) githubEndpoint.openConnection();
            // myConnection.setRequestProperty("guardId", "960");
            // myConnection.setRequestProperty("password", "12345");
            if (myConnection.getResponseCode() == 200) {
                // Success
                // Further processing here
                InputStream responseBody = myConnection.getInputStream();
                InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                String response = responseBodyReader.toString();
                Log.d("Rest Response",response);
                                     /*
                                        JsonReader jsonReader = new JsonReader(responseBodyReader);
                                        jsonReader.beginObject(); // Start processing the JSON object
                                        while (jsonReader.hasNext()) { // Loop through all keys
                                            String key = jsonReader.nextName(); // Fetch the next key
                                            if (key.equals("organization_url")) { // Check if desired key
                                                // Fetch the value as a String
                                                String value = jsonReader.nextString();

                                                // Do something with the value
                                                // ...

                                                break; // Break out of the loop
                                            } else {
                                                jsonReader.skipValue(); // Skip values of other keys
                                            }
                                        }
                                        */
            return response;
            } else {
                // Error handling code goes here
            }
            myConnection.disconnect();
            return  null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }
}
