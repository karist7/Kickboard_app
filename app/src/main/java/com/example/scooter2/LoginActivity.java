package com.example.scooter2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.se.omapi.Session;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scooter2.server.RetrofitManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    EditText idText,pwdText;
    RetrofitManager retrofitManager = new RetrofitManager();
    Button login_btn;
    String set;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button moveButton = findViewById(R.id.find_password);
        idText = findViewById(R.id.input_id);
        pwdText = findViewById(R.id.input_password);
        login_btn = findViewById(R.id.login_button);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginProcess();

            }
        });
        moveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), FindPasswordActivity.class);
                startActivity(intent);
            }
        });

        Button moveButton2 = findViewById(R.id.join);
        moveButton2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    private void loginProcess(){
        String id = idText.getText().toString();
        String pwd = pwdText.getText().toString();
        if (id!=null && pwd!=null){
            loginAccount(id,pwd);
        }
    }
    private void loginAccount(final String id, final String pwd){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", id);
            jsonObject.put("password", pwd);

            // ... 다른 필드들
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        retrofitManager.getApiService().login(requestBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    if (response.code() == 200) {

                        try {
                            set = response.body().string();
                            Log.d("checkResponse","response "+response.body().string());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        Toast.makeText(LoginActivity.this, "로그인 성공.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        Log.d("set",set);
                        intent.putExtra("name",set);
                        startActivity(intent);
                        finish();

                    } else if (response.code() == 401) {
                        Toast.makeText(LoginActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(LoginActivity.this, "네트워크 오류입니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                Log.d("LoginError", t.getMessage());

                Log.d("responseBody",call.request().body().toString());
            }
        });
    }
}