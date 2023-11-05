package com.example.scooter2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.scooter2.server.Markers;
import com.example.scooter2.server.RetrofitManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    TextView text;
    RetrofitManager retrofitManager = new RetrofitManager();
    private double latitude, longitude;
    FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 10;
    ArrayList<Double> latitudeArrayList=new ArrayList<>();
    ArrayList<Double> longitudeArrayList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }
        loadMarker();
        text = findViewById(R.id.map_text);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildLocationRequest();
                startLocationUpdates();
                mMap.setMyLocationEnabled(true);
            } else {
                checkLocationPermission();
            }
        } else {
            buildLocationRequest();
            startLocationUpdates();
            mMap.setMyLocationEnabled(true);
        }
        mMap.setMinZoomPreference(15.0f);
    }

    private void buildLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(500); // 1초마다 위치 업데이트를 요청
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        double lat = Math.round(location.getLatitude() * 10000.0) / 10000.0;
                        double log = Math.round(location.getLongitude() * 10000.0) / 10000.0;
                        boolean flag=false;
                        for(int i=0;i<latitudeArrayList.size();i++) {
                            double checkLat = latitudeArrayList.get(i);
                            double checkLog = longitudeArrayList.get(i);

                            if (lat >= checkLat - 0.0001 && lat <= checkLat + 0.0001 && log >= checkLog - 0.0001 && log <= checkLog + 0.0001) {
                                flag = true;
                            }
                        }
                        if(flag){
                            text.setText("사고 다발 구역입니다.");
                            text.setTextColor(Color.parseColor("#FF0000"));
                        }
                        else{
                            text.setText("안전 구역입니다.");
                            text.setTextColor(Color.parseColor("#90EE90"));
                        }



                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                    }
                }
            }, null);
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(new LocationRequest(), new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

                    }
                }
            }, null);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("TAG", "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }
    public void loadMarker(){
        retrofitManager.getApiService().accident().enqueue(new Callback<List<Markers>>() {
            @Override
            public void onResponse(Call<List<Markers>> call, Response<List<Markers>> response) {
                if(response.isSuccessful()){
                    List<Markers> markerList = response.body();
                    for(Markers markers :markerList){

                        double lat = Math.round(Double.parseDouble(markers.getLatitude()) * 10000.0) / 10000.0;
                        double log = Math.round(Double.parseDouble(markers.getLongitude()) * 10000.0) / 10000.0;
                        latitudeArrayList.add(lat);
                        longitudeArrayList.add(log);
                        Log.d("Array", lat + " " + log);
                        LatLng location = new LatLng(lat,log);
                        mMap.addMarker(new MarkerOptions().position(location));

                    }
                }
            }

            @Override
            public void onFailure(Call<List<Markers>> call, Throwable t) {
                Log.d("loadMarkerError",t.toString());
            }
        });
    }
}
