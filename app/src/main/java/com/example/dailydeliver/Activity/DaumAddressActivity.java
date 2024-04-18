package com.example.dailydeliver.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;
import android.widget.ProgressBar;
import android.net.http.SslError;
import android.content.Intent;

import com.example.dailydeliver.R;

public class DaumAddressActivity extends AppCompatActivity {

    private static String IP_ADDRESS = "IP ADDRESS";

    String TAG = "다음 주소 찾기 ";

    String baseUri = "http://52.79.88.52/address.html";

    class MyJavaScriptInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processDATA(String data) {
            Bundle extra = new Bundle();
            Intent intent = new Intent();

            Log.d(TAG, "들어오긴함?1");
            extra.putString("data", data);
            intent.putExtras(extra);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private WebView wv_search_address;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daum_address);

        Log.d(TAG, "들어오긴함?2");

        progress = findViewById(R.id.web_progress);
        wv_search_address = findViewById(R.id.wv_search_address);

        wv_search_address.getSettings().setJavaScriptEnabled(true);
        wv_search_address.getSettings().setDomStorageEnabled(true);

        Log.d(TAG, "들어오긴함?3");
        wv_search_address.addJavascriptInterface(new MyJavaScriptInterface(), "Android");

        Log.d(TAG, "들어오긴함?4");

        wv_search_address.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                Log.d(TAG, "들어오긴함?5");
                handler.proceed(); // SSL 에러가 발생해도 계속 진행
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "들어오긴함?6");
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progress.setVisibility(View.GONE);
                wv_search_address.loadUrl("javascript:sample2_execDaumPostcode();");
            }
        });

        wv_search_address.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        wv_search_address.loadUrl(baseUri);
    }
}
