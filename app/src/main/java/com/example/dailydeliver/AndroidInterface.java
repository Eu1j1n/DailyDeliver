package com.example.dailydeliver;

import android.content.Context;
import android.webkit.JavascriptInterface;

public class AndroidInterface {
    Context mContext;

    AndroidInterface(Context context) {
        mContext = context;
    }

    @JavascriptInterface
    public void onAddressSelected(String roadAddress, String jibunAddress) {

    }
}



