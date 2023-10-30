package com.example.scooter2;

import android.Manifest;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 100;
    TextView status;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        ActivityCompat.requestPermissions(this, new String[]{Manifest
                .permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);


    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        BitmapDescriptor highlightedIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        mMap = googleMap;
        LatLng location = new LatLng(36.7726, 126.9347);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(36.7702,126.9332), 16));
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
