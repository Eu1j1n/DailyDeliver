package com.example.dailydeliver.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.dailydeliver.ApiService;
import com.example.dailydeliver.Fragment.KakaoPayLoad;
import com.example.dailydeliver.R;
import com.example.dailydeliver.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.IOException;

import java.net.URISyntaxException;
import java.util.HashMap;

public class KakaoPayWebViewActivity extends AppCompatActivity {

    private String productName;
    private Integer updatedCredit;
    private ApiService apiService;
    private String baseUri = "https://open-api.kakaopay.com/";

    private WebView webView;

    String TAG = "카카오페이";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kakao_pay_web_view);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUri)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        productName = getIntent().getStringExtra("productName");
        updatedCredit = getIntent().getIntExtra("updatedCredit", 0);
        Log.d(TAG, "updatedCredit type: " + updatedCredit.getClass().getSimpleName());

        webView = findViewById(R.id.kakaoWebView);
        Log.d(TAG, "productName: " + productName);
        Log.d(TAG, "updatedCredit" + updatedCredit);

        startKakaoPayProcess();
    }

    private void startKakaoPayProcess() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("cid", "TC0ONETIME");
        data.put("partner_order_id", "partner_order_id");
        data.put("partner_user_id", "partner_user_id");
        data.put("item_name", productName);
        data.put("quantity", 1);
        data.put("total_amount", updatedCredit);
        data.put("tax_free_amount", 0);
        data.put("approval_url", "http://43.201.32.122/success.php");
        data.put("cancel_url", "http://43.201.32.122/fail.php");
        data.put("fail_url", "http://43.201.32.122/cancel.php");

        String authorization = "SECRET_KEY DEV612D596032A934582CD325DEAD57F58003F63";

        Call<KakaoPayReadyResponse> call = apiService.readyKakaoPay(authorization, data);
        call.enqueue(new Callback<KakaoPayReadyResponse>() {
            @Override
            public void onResponse(Call<KakaoPayReadyResponse> call, Response<KakaoPayReadyResponse> response) {
                if (response.isSuccessful()) {
                    KakaoPayReadyResponse readyResponse = response.body();
                    if (readyResponse != null) {
                        final String nextRedirectAppUrl = readyResponse.getNext_redirect_app_url();
                        runOnUiThread(() -> {
                            webView.setWebViewClient(new CustomWebViewClient());
                            webView.getSettings().setJavaScriptEnabled(true);
                            webView.loadUrl(nextRedirectAppUrl);
                        });
                    }
                } else {
                    Log.e(TAG, "KakaoPay 준비 요청 실패: " + response.toString());
                }
            }

            @Override
            public void onFailure(Call<KakaoPayReadyResponse> call, Throwable t) {
                Log.e(TAG, "KakaoPay 준비 요청 실패: " + t.getMessage());
            }
        });
    }

    private void approveKakaoPay(String pgToken) {
        HashMap<String, String> data = new HashMap<>();
        data.put("cid", "TC0ONETIME");
        data.put("tid", "T1234567890123456789");
        data.put("partner_order_id", "partner_order_id");
        data.put("partner_user_id", "partner_user_id");
        data.put("pg_token", pgToken);

        String authorization = "SECRET_KEY DEV612D596032A934582CD325DEAD57F58003F63";

        Call<KakaoPayLoad> call = apiService.approveKakaoPay(authorization, data);
        call.enqueue(new Callback<KakaoPayLoad>() {
            @Override
            public void onResponse(Call<KakaoPayLoad> call, Response<KakaoPayLoad> response) {
                if (response.isSuccessful()) {
                    KakaoPayLoad payLoad = response.body();
                    if (payLoad != null) {
                        // 결제가 성공적으로 승인됨
                        Log.d(TAG, "결제 승인 성공");
                    }
                } else {
                    Log.e(TAG, "결제 승인 실패: " + response.toString());
                }
            }

            @Override
            public void onFailure(Call<KakaoPayLoad> call, Throwable t) {
                Log.e(TAG, "결제 승인 요청 실패: " + t.getMessage());
            }
        });
    }


    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("intent://")) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    if (intent != null) {
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        } else {

                            Toast.makeText(getApplicationContext(), "카카오톡 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        }
    }

}
