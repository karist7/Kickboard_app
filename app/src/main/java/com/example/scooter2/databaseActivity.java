package com.example.scooter2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.text.SimpleDateFormat;
/*
class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "abnormalState.db";
    private static final int DATABASE_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        // | 일련 번호 (1씩 증가) - INT | 날짜 - TEXT | 위도 | 경도 |
        db.execSQL("CREATE TABLE abnormalStates (_id INTEGER PRIMARY KEY " +
                " AUTOINCREMENT, time TEXT, latitude REAL, longitude REAL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 데이터베이스의 버전이 증가되었을 때 호출
        // 테이블의 데이터를 백업하고 기존 테이블을 삭제한 후 다시 불러와 저장한다
    }
}


public class databaseActivity extends AppCompatActivity {
    DBHelper helper;
    SQLiteDatabase db;
    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = new DBHelper(this);
        try {
            db = helper.getWritableDatabase();
        } catch (SQLException e) {
            db = helper.getReadableDatabase();
        }
    }

    public void insert() {
        // 권한 요청
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permission = new String[2];
            permission[0] = Manifest.permission.ACCESS_COARSE_LOCATION;
            permission[1] = Manifest.permission.ACCESS_FINE_LOCATION;
            requestPermissions(permission, 1);
            return;
        }
        Location loc_Current = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String time = simpleDateFormat.toString();
        double latitude = loc_Current.getLatitude();
        double longitude = loc_Current.getLongitude();

        db.execSQL("INSERT INTO abnormalStates VALUES (null, '"+ time +"', '"+ latitude + "', '" +longitude+"');");
    }

}
*/
