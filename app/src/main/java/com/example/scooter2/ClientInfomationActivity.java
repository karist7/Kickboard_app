package com.example.scooter2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ClientInfomationActivity extends AppCompatActivity {
    private double accuracy=0.0,latitude=0.0, longitude=0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_infomation);

        Button use_time_button = findViewById(R.id.use_time_button);
        TextView textView = findViewById(R.id.info_name);
        textView.setText(MainActivity.name + "님");
        TextView phoneText = findViewById(R.id.phone_num);
        phoneText.setText(MainActivity.phone);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Location loc_Current = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc_Current!=null){
            accuracy = loc_Current.getAccuracy();
            longitude = loc_Current.getLongitude();
            latitude = loc_Current.getLatitude();

        }
        String address = LocationHelper.getDetailedAddressFromLocation(getApplicationContext(), latitude, longitude);
        TextView locationText = findViewById(R.id.location_name);
        locationText.setText(address);
        use_time_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UseHistoryActivity.class);
                startActivity(intent);

            }
        });

    }
}
