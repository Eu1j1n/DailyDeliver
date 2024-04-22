package com.example.dailydeliver.Chatting;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.dailydeliver.Activity.LoginActivity;
import com.example.dailydeliver.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHANNEL_ID = "my_notification_channel";

    private String otherName;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: 여기들어옴?");

        // 푸시 알림의 제목과 내용 가져오기
        String title = remoteMessage.getNotification().getTitle();
        String msg = remoteMessage.getNotification().getBody();

        Log.d(TAG, "title: " + title);
        Log.d(TAG, "msg: " + msg);

        // 추가 데이터 가져오기
        String senderID = remoteMessage.getData().get("senderID");
        String messageContent = remoteMessage.getData().get("messageContent");
        String time = remoteMessage.getData().get("time");
        String roomName = remoteMessage.getData().get("roomName");

        String[] splitRoomName = roomName.split("01072047094");
        otherName = splitRoomName[0];

        Intent chatRoomIntent = new Intent(this, ChatFragment.class);
        chatRoomIntent.setAction("MESSAGE_RECEIVED");

        chatRoomIntent.putExtra("senderID", senderID);
        chatRoomIntent.putExtra("messageContent", messageContent);
        chatRoomIntent.putExtra("time", time);
        chatRoomIntent.putExtra("roomName", roomName);
        sendBroadcast(chatRoomIntent);







        Log.d(TAG, "senderID: " + senderID);
        Log.d(TAG, "messageContent: " + messageContent);
        Log.d(TAG, "time: " + time);
        Log.d(TAG, "roomName" + roomName);

        // 알림 클릭 시 열릴 액티비티 설정
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // NotificationChannel 생성
        createNotificationChannel();

        // 알림 생성
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // 알림 표시
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, mBuilder.build());

        // 알림 클릭 시 실행할 인텐트 설정
        mBuilder.setContentIntent(contentIntent);
    }


    // NotificationChannel 생성 메서드
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        // SharedPreferences를 사용하여 토큰 저장
        saveTokenToPreferences(token);
    }

    private void saveTokenToPreferences(String token) {
        SharedPreferences preferences = getSharedPreferences("FCM_PREF", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("FCM_TOKEN", token);
        editor.apply();
    }


}