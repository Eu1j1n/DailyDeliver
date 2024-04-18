package com.example.dailydeliver;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class KaKaoApplication extends Application {
    private  static KaKaoApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this,"20458c53f41feddbd90e4c7bc58dddda");


    }
}
