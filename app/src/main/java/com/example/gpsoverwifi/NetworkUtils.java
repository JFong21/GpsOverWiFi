package com.example.gpsoverwifi;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkUtils {
    // need to set this to target IP
    private static final String SERVER_URL = "http://192.999.99.99:5000/location";

    // Send Data to Flask with HTTP POST
    public static void sendLocation(double latitude, double longitude) {
        // HTTP client, client
        OkHttpClient client = new OkHttpClient();
        // JSON object, json
        JSONObject json = new JSONObject();
        try {
            // add lat and long
            json.put("latitude", latitude);
            json.put("longitude", longitude);
        } catch (JSONException e) {
            // exception handling required
            Log.e("NetworkUtils", "Somehow JSON failed", e);
            return;
        }

        // Encodes the GPS data as JSON and puts into request
        RequestBody body = RequestBody.create(json.toString(),
                MediaType.get("application/json; charset=utf-8"));
        // Creates POST request and fills with GPS JSON data
        Request request = new Request.Builder()
                .url(SERVER_URL)
                .post(body)  // POST request
                .build();

        // Runs network request in a background thread.
        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    Log.e("Network", "Failed to send location! Response code: " + response.code());
                } else {
                    Log.d("Network", "Location sent successfully: " + response.body().string());
                }
            } catch (IOException e) {
                Log.e("Network", "Network request failed!", e);
            }
        }).start();
    }
}
