package com.example.dailydeliver.Chatting;

import static java.lang.System.out;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.privacysandbox.tools.core.model.Method;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.dailydeliver.ApiService;
import com.example.dailydeliver.ImageResponse;
import com.example.dailydeliver.R;
import com.example.dailydeliver.RetrofitClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonObject;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Chatting extends AppCompatActivity {

    private EditText editMessage;

    private Handler mHandler;

    MessageAdapter messageAdapter;



    InetAddress serverAddr;


    Socket socket;

    PrintWriter sendWriter;


    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int REQUEST_CODE = 1;
    private BottomSheetDialog dialog;


    private String ip = "43.201.32.122";
    private int port = 8888;
    private ImageButton quit, plusButton, submitButton;

    String TAG = "채팅방 액티비티";


    TextView textViewChatname, date;

    String profileImageUrl, kakaoImageUrl,loginType, read, roomName, receivedID, receiver
           ;

    private String otherPeopleToken;

    private RecyclerView recyclerView;

    private WebSocketClient webSocketClient;

    String baseUri = "http://43.201.32.122";

    private File cameraImageFile;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    int PICK_IMAGE_REQUEST = 1;

    int CAMERA_REQUEST = 0;


    private List<com.example.dailydeliver.Chatting.Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        date =findViewById(R.id.date);
        editMessage = findViewById(R.id.editMessage);
        submitButton = findViewById(R.id.submitButton);
        recyclerView = findViewById(R.id.recyclerMessage);
        textViewChatname = findViewById(R.id.chatroomName);
        plusButton = findViewById(R.id.plusButton);
        quit = findViewById(R.id.quit);
        dialog = new BottomSheetDialog(Chatting.this);
        Intent intent = getIntent();
        receivedID = intent.getStringExtra("id");
        roomName = intent.getStringExtra("roomName");
        kakaoImageUrl = intent.getStringExtra("kakaoUrl");
        loginType = intent.getStringExtra("loginType");
        Log.d(TAG, "loginType" + loginType);
        Log.d(TAG, "kakaImageUrl" + kakaoImageUrl);


        String[] splitRoomName = roomName.split("01072047094");
        if (!receivedID.equals(splitRoomName[0])) {
            receiver = splitRoomName[0];
        } else {
            receiver = splitRoomName[1];
        }

        Log.d(TAG, "receiver" + receiver);

        textViewChatname.setText(receiver);


        getToken(receiver);


        getChatHistory(roomName);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }





        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() { //스크롤 위치에 따라 날짜 바뀌게.
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateDateBasedOnLastItem();
            }
        });

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                } else {
                    showBottomSheetDialog();
                }
            }
        });


        submitButton.setOnClickListener(new View.OnClickListener() { // 메세지를전송하는곳
            @Override
            public void onClick(View view) {
                String text = editMessage.getText().toString().trim();
                if (!text.isEmpty()) {
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                long messageID = generateMessageId(); //  return System.currentTimeMillis();
                                String messageIdAsString = String.valueOf(messageID);
                                String receiveRoomName = roomName;
                                String senderID = receivedID;
                                String receivedID = receiver;
                                String messageContent = editMessage.getText().toString();
                                Log.d(TAG, "messageContent" + messageContent);

                                // 24시간제로 시간 포맷 설정
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                String currentTime = sdf.format(new Date());


                                String messageToSend = receiveRoomName + ">" + senderID + ">" + messageContent +">" + messageIdAsString;

                                Log.d(TAG, "messageTosend의 값" + messageToSend);

                                String currentDate = (String) DateFormat.format("MM-dd-yyyy", new Date());

                                String saveMessage = receiveRoomName + ">" + senderID + ">" + messageContent + ">" + currentTime + ">"
                                        + currentDate + ">" + receiver ;

                                Log.d(TAG, "receiver" + receiver);

                                Log.d(TAG, "saveMessage의 값 " + saveMessage);
                                sendWriter.println(messageToSend);
                                sendWriter.flush();

                                // Retrofit 클라이언트 생성
                                ApiService chatApiService = RetrofitClient.getClient(baseUri).create(ApiService.class);

                                Call<Void> call = chatApiService.sendChatMessage(saveMessage);


                                call.enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        if (response.isSuccessful()) {
                                            // 성공적으로 전송된 경우
                                            Log.d(TAG, "채팅 메시지 전송 성공");
                                            getToken(receiver);
                                            Log.d(TAG, "receiver" + receiver);
                                            sendNotification(senderID, messageContent, currentTime, roomName);
                                        } else {
                                            // 전송 실패한 경우
                                            Log.e(TAG, "채팅 메시지 전송 실패");
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {
                                        // HTTP 요청 실패 또는 네트워크 오류 등을 처리
                                        Log.e(TAG, "에러1 = " + t.getMessage());
                                        // 실패 처리 로직
                                    }
                                });


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                    editMessage.setText("");
                }
            }
        });

        mHandler = new Handler();

        new Thread() {
            @Override
            public void run() {
                try {

                    InetAddress serverAddr = InetAddress.getByName(ip);
                    socket = new Socket(serverAddr, port);
                    sendWriter = new PrintWriter(socket.getOutputStream());


                    sendWriter.println(roomName); // 여기서 방정보 보내는사람 그리고 입장을 알리는 메세지를 한번에 보내면

                    sendWriter.flush();

                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                    while (true) {
                        read = input.readLine();

                        if (read != null) {
                            mHandler.post(new msgUpdate(read));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {

                    @Override
                    public void run() {
                        // 서버에 종료를 알리는 메시지를 보냄
                        sendMessageToServer("alsldlfkdscjndnjdidjaknckajsnkqwduin-=e3wqsa");

                        // 소켓을 닫음
                        closeSocket();
                        Log.d(TAG, "소켓 닫힘?");
                        finish();
                    }

                    private void closeSocket() {
                        try {
                            if (socket != null && !socket.isClosed()) {
                                socket.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    // 서버에 메시지를 보내는 메서드
                    private void sendMessageToServer(String message) {
                        if (out != null) {
                            sendWriter.println(roomName + ">" + receivedID + ">" +
                                    message + ">" + 12);
                            sendWriter.flush();
                        }
                    }
                }.start();
            }
        });


    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        Log.d(TAG, "onStop:  드러옴?");
//        new Thread() {
//            @Override
//            public void run() {
//                Log.d(TAG, "들어와?");
//                sendMessageToServer("alsldlfkdscjndnjdidjaknckajsnkqwduin-=e3wqsa");
//                closeSocket();
//            }
//        }.start();
//    }

    // 서버에 메시지를 보내는 메서드
    private void sendMessageToServer(String message) {
        new Thread() {
            @Override
            public void run() {

                if (out != null) {
                    Log.d(TAG, "들어와?2");
                    sendWriter.println(roomName + ">" + receivedID + ">" +
                            message + ">" + 12);
                    Log.d(TAG, "이떄의 메세지" + message);

                    sendWriter.flush();
                }
            }
        }.start();
    }



    private void closeSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    class msgUpdate implements Runnable {
        private String msg;

        public msgUpdate(String str) {
            this.msg = str;
        }

        @Override
        public void run() {
            Log.d(TAG, "run: 들어왔음" + msg);

            if(msg.equals("방에 두 명 이상이 있습니다.")) {
                updateReadStatus(roomName);
                Log.d(TAG, "messageList size" + messageList.size());
                for (Message message : messageList) {
                    Log.d(TAG, "야야 여기들어옴>??");
                    message.setReadStatus(2);
                    messageAdapter.notifyDataSetChanged();

                }
            }

            String[] messageParts = msg.split(">");

            if (messageParts.length != 7) {
                return; // 메시지 파싱이 올바르게 이루어지지 않은 경우 종료
            }

            String room = messageParts[0];
            String senderID = messageParts[1];
            String messageContent = messageParts[2];
            String messageID = messageParts[3];
            String isEnter = messageParts[4];
            String currentTime = messageParts[5];
            String currentDate = messageParts[6];
            Log.d(TAG, "isEnter"+ isEnter);
            Log.d(TAG, "messageID123" + messageID);

            if(isEnter.equals("ENTER MESSAGE")) {
                updateReadStatus(roomName);
                setAllMessagesReadStatus(2);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (senderID.equals(receivedID)) {
                        // 내 메세지인 경우
                        Message message;
                        int messageType = isEnter.equals("ENTER MESSAGE") ? 2 : 0; // ENTER MESSAGE 여부에 따라 messageType 지정


                        if (messageContent.contains("image_")) {
                            String[] fileNameParts = messageContent.split("/");
                            String fileName = fileNameParts[fileNameParts.length - 1];
                            String imageUrl = baseUri + "/chattingImage/" + fileName;

                            message = new Message(imageUrl, currentTime, Message.TYPE_MY_PICTURE_MESSAGE, senderID, profileImageUrl, currentDate, imageUrl, messageID, messageType);
                        } else {
                            message = new Message(messageContent, currentTime, Message.TYPE_MY_MESSAGE, senderID, profileImageUrl, currentDate, null, messageID, messageType);
                        }

                        messageList.add(message);
                        sortMessagesByTimestamp();

                        messageAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(messageList.size() - 1);
                    } else {
                        // 상대 메세지인 경우
                        if (messageContent.contains("image_")) {
                            String[] fileNameParts = messageContent.split("/");
                            String fileName = fileNameParts[fileNameParts.length - 1];
                            String imageUrl = baseUri + "/chattingImage/" + fileName;

                            // 상대방 프로필 이미지 가져오기
                            ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
                            Call<ImageResponse> profileImageCall = apiService.getImageFileName(senderID);

                            profileImageCall.enqueue(new Callback<ImageResponse>() {
                                @Override
                                public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                                    if (response.isSuccessful()) {
                                        ImageResponse imageResponse = response.body();
                                        String profileImageFileName = imageResponse.getImagePath();

                                        Log.d(TAG, "profileImageUrl !!!!" + profileImageUrl);

                                        Message imageMessage = new Message(imageUrl, currentTime, Message.TYPE_OTHER_PICTURE_MESSAGE, senderID, profileImageFileName, currentDate, imageUrl, messageID, 2);
                                        messageList.add(imageMessage);
                                        sortMessagesByTimestamp();
                                        messageAdapter.notifyDataSetChanged();
                                        recyclerView.smoothScrollToPosition(messageList.size() - 1);
                                    }
                                }

                                @Override
                                public void onFailure(Call<ImageResponse> call, Throwable t) {
                                    Log.e(TAG, "에러 2= " + t.getMessage());
                                }
                            });
                        } else {
                            ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
                            Call<ImageResponse> profileImageCall = apiService.getImageFileName(senderID);

                            profileImageCall.enqueue(new Callback<ImageResponse>() {
                                @Override
                                public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                                    if (response.isSuccessful()) {
                                        ImageResponse imageResponse = response.body();
                                        String profileImageFileName = imageResponse.getImagePath();

                                        Log.d(TAG, "profileImageUrl !!!!" + profileImageUrl);

                                        Message otherMessage = new Message(messageContent, currentTime, Message.TYPE_OTHER_MESSAGE, senderID, profileImageFileName, currentDate, null, messageID, 2);
                                        messageList.add(otherMessage);

                                        sortMessagesByTimestamp();
                                        messageAdapter.notifyDataSetChanged();
                                        recyclerView.smoothScrollToPosition(messageList.size() - 1);
                                    }
                                }

                                @Override
                                public void onFailure(Call<ImageResponse> call, Throwable t) {
                                    Log.e(TAG, "에러 3= " + t.getMessage());
                                }
                            });
                        }
                    }
                }
            });
        }
    }


    private void getToken(String orderPeople) {


        // Retrofit 인터페이스 생성
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);

        // 특정 사용자의 토큰을 가져오기 위한 API 호출
        Call<JsonObject> call = apiService.getToken(orderPeople);

        // 비동기적으로 API 호출
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    // 응답을 JsonObject로 받음
                    JsonObject jsonObject = response.body();

                    otherPeopleToken = jsonObject.get("token").getAsString();
                    Log.d(TAG, "상대방 토큰" + otherPeopleToken);



                } else {
                    // API 호출에 실패한 경우
                    Log.e(TAG, "토큰을 가져오는 데 실패했습니다.");

                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // 네트워크 오류 등으로 API 호출에 실패한 경우
                Log.e(TAG, "토큰을 가져오는 데 실패했습니다.", t);

            }
        });
    }



    private void sendNotification(String senderID, String messageContent, String currentTime, String roomName) {
        // 상대방 토큰 값을 받고 노티피케이션으로 아이디랑 내용 보냄
        FCMNotificationSender notificationSender = new FCMNotificationSender(otherPeopleToken, senderID, messageContent, senderID, messageContent, currentTime, roomName);
        Log.d(TAG, "OtherPeopleToken값" + otherPeopleToken);
        Log.d(TAG, "senderID" + senderID);
        Log.d(TAG, "messageContent" + messageContent);

        notificationSender.execute();
    }















    private long generateMessageId() {
        // 현재 시간을 기반으로한 고유한 ID 생성
        return System.currentTimeMillis();
    }

    private void setAllMessagesReadStatus(int status) {
        for (Message message : messageList) {
            message.setReadStatus(2);
        }
    }

    private void updateReadStatus(String roomName) {
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
        Call<Void> updateReadStatusCall = apiService.updateReadStatus(roomName);
        updateReadStatusCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {

                    Log.d(TAG, "읽음 상태 업데이트 다함");
                } else {

                    Log.d(TAG, "읽음 상태 업데이트 실패");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

                Log.d(TAG, "읽음 상태 네트워크 실패");
            }
        });
    }




    private void getChatHistory(String roomName) {
        ApiService chatApiService = RetrofitClient.getClient(baseUri).create(ApiService.class);

        Call<List<ChatMessage>> call = chatApiService.getChatHistory(roomName);

        call.enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful()) {
                    List<ChatMessage> chatHistory = response.body();

                    // chatHistory를 받아온 순서대로 메시지 리스트에 추가
                    for (ChatMessage message : chatHistory) {
                        String messageContent = message.getMessage();
                        String currentTime = message.getTime();
                        String senderID = message.getSender();
                        String currentDate = message.getDate();
                        int isRead = message.getIsRead();
                        String messageID = message.getMessageID();
                        Log.d(TAG, "messageContent" + messageContent);
                        Log.d(TAG, "currentTime" + currentTime);
                        Log.d(TAG, "senderID" + senderID);
                        Log.d(TAG, "currentDate" + currentDate);
                        Log.d(TAG, "isRead" + isRead);

                        // 받아온 메시지를 메시지 객체로 변환하여 리스트에 추가
                        if (senderID.equals(receivedID)) {
                            if (messageContent.contains("image_")) {
                                String[] fileNameParts = messageContent.split("/");
                                String fileName = fileNameParts[fileNameParts.length - 1];

                                String imageUrl = baseUri + "/chattingImage/" + fileName;
                                Message imageMessage = new Message(imageUrl, currentTime, Message.TYPE_MY_PICTURE_MESSAGE, senderID, null, currentDate, imageUrl, messageID, isRead);
                                messageList.add(imageMessage);
                            } else {
                                Message chatMessage = new Message(messageContent, currentTime, Message.TYPE_MY_MESSAGE, senderID, null, currentDate, null, messageID, isRead);
                                messageList.add(chatMessage);
                            }
                        } else {

                            if (messageContent.contains("image_")) {
                                String[] fileNameParts = messageContent.split("/");
                                String fileName = fileNameParts[fileNameParts.length - 1];
                                String imageUrl = baseUri + "/chattingImage/" + fileName;
                                Message otherImageMessage = new Message(imageUrl, currentTime, Message.TYPE_OTHER_PICTURE_MESSAGE, senderID, null, currentDate, imageUrl, messageID, 2);
                                messageList.add(otherImageMessage);
                                retrieveProfileImageAndUpdateMessages(senderID);
                            } else {
                                Message otherChatMessage = new Message(messageContent, currentTime, Message.TYPE_OTHER_MESSAGE, senderID, null, currentDate, null, messageID, 2);
                                messageList.add(otherChatMessage);
                                retrieveProfileImageAndUpdateMessages(senderID);

                            }
                        }
                    }

                    messageAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(messageList.size() - 1);
                    // UI에 변경 사항 반영
                } else {
                    Log.e(TAG, "이전 채팅 내용 가져오기 실패");
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                Log.e(TAG, "에러 5= " + t.getMessage());
            }
        });
    }

    private void retrieveProfileImageAndUpdateMessages(String senderID) {
        // 프로필 이미지를 가져오기 위한 Retrofit 호출
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
        Call<ImageResponse> profileImageCall = apiService.getImageFileName(senderID);

        profileImageCall.enqueue(new Callback<ImageResponse>() {
            @Override
            public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                if (response.isSuccessful()) {
                    ImageResponse imageResponse = response.body();
                    String profileImageFileName = imageResponse.getImagePath();


                    // 가져온 프로필 이미지를 메시지 객체에 설정
                    for (Message message : messageList) {
                        if (message.getSender().equals(senderID)) {
                            message.setProfileImageUrl(profileImageFileName);
                        }
                    }

                    // RecyclerView 갱신
                    messageAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "프로필 이미지 가져오기 실패");
                }
            }

            @Override
            public void onFailure(Call<ImageResponse> call, Throwable t) {
                Log.e(TAG, "프로필 이미지 가져오기 실패: " + t.getMessage());
            }
        });
    }





    // 메시지를 타임스탬프 순서로 정렬
    private void sortMessagesByTimestamp() {
        Collections.sort(messageList, new Comparator<Message>() {
            @Override
            public int compare(Message message1, Message message2) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    // 날짜와 시간을 합친 문자열을 생성
                    String dateString1 = message1.getDate() + " " + message1.getTime();
                    String dateString2 = message2.getDate() + " " + message2.getTime();

                    // 날짜와 시간을 파싱
                    Date date1 = format.parse(dateString1);
                    Date date2 = format.parse(dateString2);

                    // 두 날짜를 비교하여 순서를 반환
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });
        messageAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void updateDateBasedOnLastItem() {
        if (!messageList.isEmpty()) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

            if (lastVisibleItemPosition != RecyclerView.NO_POSITION) {
                Message lastVisibleMessage = messageList.get(lastVisibleItemPosition);
                String currentDateFromServer = lastVisibleMessage.getDate();

                // 현재 날짜 구하기
                Date currentDate = Calendar.getInstance().getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String today = dateFormat.format(currentDate);

                if (currentDateFromServer.equals(today)) {
                    date.setText("오늘");
                } else {
                    date.setText(currentDateFromServer);
                }
            }
        }
    }









    private void uploadImage(Uri imageUri) {
        // Retrofit 클라이언트 생성
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);

        String fileName = "image_" + System.currentTimeMillis() + ".jpg";

        // 폴더 이름 설정
        RequestBody folderName = RequestBody.create(MediaType.parse("text/plain"), "your_folder_name");

        try {
            // 이미지 파일 생성
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e(TAG, "Failed to open InputStream from URI");
                return;
            }

            File tempFile = new File(getCacheDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), tempFile);

            // MultipartBody.Part 생성
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", tempFile.getName(), requestFile);

            // 이미지 업로드 API 호출
            Call<ResponseBody> call = apiService.uploadChatImage(folderName, body, RequestBody.create(MediaType.parse("text/plain"), fileName));

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            sendImageMessage(fileName);
                            Log.d(TAG, "Server response: " + response.body().string());
                            Log.d(TAG, "Image uploaded successfully");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // 이미지 업로드 실패
                        Log.e(TAG, "Image upload failed");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // 네트워크 오류 등 실패 시 처리
                    Log.e(TAG, "Image upload failure: " + t.getMessage());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "File creation failed: " + e.getMessage());
        }
    }



    private void sendImageMessage(String fileName) {
        long messageID = generateMessageId(); //  return System.currentTimeMillis();
        String messageIdAsString = String.valueOf(messageID);
        // 이미지가 업로드되었음을 나타내는 메시지를 생성
        String messageToSend = roomName + ">" + receivedID + ">" + fileName + ">" + messageIdAsString;

        new Thread(new Runnable() {
            @Override
            public void run() {
                sendWriter.println(messageToSend);
                sendWriter.flush();

                // Retrofit 클라이언트 생성
                ApiService chatApiService = RetrofitClient.getClient(baseUri).create(ApiService.class);

                // 이미지가 업로드된 시간을 현재 시간으로 설정
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentTime = sdf.format(new Date());
                String currentDate = (String) DateFormat.format("MM-dd-yyyy", new Date());


                // 이미지 메시지를 서버에 저장하기 위한 API 호출
                String saveMessage = roomName + ">" + receivedID + ">" + fileName + ">" + currentTime + ">" + currentDate + ">" + messageIdAsString;
                Call<Void> call = chatApiService.sendChatMessage(saveMessage);

                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // 성공적으로 전송된 경우
                            getToken(receiver);
                            Log.d(TAG, "이미지 리시버" + receiver);
                            sendNotification(receivedID, fileName, currentTime, roomName);
                            Log.d(TAG, "이미지 메시지 전송 및 저장 성공");
                        } else {
                            // 전송 실패한 경우
                            Log.e(TAG, "이미지 메시지 저장 실패");
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        // HTTP 요청 실패 또는 네트워크 오류 등을 처리
                        Log.e(TAG, "에러 6= " + t.getMessage());
                        // 실패 처리 로직 추가
                    }
                });
            }
        }).start();
    }



    // 이미지 실제 경로 가져오기
    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String filePath = cursor.getString(column_index);
        cursor.close();
        return filePath;
    }

    private File saveImageFromCamera(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            cameraImageFile = saveBitmapToFile(imageBitmap);
            return cameraImageFile;
        }
        return null;
    }

    private File saveBitmapToFile(Bitmap bitmap) {

        String filename = "image_" + System.currentTimeMillis() + ".jpg";


        File externalFilesDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(externalFilesDir, filename);

        try {
            // 파일 출력 스트림 생성
            FileOutputStream outputStream = new FileOutputStream(imageFile);

            // Bitmap을 JPEG 형식으로 압축하여 파일에 저장
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            // 파일 출력 스트림 닫기
            outputStream.close();

            // 저장된 파일의 Uri를 반환
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }








    private void showBottomSheetDialog() {
        dialog = new BottomSheetDialog(Chatting.this);
        View contentView = getLayoutInflater().inflate(R.layout.bottomdialog, null);
        dialog.setContentView(contentView);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);

                    // 최대 높이 설정
                    behavior.setPeekHeight(contentView.getHeight());
                }
            }
        });

        dialog.show();
    }

    public void onCameraClick(View view) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                // 이미지 파일을 저장할 임시 파일을 생성합니다.
                cameraImageFile = createImageFile();
                if (cameraImageFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this, "your.package.name.fileprovider", cameraImageFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private File createImageFile() throws IOException {
        String fileName = "image_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(fileName, ".jpg", storageDir);
    }


    public void onAlbumClick(View view) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        // 갤러리 앱을 열기 위한 Intent 생성
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");  // 이미지 파일만 선택하도록 지정

        // 갤러리 앱 시작
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // 갤러리로부터 이미지를 선택한 경우
            Uri selectedImageUri = data.getData();
            uploadImage(selectedImageUri);
        } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            // 카메라로부터 이미지를 촬영한 경우
            if (cameraImageFile != null && cameraImageFile.exists()) {
                Uri imageUri = Uri.fromFile(cameraImageFile);
                uploadImage(imageUri);
            }
        }
    }










}


