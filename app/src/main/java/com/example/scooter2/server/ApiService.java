package com.example.scooter2.server;

import org.json.JSONObject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("/checkHelmet/")
    Call<ResponseBody> requestPhoto(
            @Part  MultipartBody.Part photo);

    @POST("/signup")
    Call<ResponseBody> regist(
            @Body RequestBody requestBody
            );
    @POST("/signin/")
    Call<ResponseBody> login(
            @Body RequestBody requestBody
    );

    @POST("/start/")
    Call<ResponseBody> startUp(
            @Body RequestBody requestBody
    );
    @POST("/start/")
    Call<JSONObject> startUpTest(
            @Body RequestBody requestBody
    );

    @POST("/sendaccident/")
    Call<List<Markers>> accident();
}
