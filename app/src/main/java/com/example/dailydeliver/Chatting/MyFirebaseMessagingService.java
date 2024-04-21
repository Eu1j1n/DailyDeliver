package com.example.dailydeliver.Chatting;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.dailydeliver.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHANNEL_ID = "my_notification_channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // 알림 메시지 페이로드 데이터
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message : " + remoteMessage.getData());
            Log.d(TAG, "onMessageReceived: 야야들어옴?");

            // 알림을 표시하는 메서드 호출
            showNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"));
        }

        // 알림 메시지 내용
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    private void showNotification(String title, String message) {
        Log.d(TAG, "showNotification: 야야들어옴 2?");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Notification Channel";
            String description = "Description of My Notification Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            Log.d(TAG, "showNotification: 들어오긴하냐");
            notificationManager.createNotificationChannel(channel);
        }

        // 알림 빌더 생성
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.applogo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // 알림 매니저를 통해 알림 표시
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
         
            return;
        }
        notificationManager.notify(1, builder.build());
    }
}
