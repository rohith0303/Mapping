package com.rohith.mapping;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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

import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    private MapView mapView;
    private GoogleMap googleMap;
    private PolylineOptions polylineOptions;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationManager locationManager;
    private boolean isTracking = false;
    private static final String POLYLINE_PREF_KEY = "polyline_coordinates";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request location permissions
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Initialize map view, location manager, and shared preferences
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);

        // Load previously saved polylines when the app starts
        loadPolylineFromPrefs();

        // Initialize buttons and their click listeners
        Button startTrackingButton = findViewById(R.id.startTrackingButton);
        startTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isTracking = true;
                requestLocationUpdates();
            }
        });

        Button clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAllLines();
            }
        });

        Button stopTrackingButton = findViewById(R.id.stopTrackingButton);
        stopTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isTracking = false;
                locationManager.removeUpdates(MainActivity.this);
            }
        });

         } else {
            Toast.makeText(this, "Location permission is needed to display your location on the map.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearAllLines() {
        if (googleMap != null && polylineOptions != null) {
            googleMap.clear();
            polylineOptions = null;
            // Clear saved polyline coordinates
            sharedPreferences.edit().remove(POLYLINE_PREF_KEY).apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (isTracking) {
            requestLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        locationManager.removeUpdates(this);
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Handle the case when permissions are not granted.
            return;
        }
        this.googleMap.setMyLocationEnabled(true);
        loadPolylineFromPrefs();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isTracking) {
            if (googleMap != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng currentLocation = new LatLng(latitude, longitude);

                if (polylineOptions == null) {
                    polylineOptions = new PolylineOptions();
                    polylineOptions.width(5);
                    polylineOptions.color(Color.BLUE);
                }

                polylineOptions.add(currentLocation);
                googleMap.addPolyline(polylineOptions);
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

                // Save updated polyline coordinates
                savePolylineToPrefs();
            }
        }
    }

    private void savePolylineToPrefs() {
        if (polylineOptions != null) {
            List<LatLng> points = polylineOptions.getPoints();
            StringBuilder stringBuilder = new StringBuilder();

            for (LatLng point : points) {
                stringBuilder.append(point.latitude).append(",").append(point.longitude).append(";");
            }

            sharedPreferences.edit().putString(POLYLINE_PREF_KEY, stringBuilder.toString()).apply();
        }
    }

    private void loadPolylineFromPrefs() {
        String polylineString = sharedPreferences.getString(POLYLINE_PREF_KEY, "");
        if (!polylineString.isEmpty()) {
            String[] pointsArray = polylineString.split(";");
            polylineOptions = new PolylineOptions();
            polylineOptions.width(5);
            polylineOptions.color(Color.BLUE);

            for (String pointString : pointsArray) {
                String[] latLng = pointString.split(",");
                double latitude = Double.parseDouble(latLng[0]);
                double longitude = Double.parseDouble(latLng[1]);
                LatLng point = new LatLng(latitude, longitude);
                polylineOptions.add(point);
            }

            if (googleMap != null) {
                googleMap.addPolyline(polylineOptions);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isTracking) {
                    requestLocationUpdates();
                }
            } else {
                Toast.makeText(this, "Location permission is needed to display your location on the map.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
