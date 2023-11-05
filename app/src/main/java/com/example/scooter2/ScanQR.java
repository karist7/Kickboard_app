package com.example.scooter2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.scooter2.server.RetrofitManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanQR extends AppCompatActivity {
    RetrofitManager retrofitManager = new RetrofitManager();
    String start="";
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent=getIntent();

        new IntentIntegrator(this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();

            } else {
                start = result.getContents();
                Log.d("ScanQRTEST",start);

                startProcess(start);

                finish();


            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void startProcess(final String start){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("start", start);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Log.d("requestTest",jsonObject.toString());
        retrofitManager.getApiService().startUp(requestBody).enqueue(new Callback<ResponseBody>(){

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code()==201){

                    try {
                        Log.d("rsponseData",response.body().string());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(ScanQR.this, "운행을 시작합니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), DriveMapActivity.class);
                    startActivity(intent);
                }
                if(response.code()==202){
                    try {
                        Log.d("rsponseData",response.body().string());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(ScanQR.this, "운행을 종료합니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("qrFail",t.toString());
            }
        });
    }
}
