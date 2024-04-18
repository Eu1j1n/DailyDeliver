package com.example.dailydeliver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dailydeliver.Activity.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    String TAG = "스플래시 액티비티";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.animation);




        ImageView imageView = findViewById(R.id.imageView);

        imageView.startAnimation(fadeInAnimation);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        },3000); // 2초 있다 메인액티비티로



    }
}