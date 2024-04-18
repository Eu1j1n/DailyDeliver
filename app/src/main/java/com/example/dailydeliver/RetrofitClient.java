package com.example.dailydeliver;

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
                    .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS) // 연결 시간 초과
                    .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS) // 읽기 시간 초과
                    .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS) // 쓰기 시간 초과
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client) // OkHttpClient 설정 추가
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getClient() {
        return getClient(BASE_URL);
    }
}

