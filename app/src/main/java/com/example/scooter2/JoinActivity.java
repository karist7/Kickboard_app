package com.example.scooter2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.scooter2.server.RetrofitManager;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinActivity extends AppCompatActivity {
    EditText editTextName,editTextPassword,editTextCheck,editTextEmail,editTextPhone;
    String name, pwd,check_pwd,email,phone;
    Button login_btn;
    RetrofitManager retrofitManager = new RetrofitManager();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        editTextName = findViewById(R.id.name);
        editTextPassword = findViewById(R.id.password);
        editTextCheck = findViewById(R.id.password_check);
        editTextEmail = findViewById(R.id.id);
        editTextPhone =findViewById(R.id.phone_num);
        login_btn = findViewById(R.id.register_button);


        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();

            }
        });
    }
    public void register(){
        name = editTextName.getText().toString();
        pwd = editTextPassword.getText().toString();
        check_pwd = editTextCheck.getText().toString();
        email = editTextEmail.getText().toString();
        phone = editTextPhone.getText().toString();
        if (isValid(email, pwd,check_pwd)) {
            regist(name,pwd,check_pwd,phone,email);

        } else {
            if(pwd.length()<6)
                Toast.makeText(JoinActivity.this, "비밀번호는 6자 이상입니다.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(JoinActivity.this, "올바른 이메일을 입력하세요.", Toast.LENGTH_SHORT).show();

        }
    }
    private boolean isValid(String email, String password, String check) {
        // 간단한 예제: 이메일 형식 및 비밀번호 길이 검사
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length() >= 6 && check.length() >= 6 ;
    }
    private void regist(final String name, final String password, final String check_pwd, final String phone, final String email){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("password", password);
            jsonObject.put("confirm_password", check_pwd);
            jsonObject.put("email", email);
            jsonObject.put("phone_number", phone);
            // ... 다른 필드들
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
        Log.d("errorcheck",jsonObject.toString());
        Log.d("errorcheck",requestBody.toString());
        retrofitManager.getApiService().regist(requestBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code()==201){
                    Toast.makeText(JoinActivity.this, "회원가입 완료", Toast.LENGTH_SHORT).show();
                    Log.d("RegisterSuccess", "회원가입 성공");
                    Intent result = new Intent(JoinActivity.this, MainActivity.class);
                    result.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(result);
                    finish();
                }
                else if(response.code()==202){
                    Log.d("RegisterError", "이메일 중복");
                    Toast.makeText(JoinActivity.this, "중복된 이메일입니다.", Toast.LENGTH_SHORT).show();
                }
                else if(response.code()==203){
                    Log.d("RegisterError", "두 비밀번호 불일치");
                    Toast.makeText(JoinActivity.this, "두 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("ResponseError",requestBody.toString());
                Log.d("LoginFail",t.toString());
            }
        });
    }
}
