package com.example.dailydeliver.Chatting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.dailydeliver.ApiService;

import com.example.dailydeliver.R;
import com.example.dailydeliver.RetrofitClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<ChatRoomItem> chatRoomItems;
    private ChatRoomAdapter chatAdapter;

    String baseUri = "http://52.79.88.52";

    private String receivedID;
    private String loginType;
    private String kakaoProfileImageUrl;
    private String TAG = "채팅 목록 액티비티 프래그먼트";

    private String ip = "52.79.88.52";
    private int port = 8888;

    private Handler mHandler;

    private Socket socket;
    private PrintWriter out;

    public ChatFragment() {
        chatRoomItems = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recycler_chat_rooms);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);


        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());


        recyclerView.addItemDecoration(dividerItemDecoration);
        //구분선
        chatAdapter = new ChatRoomAdapter(getContext(), chatRoomItems);
        recyclerView.setAdapter(chatAdapter);

        chatAdapter.setOnItemClickListener(position -> { //채팅방 들어가는 거
            ChatRoomItem clickedChatRoom = chatRoomItems.get(position);
            Intent intent = new Intent(getActivity(), Chatting.class);
            intent.putExtra("id", receivedID);
            intent.putExtra("loginType", loginType);
            intent.putExtra("kakaoUrl", kakaoProfileImageUrl);
            intent.putExtra("roomName", clickedChatRoom.getChatRoomName());

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            startActivity(intent);
        });

        Button startChat = view.findViewById(R.id.startChat);
        startChat.setOnClickListener(v -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            View dialogView = getLayoutInflater().inflate(R.layout.activity_dialog_chat, null);
            dialogBuilder.setView(dialogView);

            EditText editTextRoomName = dialogView.findViewById(R.id.editTextRoomName);
            dialogBuilder.setPositiveButton("확인", (dialog, which) -> {
                String roomName = editTextRoomName.getText().toString();
                getLastMessageTime(roomName);
                getLastMessage(roomName);
                ChatRoomItem chatRoomItem = new ChatRoomItem(roomName, "마지막 메시지 내용", "프로필 uri", 0, null);
                chatRoomItems.add(chatRoomItem);

                chatAdapter.notifyDataSetChanged();
                connectToServer(roomName);
                getUnreadMessageCount(receivedID, roomName);
            });

            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            receivedID = bundle.getString("receivedID");
            loginType = bundle.getString("loginType");
            kakaoProfileImageUrl = bundle.getString("kakaoUrl");

            Log.d(TAG, "receivedID 값" + receivedID);
            Log.d(TAG, "loginType 값" + loginType);
            Log.d(TAG, "kakao 값" + kakaoProfileImageUrl);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: 여기들어옴?");
        // 화면이 다시 활성화될 때 안 읽은 메시지 개수를 다시 가져오고, 소켓을 연결합니다.
        updateUnreadMessageCounts();
        updateLastMessages();

        // 각 채팅방에 대해 소켓을 연결합니다.
        for (ChatRoomItem room : chatRoomItems) {
            Log.d(TAG, "onResume: 여긴?");
            connectToServer(room.getChatRoomName());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // 화면이 비활성화될 때, 안 읽은 메시지 개수를 초기화
        resetUnreadMessageCounts();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 화면이 종료될 때 소켓을 닫습니다.
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUnreadMessageCounts() {
        // 여기에 현재 사용자의 모든 채팅방에 대한 안 읽은 메시지 개수를 가져오는 로직을 구현
        for (ChatRoomItem room : chatRoomItems) {
            // 안 읽은 메시지 개수 업데이트 로직
            getUnreadMessageCount(receivedID, room.getChatRoomName());
        }
    }

    private void updateLastMessages() {
        // 각 채팅방에 대해 마지막 메시지의 시간과 내용을 업데이트합니다.
        for (ChatRoomItem room : chatRoomItems) {
            getLastMessageTime(room.getChatRoomName());
            getLastMessage(room.getChatRoomName());
        }
    }

    private void resetUnreadMessageCounts() {
        // 사용자가 나가거나 화면이 비활성화될 때, 모든 채팅방의 안 읽은 메시지 개수를 초기화
        for (ChatRoomItem room : chatRoomItems) {
            room.setMessageCount(0);
        }
        // UI 업데이트를 위해 어댑터에 변경 사항 알림
        chatAdapter.notifyDataSetChanged();
    }

    private void getLastMessageTime(String roomName) {
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);

        Call<ResponseBody> call = apiService.getLastMessageTime(roomName);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String lastMessageTime = response.body().string();
                        Log.d(TAG, "마지막 메시지 시간: " + lastMessageTime);

                        for (int i = 0; i < chatRoomItems.size(); i++) {
                            if (chatRoomItems.get(i).getChatRoomName().equals(roomName)) {
                                chatRoomItems.get(i).setMessageTime(lastMessageTime);
                                break;
                            }
                        }

                        chatAdapter.notifyDataSetChanged();
                        // 여기에서 마지막 메시지 시간을 처리합니다.
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "서버 응답 실패");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "통신 실패: " + t.getMessage());
            }
        });
    }

    private void getLastMessage(String roomName) {
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);

        Call<ResponseBody> call = apiService.getLastMessage(roomName);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String lastMessageText = response.body().string();
                        Log.d(TAG, "마지막 메시지 내용 : " + lastMessageText);

                        for (int i = 0; i < chatRoomItems.size(); i++) {
                            if (chatRoomItems.get(i).getChatRoomName().equals(roomName)) {
                                chatRoomItems.get(i).setLastMessage(lastMessageText);
                                break;
                            }
                        }

                        chatAdapter.notifyDataSetChanged();
                        // 여기에서 마지막 메시지 시간을 처리합니다.
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "서버 응답 실패");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "통신 실패: " + t.getMessage());
            }
        });
    }

    private void getUnreadMessageCount(String receivedID, String roomName) {
        ApiService chatCountApiService = RetrofitClient.getClient(baseUri).create(ApiService.class);

        Call<Integer> call = chatCountApiService.getUnreadMessageCount(receivedID, roomName);

        Log.d(TAG, "receivedID" + receivedID);
        Log.d(TAG, "roomName" + roomName);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful()) {
                    Integer unreadCount = response.body();
                    Log.d(TAG, "안 읽은 메시지 개수: " + unreadCount);

                    for (int i = 0; i < chatRoomItems.size(); i++) {
                        if (chatRoomItems.get(i).getChatRoomName().equals(roomName)) {
                            chatRoomItems.get(i).setMessageCount(unreadCount);
                            break;
                        }
                    }

                    chatAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "서버 응답 실패");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(TAG, "통신 실패: " + t.getMessage());
            }
        });
    }



    private void connectToServer(String roomName) { // 채팅목록에서 소켓 연결하는곳, 채팅 액티비티도 동일함
        new Thread(() -> {
            try {
                socket = new Socket(ip, port);
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println(roomName);
                Log.d(TAG, "이때의 roomName" + roomName);
                Log.d(TAG, "connect 됨?");

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while ((message = in.readLine()) != null) {
                    // 메시지 파싱
                    String[] messageParts = message.split(">");
                    Log.d(TAG, "message" + message);
                    if (messageParts.length >= 5) {
                        String senderID = messageParts[1];
                        String lastMessage = messageParts[2];
                        String lastTime = messageParts[5];
                        Log.d(TAG, "senderID: " + senderID);
                        Log.d(TAG, "lastMessage" + lastMessage);

                        if (!senderID.equals(receivedID)) {
                            // 다른 클라이언트에게만 메시지 전송
                            increaseUnreadMessageCount(roomName);
                        }

                        updateLastMessage(roomName, lastMessage);
                        updateLastTime(roomName, lastTime);
                    } else {
                        Log.e(TAG, "Invalid message format: " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void updateLastMessage(String roomName, String lastMessage) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 해당 채팅방을 찾아서 마지막 메시지 업데이트
                for (ChatRoomItem room : chatRoomItems) {
                    if (room.getChatRoomName().equals(roomName)) {
                        room.setLastMessage(lastMessage);
                        // UI 업데이트를 위해 어댑터에 변경 사항 알림
                        chatAdapter.notifyDataSetChanged();
                        break; // 채팅방을 찾았으면 반복문 종료
                    }
                }
            }
        });
    }

    private void updateLastTime(String roomName, String lastTime) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 해당 채팅방을 찾아서 마지막 메시지 업데이트
                for (ChatRoomItem room : chatRoomItems) {
                    if (room.getChatRoomName().equals(roomName)) {
                        room.setMessageTime(lastTime);
                        // UI 업데이트를 위해 어댑터에 변경 사항 알림
                        chatAdapter.notifyDataSetChanged();
                        break; // 채팅방을 찾았으면 반복문 종료
                    }
                }
            }
        });
    }

    private void increaseUnreadMessageCount(String roomName) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 해당 채팅방을 찾아서 안 읽은 메시지 개수를 증가시킴
                for (ChatRoomItem room : chatRoomItems) {
                    if (room.getChatRoomName().equals(roomName)) {
                        // 현재 채팅방의 안 읽은 메시지 개수를 증가시킴
                        int unreadCount = room.getMessageCount();
                        room.setMessageCount(unreadCount + 1);
                        // UI 업데이트를 위해 어댑터에 변경 사항 알림
                        chatAdapter.notifyDataSetChanged();
                        break; // 채팅방을 찾았으면 반복문 종료
                    }
                }
            }
        });
    }
}
