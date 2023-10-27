package com.example.scooter2.server;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {
    private Retrofit retrofit;
    private ApiService apiService;

    Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl("https://5452-220-69-208-115.ngrok-free.app")
            .addConverterFactory(GsonConverterFactory.create());

    public RetrofitManager() {
        retrofit = builder.build();
        apiService = retrofit.create(ApiService.class);
    }

    public ApiService getApiService() {
        return apiService;
    }
}
