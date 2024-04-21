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

            // getChatroom 메소드 호출

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
        // 화면이 비활성화될 때, 안 읽은 메시지 개수를 초기화
        resetUnreadMessageCounts();
    }







    private void resetUnreadMessageCounts() {
        // 사용자가 나가거나 화면이 비활성화될 때, 모든 채팅방의 안 읽은 메시지 개수를 초기화
        for (ChatRoomItem room : chatRoomItems) {
            room.setMessageCount(0);
        }
        // UI 업데이트를 위해 어댑터에 변경 사항 알림
        chatAdapter.notifyDataSetChanged();
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










}
