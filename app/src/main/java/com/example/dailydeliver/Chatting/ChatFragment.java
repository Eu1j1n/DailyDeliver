package com.example.dailydeliver.Chatting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

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

    String baseUri = "http://43.201.32.122";

    private String receivedID;
    private String loginType;
    private String kakaoProfileImageUrl;
    private String TAG = "채팅 목록 액티비티 프래그먼트";

    private String ip = "52.79.88.52";
    private int port = 8888;

    // BroadcastReceiver 정의
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "여기들어오냐");
            if ("MESSAGE_RECEIVED".equals(intent.getAction())) {
                // 데이터 받기
                String senderID = intent.getStringExtra("senderID");
                String messageContent = intent.getStringExtra("messageContent");
                String time = intent.getStringExtra("time");
                String roomName = intent.getStringExtra("roomName");

                Log.d(TAG, "senderID" + senderID);
                Log.d(TAG, "roomName" + roomName);

                // 채팅방 아이템 생성 및 추가
                ChatRoomItem newItem = new ChatRoomItem(senderID, messageContent, null, 0, time, roomName);
                chatRoomItems.add(newItem);
                chatAdapter.notifyItemInserted(chatRoomItems.size() - 1);
            }
        }
    };

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

        chatAdapter = new ChatRoomAdapter(getContext(), chatRoomItems);
        recyclerView.setAdapter(chatAdapter);

        // BroadcastReceiver 등록
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("MESSAGE_RECEIVED");
        requireActivity().registerReceiver(receiver, intentFilter);

        chatAdapter.setOnItemClickListener(position -> {
            ChatRoomItem clickedChatRoom = chatRoomItems.get(position);
            Intent intent = new Intent(getActivity(), Chatting.class);
            intent.putExtra("id", receivedID);
            intent.putExtra("loginType", loginType);
            intent.putExtra("kakaoUrl", kakaoProfileImageUrl);
            intent.putExtra("roomName", clickedChatRoom.getHideRoomName());
            startActivity(intent);
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            receivedID = bundle.getString("receivedID");
            loginType = bundle.getString("loginType");
            kakaoProfileImageUrl = bundle.getString("kakaoUrl");
            Log.d(TAG, "receivedID 값" + receivedID);
            Log.d(TAG, "loginType 값" + loginType);
            Log.d(TAG, "kakao 값" + kakaoProfileImageUrl);
        } else {
            Log.e(TAG, "Arguments bundle is null");
        }



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: 여기들어옴?");
    }

    @Override
    public void onPause() {
        super.onPause();
        resetUnreadMessageCounts();
    }

    private void resetUnreadMessageCounts() {
        for (ChatRoomItem room : chatRoomItems) {
            room.setMessageCount(0);
        }
        chatAdapter.notifyDataSetChanged();
    }

    private void getUnreadMessageCount(String receivedID, String roomName) {
        ApiService chatCountApiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
        Call<Integer> call = chatCountApiService.getUnreadMessageCount(receivedID, roomName);
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
}
