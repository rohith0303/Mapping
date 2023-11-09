package com.rohith.mapping;

import android.graphics.Color;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap googleMap;
    private PolylineOptions polylineOptions;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Request location permissions
        requestLocationPermissions();

        // Initialize PolylineOptions
        polylineOptions = new PolylineOptions();

        // Start Tracking Button Click
        Button startTrackingButton = findViewById(R.id.startTrackingButton);
        startTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Clear previous polyline if any
                polylineOptions = new PolylineOptions();
                polylineOptions.width(5);  // Set the width of the polyline
                polylineOptions.color(Color.BLUE);  // Set the color of the polyline

                // Add the current location to the new polyline
                addCurrentLocationToPolyline();
            }
        });

        // Stop Tracking Button Click
        Button stopTrackingButton = findViewById(R.id.stopTrackingButton);
        stopTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addCurrentLocationToPolyline();

                // Draw the current polyline on the map
                if (googleMap != null) {
                    googleMap.addPolyline(polylineOptions);
                }
            }
        });
    }

    private void requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // Check if location permissions are granted
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Location permissions are granted, enable location tracking
            googleMap.setMyLocationEnabled(true);

            // Get the user's current location
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                // Get the latitude and longitude of the user's current location
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Move the camera to the user's current location
                LatLng currentLocation = new LatLng(latitude, longitude);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // The user granted location permissions, you can now enable location tracking
                if (googleMap != null) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    googleMap.setMyLocationEnabled(true);
                }
            } else {
                Toast.makeText(this, "Location permission is needed to display your location on the map.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addCurrentLocationToPolyline() {
        if (googleMap != null) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng currentLocation = new LatLng(latitude, longitude);

                // Move the camera to the user's current location
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

                // Add the current location to the polyline
                polylineOptions.add(currentLocation);
                Log.d("MyApp", "Adding location to polyline: " + currentLocation.toString());
            }
        }
    }
}
