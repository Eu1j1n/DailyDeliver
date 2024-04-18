package com.example.dailydeliver.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dailydeliver.ApiService;
import com.example.dailydeliver.R;
import com.example.dailydeliver.RetrofitClient;
import com.example.dailydeliver.user_profile;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    TextView sign;

    EditText editID, editPassword;
    Button loginButton;

    ImageButton kakaoLoginButton, naverButton;

    Fragment UserProfileFragment;

    ImageView appLogo;

    String baseUri = "http://43.201.32.122/";

    private static final int PERMISSION_REQUEST_CODE = 1;

    String TAG = "로그인 액티비티";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        findViewByID();


        getHashKey();

        // 퍼미션 체크 및 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            // 퍼미션 요청
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            // 이미 허용된 경우 처리 로직 실행
        }


        Function2<OAuthToken,Throwable, Unit> callback =new Function2<OAuthToken, Throwable, Unit>() {
            @Override
            // 콜백 메서드 ,
            public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
                Log.e(TAG,"CallBack Method");
                //oAuthToken != null 이라면 로그인 성공
                if(oAuthToken!=null){
                    Log.d(TAG, "여기 들어옴? kakao");
                    updateKakaoLoginUi();
                    Intent intent = new Intent(LoginActivity.this, user_profile.class);
                    startActivity(intent);
                    finish();



                }else {
                    //로그인 실패
                    Log.e(TAG, "invoke: login fail" );
                    Toast.makeText(LoginActivity.this, "카카오 로그인에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }

                return null;
            }
        };







        KakaoSdk.init(this, "20458c53f41feddbd90e4c7bc58dddda");


        kakaoLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 해당 기기에 카카오톡이 설치되어 있는 확인
                if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(LoginActivity.this)){
                    UserApiClient.getInstance().loginWithKakaoTalk(LoginActivity.this, callback);
                }else{
                    // 카카오톡이 설치되어 있지 않다면
                    UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this, callback);
                }
            }
        });



        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
                startActivity(intent);

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String receiveID = editID.getText().toString();
                String receivePassword = editPassword.getText().toString();

                ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);

                // Retrofit을 사용하여 로그인 요청 전송
                Call<ResponseBody> call = apiService.loginUser(receiveID, receivePassword);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {
                                String responseBody = response.body().string();
                                if (responseBody.equals("success")) {
                                    // 성공 처리 (ID 사용 가능)
                                    Toast.makeText(LoginActivity.this, "로그인에 성공했습니다.", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);


                                    intent.putExtra("아이디 값", receiveID); // 원하는 데이터를 추가
                                    intent.putExtra("logintype", "general");

                                    startActivity(intent);

                                    finish();
                                } else if (responseBody.equals("fail")) {
                                    // 실패 처리 (ID 중복)
                                    Toast.makeText(LoginActivity.this, "아이디 혹은 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // 기타 응답 처리
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                // 예외 처리
                            }
                        } else {
                            // 실패 처리 (서버 응답이 실패인 경우)
                            Toast.makeText(LoginActivity.this, "서버 응답 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        // 네트워크 오류 등 실패 처리
                        Log.e(TAG, "중복확인 에러 = " + t.getMessage());
                        Toast.makeText(LoginActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    protected void findViewByID() {

        appLogo = findViewById(R.id.appLogo);
        kakaoLoginButton = findViewById(R.id.kakaobtn);
        sign = findViewById(R.id.sign);
        editID = findViewById(R.id.editID);
        editPassword = findViewById(R.id.editPassword);
        loginButton = findViewById(R.id.loginButton);


    }

    protected  void getHashKey() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }
    private void updateKakaoLoginUi() {

        // 로그인 여부에 따른 UI 설정
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {

                if (user != null) {
                    Long id = user.getId();
                    String email = user.getKakaoAccount().getEmail();
                    String nickname = user.getKakaoAccount().getProfile().getNickname();
                    String profileImageUrl = user.getKakaoAccount().getProfile().getProfileImageUrl(); // 프로필 이미지 URL

                    // 사용자 정보를 전달할 Intent 생성
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("id", id);
                    intent.putExtra("email", email);
                    intent.putExtra("아이디 값", nickname);
                    Log.d(TAG, "닉네임" + nickname);
                    intent.putExtra("profileImageUrl", profileImageUrl);
                    intent.putExtra("logintype", "kakao");

                    startActivity(intent);
                    finish();
                    Log.d(TAG, "profileImageUrl 값" + profileImageUrl);
                   // 프로필 이미지 URL도 전달

                    // 액티비티 전환


                    // 유저의 아이디



                } else {


                }
                return null;
            }
        });
    }


}







