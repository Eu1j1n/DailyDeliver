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
        // 주소 선택 시 호출되는 메소드로, 선택한 주소를 처리하는 로직을 작성합니다.
        // 예를 들어, 주소를 안드로이드 액티비티에 전달하는 등의 작업을 수행할 수 있습니다.
    }
}



