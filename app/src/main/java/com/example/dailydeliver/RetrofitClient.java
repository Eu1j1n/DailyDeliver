package com.example.dailydeliver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    private static final int TIMEOUT_SECONDS = 30; // 타임아웃 시간 (초)
    private static final String BASE_URL = "http://43.201.32.122";

    public static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .build();

            Gson gson = new GsonBuilder().setLenient().create(); // Gson 객체 생성 및 설정 추가

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson)) // Gson 설정 추가
                    .build();
        }
        return retrofit;
    }


    public static Retrofit getClient() {
        return getClient(BASE_URL);
    }
}

