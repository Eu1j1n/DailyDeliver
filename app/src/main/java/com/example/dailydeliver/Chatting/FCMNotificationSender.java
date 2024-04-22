package com.example.dailydeliver.Chatting;

import android.os.AsyncTask;
import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FCMNotificationSender extends AsyncTask<Void, Void, Void> {

    private static final String FCM_SERVER_KEY = "AAAAkXAX9cI:APA91bHdvfweKZPgbA91Ez5pefhpR0aBoEoGxI5fjvDc0BDyMw27rBIvyXGP7O8EcyBlsNcSV13tnl-Umy4NksFo71zqSPmAKyljISB_v2IjuKgSxQcoItqD-vjj_0At2s-e5dhiqquS";
    private static final String FCM_API_URL = "https://fcm.googleapis.com/fcm/send";

    private String token;
    private String title;
    private String message;

    private String senderID;

    private String messageContent;

    private String time;

    private String roomName;

    public FCMNotificationSender(String token, String title, String message, String senderID, String messageContent, String time, String roomName) {
        this.token = token;
        this.title = title;
        this.message = message;
        this.senderID = senderID;
        this.messageContent = messageContent;
        this.time = time;
        this.roomName = roomName;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL(FCM_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "key=" + FCM_SERVER_KEY);
            connection.setRequestProperty("Content-Type", "application/json");

            // 푸시 알림 및 추가 데이터 설정
            String notificationMessage = "{"
                    + "\"to\":\"" + token + "\","
                    + "\"notification\":{"
                    + "\"title\":\"" + title + "\","
                    + "\"body\":\"" + message + "\""
                    + "},"
                    + "\"data\":{"
                    + "\"senderID\":\"" + senderID + "\","
                    + "\"messageContent\":\"" + messageContent + "\","
                    + "\"time\":\"" + time + "\","
                    + "\"roomName\":\"" + roomName + "\""
                    + "}"
                    + "}";

            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(notificationMessage.getBytes());
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            Log.d("FCMNotificationSender", "FCM 서버 응답 코드: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("FCMNotificationSender", "푸시 알림 전송 성공");
            } else {
                Log.e("FCMNotificationSender", "푸시 알림 전송 실패");
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
