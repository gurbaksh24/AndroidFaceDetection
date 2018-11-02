package com.tavisca.taviscavisitor;

import android.os.AsyncTask;
import android.os.StrictMode;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class LogoutApiCaller extends AsyncTask<String, Void, String> {
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
    protected String doInBackground(String... employeeData) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        URL apiEndPoint = null;
        try {

            apiEndPoint = new URL("http://taviscaemployeevisitor-dev.ap-south-1.elasticbeanstalk.com/api/User/Logout");

            HttpURLConnection httpURLConnection = (HttpURLConnection) apiEndPoint.openConnection();
            httpURLConnection.setRequestMethod("PUT");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");

            OutputStream outStream = httpURLConnection.getOutputStream();
            OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("UserInput", employeeData[0]);

            outStreamWriter.write(String.valueOf(jsonObject));
            outStreamWriter.flush();
            outStreamWriter.close();
            outStream.close();
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.connect();

            if (httpURLConnection.getResponseCode() == 200) {
                InputStream responseBody = httpURLConnection.getInputStream();
                String response = readFromStream(responseBody);
                return response;
            }
            httpURLConnection.disconnect();
            return null;
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
