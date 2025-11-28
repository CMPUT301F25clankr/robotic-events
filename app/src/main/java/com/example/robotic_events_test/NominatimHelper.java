package com.example.robotic_events_test;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NominatimHelper {

    private static final String TAG = "NominatimHelper";
    private static final String NOMINATIM_API_URL = "https://nominatim.openstreetmap.org/search?format=json&q=";

    public interface NominatimCallback {
        void onCoordinatesResolved(Location location);
        void onError(String errorMessage);
    }

    public static void getCoordinatesFromAddress(String address, NominatimCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            HttpURLConnection urlConnection = null;
            try {
                String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
                URL url = new URL(NOMINATIM_API_URL + encodedAddress);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                JSONArray jsonArray = new JSONArray(stringBuilder.toString());
                if (jsonArray.length() > 0) {
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    double lat = jsonObject.getDouble("lat");
                    double lon = jsonObject.getDouble("lon");

                    Location location = new Location("nominatim");
                    location.setLatitude(lat);
                    location.setLongitude(lon);

                    handler.post(() -> callback.onCoordinatesResolved(location));
                } else {
                    handler.post(() -> callback.onError("Address not found"));
                }

            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error getting coordinates from Nominatim", e);
                handler.post(() -> callback.onError("Error getting coordinates"));
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        });
    }
}
