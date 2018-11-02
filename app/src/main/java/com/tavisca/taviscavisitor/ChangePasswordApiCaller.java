package com.tavisca.taviscavisitor;

import android.os.AsyncTask;
import android.os.StrictMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class ChangePasswordApiCaller extends AsyncTask<String, Void, String> {

    String response = null;

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    @Override
    protected String doInBackground(String... empty) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        URL apiEndPoint = null;
        try {
            apiEndPoint = new URL("http://visitortavisca-dev.ap-south-1.elasticbeanstalk.com/api/Guard");

            HttpURLConnection httpURLConnection = (HttpURLConnection) apiEndPoint.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 200) {
                InputStream responseBody = httpURLConnection.getInputStream();
                response = readFromStream(responseBody);
            } else {
                response = "Something went wrong";
            }
            httpURLConnection.disconnect();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        getResponse(response);
    }

    protected String getResponse(String response) {
        return response;
    }
}