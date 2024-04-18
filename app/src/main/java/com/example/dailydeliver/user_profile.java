package com.example.dailydeliver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.dailydeliver.Activity.LoginActivity;
import com.kakao.sdk.user.UserApiClient;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class user_profile extends AppCompatActivity {

    CircleImageView profileImage;

    TextView nickName;

    Button kakaologoutButton, logoutButton;

    String id;

    String TAG = "프로필 액티비티";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        profileImage = findViewById(R.id.profileImage);
        nickName= findViewById(R.id.nickname);
        kakaologoutButton = findViewById(R.id.kakaoLogout);
        logoutButton = findViewById(R.id.Logout);






        boolean isKakaoLoggedIn = UserApiClient.getInstance().isKakaoTalkLoginAvailable(this);

        Intent intent = getIntent();
        String loginType = intent.getStringExtra("logintype");
        String receivedID = intent.getStringExtra("아이디 값");

        Log.d(TAG, "logintType 값" + loginType);
        Log.d(TAG, "receivedID의 값" + receivedID);

        String imagePathFromServer = "http://52.79.88.52/newImage/" + receivedID + ".jpg"; // 서버에서 받아온 이미지 URL

        // 카카오톡 로그인에서 받아온 이미지 URL
        String kakaoProfileImageUrl = intent.getStringExtra("profileImageUrl");

        // 기본 이미지 리소스 (drawable 폴더에 저장한 기본 이미지)
        int defaultImageResource = R.drawable.profile;

        if ("kakao".equals(loginType)) {
            Log.d(TAG, "카카오 로그인 들어옴?");
            // 카카오 로그인일 경우
            kakaologoutButton.setVisibility(View.VISIBLE); // 로그아웃 버튼 보이게 설정
            logoutButton.setVisibility(View.GONE);
            Glide.with(this)
                    .load(kakaoProfileImageUrl)
                    .into(profileImage);
            String nickname = intent.getStringExtra("nickname");
            Log.d(TAG, "카카오 프로필" + kakaoProfileImageUrl);

            Log.d(TAG, "nickname 값" + nickname);
            nickName.setText(nickname);// 다른 로그아웃 버튼 숨기게 설정
        } else {
            Log.d(TAG, "일반 로그인일 경우");
            // 카카오 로그인이 아닐 경우
            kakaologoutButton.setVisibility(View.GONE); // 카카오 로그아웃 버튼 숨기게 설정
            logoutButton.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imagePathFromServer)
                    .error(R.drawable.profile)
                    .into(profileImage);
            String nickname = receivedID;
            nickName.setText(nickname);// 다른 로그아웃 버튼 보이게 설정
        }









        //shared를 이용하는 방법.

        kakaologoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 카카오 로그아웃 메서드 호출
                UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
                    @Override
                    public Unit invoke(Throwable throwable) {
                        if (throwable == null) {
                            Intent intent = new Intent(user_profile.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // 로그아웃 실패 처리
                            Log.e(TAG, "로그아웃 실패: " + throwable.getMessage());
                        }
                        return null;
                    }
                });
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(user_profile.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });





    }
}
