package com.example.android.myguardian;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Collection of static methods that help to retrieve and parse date from Guardian web servers.
 */

class QueryUtils {
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Fetches HTTP response from the given URL
     *
     * @param queryUrl the URL used for sending request to the server
     * @return response received from the server
     */
    static String fetchNewsData(String queryUrl) {
        String response = null;

        try {
            URL url = new URL(queryUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            if (httpURLConnection.getResponseCode() == 200) {
                InputStream inputStream =
                        new BufferedInputStream(httpURLConnection.getInputStream());
                response = convertStreamToString(inputStream);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error creating URL: " + e.getMessage());
        }

        return response;
    }

    /**
     * Parses the given JSON string into a List of News objects
     *
     * @param newsJson the JSON string to parse
     * @return a list of News objects
     */
    static List<News> parseNews(String newsJson) {
        // if the JSON String is empty or null, then return early
        if (TextUtils.isEmpty(newsJson)) {
            return null;
        }

        List<News> newsList = new ArrayList<>();

        try {
            JSONObject rootJson = new JSONObject(newsJson);
            JSONObject response = rootJson.getJSONObject("response");
            JSONArray results = response.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject newsData = results.getJSONObject(i);
                String sectionName = newsData.getString("sectionName");
                String webPublicationDate = newsData.getString("webPublicationDate");
                String webTitle = newsData.getString("webTitle");
                String webUrl = newsData.getString("webUrl");

                newsList.add(new News(sectionName, webPublicationDate, webTitle, webUrl));
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing JSON: " + e.getMessage());
        }

        return newsList;
    }

    // helper method that converts an inputStream to String object with the help of BufferedReader
    // and StringBuilder
    private static String convertStreamToString(InputStream inputStream) {
        if (inputStream != null) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            try {
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return stringBuilder.toString();

        } else {
            return "";
        }
    }
}
