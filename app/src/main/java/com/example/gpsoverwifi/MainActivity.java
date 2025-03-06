package com.example.gpsoverwifi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {
    // permission code for GPS
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Asks user for GPS permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Starts sending GPS data
            startLocationUpdates();
        }
    }

    // This will request GPS updates every second
    private void startLocationUpdates() {
        // log if gps permissions not received
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("GPS", "Location permission failed");
            return;
        }
        // set GPS request to high accuracy and interval of 1 sec
        LocationRequest locationRequest = LocationRequest.create()
                // this just makes it only use phone GPS and not wifi
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // Use GPS
                .setInterval(1000) // 1 second interval
                .setFastestInterval(1000);

        // locationCallback triggered upon new GPS location
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // Checks if it is valid GPS data
                if (locationResult == null) {
                    Log.e("GPS", "Not location data");
                    return;
                }
                // for each location
                for (Location location : locationResult.getLocations()) {
                    // get lat and long
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    // log them (removable)
                    Log.d("GPS", "Lat: " + latitude + ", Long: " + longitude);

                    // send GPS data to server
                    NetworkUtils.sendLocation(latitude, longitude);
                }
            }
        };
        // starts requesting GPS sends to locationCallback
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // KILL THE GPS on app close
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    // if we don't have GPS permission call this
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // if success
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // log
            Log.d("GPS", "Permission granted!");
            startLocationUpdates();
        } else {
            // else we lose and log
            Log.e("GPS", "Permission denied!");
        }
    }
}
