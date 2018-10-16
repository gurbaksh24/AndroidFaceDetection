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

public class LoginClient extends AsyncTask<String, Void, String> {
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
    protected String doInBackground(String... employeeData) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        URL apiEndPoint = null;
        try {
            apiEndPoint = new URL("http://visitorsprojectapi-dev.ap-south-1.elasticbeanstalk.com/api/Guard/" + employeeData[0] + "/" + employeeData[1]);

            HttpURLConnection httpURLConnection = (HttpURLConnection) apiEndPoint.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.connect();

            if (httpURLConnection.getResponseCode() == 200) {
                InputStream responseBody = httpURLConnection.getInputStream();
                String response = readFromStream(responseBody);
                return response;
            }
            httpURLConnection.disconnect();
            return  null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }
}
