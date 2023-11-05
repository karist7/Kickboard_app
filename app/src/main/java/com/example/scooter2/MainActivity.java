package com.example.scooter2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ImageView profile_icon;
    private TextView hello_text;
    private TextView name_text;
    private ImageButton imageButton;
    public static String name="";
    public static String phone="";
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent=getIntent();

        ImageButton login_button = findViewById(R.id.login_button);


        name_text = findViewById(R.id.client_name);
        if (intent != null && intent.hasExtra("name")){
            name = intent.getStringExtra("name");
            phone = intent.getStringExtra("phone");
            name_text.setText(name+"님");
            Log.d("nameTest",name);

        }
        if(name!=""){
            name_text.setText(name+"님");
        }
        login_button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
            }

        });
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        final View drawView = (View) findViewById(R.id.drawer);


        // 드로어 화면을 열고 닫을 버튼 객체 참조
        ImageButton btnOpenDrawer = (ImageButton) findViewById(R.id.menu_button);
        // 드로어 여는 버튼 리스너
        btnOpenDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawView);
            }
        });
        ImageButton dangerPlaceButton = findViewById(R.id.Danger_place_button);
        dangerPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),DriveMapActivity.class);
                startActivity(intent);
            }
        });

        imageButton = findViewById(R.id.loadmap_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name=="")
                    Toast.makeText(MainActivity.this, "로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
                else{
                    Intent intent=new Intent(getApplicationContext(),CameraActivity.class);
                    startActivity(intent);
                }

            }
        });
        Button profile_button = findViewById(R.id.profile_button);
        profile_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(name=="")
                    Toast.makeText(MainActivity.this, "로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent(getApplicationContext(), ClientInfomationActivity.class);
                    startActivity(intent);
                }
            }
        });
        Button use_time_button = findViewById(R.id.use_time_button);
        use_time_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name=="")
                       Toast.makeText(MainActivity.this, "로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show();
                else{
                    Intent intent = new Intent(getApplicationContext(), UseHistoryActivity.class);
                    startActivity(intent);
                }

            }
        });

    }

}