package com.example.scooter2;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.text.SimpleDateFormat;


public class sensorActivity extends AppCompatActivity implements SensorEventListener {
    // TimerHandler : 지정한 시간만큼 뒤에 메세지를 전송
    //                타이머를 수시로 시작 & 중지해야하고 정확한 시간 간격을 필요로 하지 않을 때 사용
    TimerHandler timerHandler = new TimerHandler();
    private static final int MESSAGE_TIMER_START = 100;
    private static final int MESSAGE_TIMER_COMPLETE = 101;
    private static final int MESSAGE_TIMER_STOP = 102;
    private boolean flag = false;
    private int count = 0;

    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
    private static final float NS2S = 1.0f / 1000000000.0f;
    private double pitch = 0, roll = 0;
    private double timestamp;
    private double dt;
    private double temp;

    private boolean gyroRunning;
    private boolean accRunning;


    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivemap);

        // 센서 매니저
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        userSensorListner = new UserSensorListner();

    }

    // drive 시작 하면 센서 자동으로 켜짐
    @Override
    protected void onStart() {
        super.onStart();
        if (mAccelerometer != null) {
            mSensorManager.registerListener(userSensorListner, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mGyroscopeSensor != null) {
            mSensorManager.registerListener(userSensorListner, mGyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    //drive 화면에서 나가면 자동으로 꺼짐
    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(userSensorListner);
    }


    // 상보필터 적용 과정
    private void complementaty(double new_ts) {
        /* 자이로랑 가속 해제 */
        gyroRunning = false;
        accRunning = false;

        /*센서 값 첫 출력시 dt(=timestamp - event.timestamp)에 오차가 생기므로 처음엔 break */
        if (timestamp == 0) {
            timestamp = new_ts;
            return;
        }
        dt = (new_ts - timestamp) * NS2S; // ns->s 변환
        timestamp = new_ts;

        /* degree measure for accelerometer */
        mAccPitch = -Math.atan2(mAccValues[0], mAccValues[2]) * 180.0 / Math.PI; // Y 축 기준
        mAccRoll = Math.atan2(mAccValues[1], mAccValues[2]) * 180.0 / Math.PI; // X 축 기준

        /**
         * 1st complementary filter.
         *  mGyroValuess : 각속도 성분.
         *  mAccPitch : 가속도계를 통해 얻어낸 회전각.
         */
        temp = (1 / a) * (mAccPitch - pitch) + mGyroValues[1];
        pitch = pitch + (temp * dt);

        temp = (1 / a) * (mAccRoll - roll) + mGyroValues[0];
        roll = roll + (temp * dt);

        // 비정상 반응
        if ((70 < Math.abs(roll) && Math.abs(roll) < 180 && 70 < Math.abs(pitch) && Math.abs(pitch) < 180) ||
                (0 < Math.abs(roll) && Math.abs(roll) < 20 && 50 < Math.abs(pitch) && Math.abs(pitch) < 90) ||
                (-90 < roll && roll < -50 && 0 < pitch && pitch < -20)) {

            // 5초 대기 타이머 가동
            if (flag == false) {
                flag = true;
                timerHandler.removeMessages(MESSAGE_TIMER_STOP);
                timerHandler.sendEmptyMessage(MESSAGE_TIMER_START);
            }
        }

        // 정상 반응
        else {
            if (flag == true) {
                flag = false;
                timerHandler.removeMessages(MESSAGE_TIMER_COMPLETE);
                timerHandler.removeMessages(MESSAGE_TIMER_START);
                timerHandler.sendEmptyMessage(MESSAGE_TIMER_STOP);
            }
        }

    }

    // 센서 값을 불러와 필터 적용
    public class UserSensorListner implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {

                /** GYROSCOPE */
                case Sensor.TYPE_GYROSCOPE:
                    /*센서 값을 mGyroValues에 저장*/
                    mGyroValues = event.values;
                    if (!gyroRunning)
                        gyroRunning = true;
                    break;


                /** ACCELEROMETER */
                case Sensor.TYPE_ACCELEROMETER:
                    /*센서 값을 mAccValues에 저장*/
                    mAccValues = event.values;
                    if (!accRunning)
                        accRunning = true;
                    break;

            }

            /**두 센서 새로운 값을 받으면 상보필터 적용*/
            if (gyroRunning && accRunning) {
                complementaty(event.timestamp);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }


    // 타이머 실행
    public class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                // 대기 (5초)
                case MESSAGE_TIMER_START:
                    this.sendEmptyMessageDelayed(MESSAGE_TIMER_COMPLETE, 5000);
                    break;

                // 비정상 상태가 5초가 지나 사고 상태로 인지
                case MESSAGE_TIMER_COMPLETE:

                    // 사고 위치 서버에 전송
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        String[] permission = new String[2];
                        permission[0] = Manifest.permission.ACCESS_COARSE_LOCATION;
                        permission[1] = Manifest.permission.ACCESS_FINE_LOCATION;
                        requestPermissions(permission, 1);
                        return;
                    }
                    Location loc_Current = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    // 발생 시간
                    String time = simpleDateFormat.toString();
                    // 위도
                    double latitude = loc_Current.getLatitude();
                    // 경도
                    double longitude = loc_Current.getLongitude();
                    // 횟수
                    count++;

                    //**************************************************************************************
                    //전송



                    //**************************************************************************************
                    this.removeMessages(MESSAGE_TIMER_COMPLETE);
                    break;

                // 안전한 상태
                case MESSAGE_TIMER_STOP:
                    break;
            }
        }
    }

}
