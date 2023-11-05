package com.example.scooter2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.scooter2.server.Markers;
import com.example.scooter2.server.RetrofitManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriveMapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SensorEventListener {
    TextView text;
    RetrofitManager retrofitManager = new RetrofitManager();
    ArrayList<Double> latitudeArrayList=new ArrayList<>();
    ArrayList<Double> longitudeArrayList=new ArrayList<>();
    ArrayList<Double> latitudeList;
    ArrayList<Double> longitudeList;

    FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 10;
    Button button;
    boolean buttonF = false;
    int i = 1;

    // TimerHandler : 지정한 시간만큼 뒤에 메세지를 전송
    //                타이머를 수시로 시작 & 중지해야하고 정확한 시간 간격을 필요로 하지 않을 때 사용
    TimerHandler timerHandler = new TimerHandler();
    private static final int MESSAGE_TIMER_START = 100;
    private static final int MESSAGE_TIMER_COMPLETE = 101;
    private static final int MESSAGE_TIMER_STOP = 102;
    private boolean flag = false;

    //senser
    private SensorManager mSensorManager = null;
    private Sensor mGyroscopeSensor = null;
    private Sensor mAccelerometer = null;
    UserSensorListner userSensorListner;

    // Sensor variables
    private float[] mGyroValues = new float[3];
    private float[] mAccValues = new float[3];
    private double mAccPitch, mAccRoll;
    private final float a = 0.2f;
    private static final float NS2S = 1.0f/1000000000.0f;
    private double pitch = 0, roll = 0;
    private double timestamp;
    private double dt;
    private double temp;
    private boolean gyroRunning;
    private boolean accRunning;
    private double currentRoll, currentPitch;
    public void onAccuracyChanged(Sensor sensor, int accuracy){}
    public void onSensorChanged(SensorEvent event){}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivemap);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.drive_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }
        button=findViewById(R.id.finish_button);
        loadMarker();
        text = findViewById(R.id.drivemap_text);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
                startActivity(intent);
                finish();

            }
        });
        userSensorListner = new UserSensorListner();
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccelerometer= mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAccelerometer != null){
            mSensorManager.registerListener(userSensorListner, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(mGyroscopeSensor != null){
            mSensorManager.registerListener(userSensorListner, mGyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
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
        locationRequest.setInterval(1000); // 1초마다 위치 업데이트를 요청
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
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

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(new LocationRequest(), new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        double lat = Math.round(location.getLatitude() * 10000.0) / 10000.0;
                        double log = Math.round(location.getLongitude() * 10000.0) / 10000.0;

                        for(int i=0;i<latitudeArrayList.size();i++) {
                            double checkLat = latitudeArrayList.get(i);
                            double checkLog = longitudeArrayList.get(i);


                            if(lat>=checkLat-0.0001 && lat<=checkLat+0.0001 && log>=checkLog-0.0001 && log<=checkLog+0.0001){


                                text.setText("사고 다발 구역입니다.");
                                text.setTextColor(Color.parseColor("#FF0000"));
                            }
                            else{
                                text.setText("안전 구역입니다.");
                                text.setTextColor(Color.parseColor("#90EE90"));
                            }
                        }



                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                    }
                }

            }, null);
        }
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // 원하는 동작 수행
        // 예시: 뒤로가기 버튼 동작 막기
        // super.onBackPressed(); // 이 부분을 주석 처리하거나 삭제하여 뒤로가기 버튼을 막을 수 있습니다.

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
                                ActivityCompat.requestPermissions(DriveMapActivity.this,
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
                    latitudeList=new ArrayList<>();
                    longitudeList = new ArrayList<>();
                    List<Markers> markerList = response.body();
                    for(Markers markers :markerList){
                        double lat = Double.parseDouble(markers.getLatitude());
                        double log = Double.parseDouble(markers.getLongitude());
                        latitudeList.add(lat);
                        longitudeList.add(log);
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

    @Override
    protected void onStart(){
        super.onStart();
    }
    @Override
    protected void onStop(){
        super.onStop();
    }


    private void complementaty(double new_ts){

        /* 자이로랑 가속 해제 */
        gyroRunning = false;
        accRunning = false;

        /*센서 값 첫 출력시 dt(=timestamp - event.timestamp)에 오차가 생기므로 처음엔 break */
        if(timestamp == 0){
            timestamp = new_ts;
            return;
        }
        dt = (new_ts - timestamp) * NS2S; // ns->s 변환
        timestamp = new_ts;

        /* degree measure for accelerometer */
        mAccPitch = -Math.atan2(mAccValues[0], mAccValues[2]) * 180.0 / Math.PI; // Y 축 기준
        mAccRoll= Math.atan2(mAccValues[1], mAccValues[2]) * 180.0 / Math.PI; // X 축 기준

        /**
         * 1st complementary filter.
         *  mGyroValuess : 각속도 성분.
         *  mAccPitch : 가속도계를 통해 얻어낸 회전각.
         */
        temp = (1/a) * (mAccPitch - pitch) + mGyroValues[1];
        pitch = pitch + (temp*dt);

        temp = (1/a) * (mAccRoll - roll) + mGyroValues[0];
        roll = roll + (temp*dt);

        text.setText("roll : "+String.format("%.2f", roll)+"\t\t\tpitch : "+String.format("%.2f", pitch));

        /*
        if(( 70 < roll && roll < 180 && -180 < pitch && pitch < -80 ) ||
        ( -20 < roll && roll < 0 && 60 < pitch && pitch < 90 ) ||
        ( -180 < roll && roll < -70 && 70 < pitch && pitch < 180 ) ||
        ( 140 < Math.abs(roll) && 140 < Math.abs(pitch) ) ||
        ( -90 < roll && roll < -50 && 0 < pitch && pitch < -20 ) ||
        ( -20 < roll && roll < 10 && -90 < pitch && pitch < -50 ) )
         */

        if( (70 < Math.abs(roll) && Math.abs(roll) < 180 && 70 < Math.abs(pitch) && Math.abs(pitch) < 180) ||
                (0 < Math.abs(roll) && Math.abs(roll) < 20 && 50 < Math.abs(pitch) && Math.abs(pitch) < 90) ||
                ( -90 < roll && roll < -50 && 0 < pitch && pitch < -20 )){

            if(flag == false){
                flag = true;
                timerHandler.removeMessages(MESSAGE_TIMER_STOP);
                timerHandler.sendEmptyMessage(MESSAGE_TIMER_START);
            }
        }
        else{

            if(flag == true){
                flag = false;
                timerHandler.removeMessages(MESSAGE_TIMER_COMPLETE);
                timerHandler.removeMessages(MESSAGE_TIMER_START);
                timerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
            }
        }
    }
    public class UserSensorListner implements SensorEventListener{

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()){

                /** GYROSCOPE */
                case Sensor.TYPE_GYROSCOPE:

                    /*센서 값을 mGyroValues에 저장*/
                    mGyroValues = event.values;

                    if(!gyroRunning)
                        gyroRunning = true;
                    break;

                /** ACCELEROMETER */
                case Sensor.TYPE_ACCELEROMETER:

                    /*센서 값을 mAccValues에 저장*/
                    mAccValues = event.values;

                    if(!accRunning)
                        accRunning = true;
                    break;

            }

            /**두 센서 새로운 값을 받으면 상보필터 적용*/
            if(gyroRunning && accRunning){
                complementaty(event.timestamp);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    }

    public class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case MESSAGE_TIMER_START:
                    Log.d("JiroTest","신고대기");
                    this.sendEmptyMessageDelayed(MESSAGE_TIMER_COMPLETE, 5000);
                    break;
                case MESSAGE_TIMER_COMPLETE:
                    Log.d("JiroTest","신고");
                    this.removeMessages(MESSAGE_TIMER_COMPLETE);
                    break;
                case MESSAGE_TIMER_STOP:
                    Log.d("JiroTest","안전");
                    break;
            }
        }
    }

}

