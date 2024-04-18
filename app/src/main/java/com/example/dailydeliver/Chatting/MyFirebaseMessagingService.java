//package com.example.dailydeliver.Chatting;
//
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//
//import com.google.firebase.messaging.FirebaseMessagingService;
//import com.google.firebase.messaging.RemoteMessage;
//
//public class MyFirebaseMessagingService extends FirebaseMessagingService {
//
//    private static final String TAG = "파이어 베이스 서비스";
//
//    @Override
//    public void onNewToken(String token) {
//        super.onNewToken(token);
//        Log.d(TAG, "Refreshed token: " + token);
//
//        // 여기에서 토큰을 사용하여 서버에 등록하거나 기타 작업을 수행
//    }
//
//    @Override
//    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
//        super.onMessageReceived(remoteMessage);
//        //수신한 메시지를 처리
//    }
//}
