package com.example.dailydeliver.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.example.dailydeliver.Chatting.ChatFragment;
import com.example.dailydeliver.Fragment.HomeFragment;
import com.example.dailydeliver.R;
import com.example.dailydeliver.WishListFragment;
import com.example.dailydeliver.profile.UserProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    Button btn1, btn2, btn3, btn4;

    BottomNavigationView bottomNavigationView;

    HomeFragment homeFragment;
    UserProfileFragment userProfileFragment;

    ChatFragment chatFragment;

    WishListFragment wishListFragment;

    String TAG = "메인 액티비티";

    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragmentbottombar);





        // 하단 네비게이션 바 초기화
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        Intent intent = getIntent();
        String loginType = intent.getStringExtra("logintype");
        String receivedID = intent.getStringExtra("아이디 값");

        String kakaoNickName = intent.getStringExtra("nickname");
        String profileImageUrl = intent.getStringExtra("profileImageUrl");
        Log.d(TAG, "그냥 값" + loginType + kakaoNickName + profileImageUrl);

        homeFragment = new HomeFragment();
        userProfileFragment = new UserProfileFragment();
        chatFragment = new ChatFragment();
        wishListFragment = new WishListFragment();

        Bundle homeBundle = new Bundle();
        homeBundle.putString("receivedID", receivedID);
        homeFragment.setArguments(homeBundle);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame, homeFragment).commit();

        // 하단 네비게이션 바 아이템 선택 리스너 설정
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.bottom_home:
                        selectedFragment = homeFragment;
                        Bundle homeBundle = new Bundle();
                        homeBundle.putString("receivedID", receivedID);
                        Log.d(TAG, "onNavigationItemSelected: " + receivedID);
                        chatFragment.setArguments(homeBundle);
                        break;
                    case R.id.bottom_chat:
                        selectedFragment = chatFragment;
                        Bundle bundle = new Bundle();
                        bundle.putString("kakaoUrl", profileImageUrl);
                        bundle.putString("loginType", loginType);
                        bundle.putString("receivedID", receivedID);
                        chatFragment.setArguments(bundle);
                        break;
                    case R.id.bottom_wish:
                        selectedFragment = wishListFragment;
                        Bundle wishBundle = new Bundle();
                        wishBundle.putString("receivedID", receivedID);
                        chatFragment.setArguments(wishBundle);

                        break;
                    case R.id.bottom_mypage:
                        // 마이페이지로 이동
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        UserProfileFragment userProfileFragment = UserProfileFragment.newInstance(loginType, receivedID, kakaoNickName, profileImageUrl);
                        selectedFragment = userProfileFragment;
                        break;
                }
                // 선택한 프래그먼트를 표시
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, selectedFragment).commit();
                return true;
            }
        });
    }


}
