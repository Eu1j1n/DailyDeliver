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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dailydeliver.ApiService;

import com.example.dailydeliver.ImageResponse;
import com.example.dailydeliver.R;
import com.example.dailydeliver.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        EventBus.getDefault().register(this);
        Log.d(TAG, "이벤 버스 등록?" );

        recyclerView = view.findViewById(R.id.recycler_chat_rooms);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        chatAdapter = new ChatRoomAdapter(getContext(), chatRoomItems);
        recyclerView.setAdapter(chatAdapter);

        chatAdapter.setOnItemClickListener(position -> {
            ChatRoomItem clickedChatRoom = chatRoomItems.get(position);
            Intent intent = new Intent(getActivity(), Chatting.class);
            intent.putExtra("id", receivedID);
            intent.putExtra("loginType", loginType);
            intent.putExtra("kakaoUrl", kakaoProfileImageUrl);
            intent.putExtra("roomName", clickedChatRoom.getHideRoomName());
            Log.d(TAG, "clickedDSda" + clickedChatRoom.getHideRoomName());
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

        getLastMessageInfoFromServer(receivedID);



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLastMessageInfoFromServer(receivedID);


        Log.d(TAG, "onResume: 여기들어옴?");

    }

    @Subscribe
    public void onMessageEvent(MessageEvent event) {
        String messageContent = event.getMessageContent();

        // 이미지 메시지인 경우 "(사진)"으로 설정
        if (messageContent.startsWith("image_")) {
            messageContent = "(사진)";
        }

        // 새로운 대화방 아이템 생성
        ChatRoomItem newRoomItem = new ChatRoomItem(event.getSenderID(), messageContent, null, 1, event.getTime(), event.getRoomName());

        // 이미 목록에 있는지 확인
        boolean roomExists = false;
        for (int i = 0; i < chatRoomItems.size(); i++) {
            ChatRoomItem room = chatRoomItems.get(i);
            if (room.getHideRoomName().equals(event.getRoomName())) {
                // 해당 방의 데이터를 업데이트
                room.setLastMessage(messageContent);
                room.setMessageTime(event.getTime());
                room.setMessageCount(room.getMessageCount() + 1);
                roomExists = true;

                // 목록에서 해당 대화방을 제거
                chatRoomItems.remove(i);
                // 제거한 후 목록의 맨 위로 다시 추가
                chatRoomItems.add(0, room);
                break;
            }
        }

        // 목록에 없는 경우 새로운 메시지를 추가
        if (!roomExists) {
            // 새로운 메시지가 도착한 대화방을 목록의 맨 위에 추가
            chatRoomItems.add(0, newRoomItem);
        }

        // UI 업데이트가 필요한 시점에서 목록을 갱신
        updateChatListUI();

        getOtherProfileImage(event.getSenderID());
    }


    private void getLastMessageInfoFromServer(String receivedID) {
        Log.d(TAG, "안들어옴?");
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
        Call<JsonArray> call = apiService.getChatRoomList(receivedID);
        Log.d(TAG, "receivedID" + receivedID);
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "성공");
                    JsonArray jsonArray = response.body();
                    Log.d(TAG, "jsonArray" + jsonArray);
                    if (jsonArray != null && jsonArray.size() > 0) {
                        chatRoomItems.clear();

                        // JSON 배열의 모든 객체를 순회
                        for (int i = 0; i < jsonArray.size(); i++) {
                            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
                            String roomName = jsonObject.get("room_name").getAsString();

                            String[] parts = roomName.split("01072047094");
                            String otherData;
                            if (parts[0].equals(receivedID)) {
                                otherData = parts[1];
                            } else {
                                otherData = parts[0];
                            }

                            Log.d(TAG, "들어옴?");
                            Log.d(TAG, "roomName " + roomName);
                            String lastMessage = jsonObject.get("message_content").getAsString();
                            String currentTime = jsonObject.get("currentTime").getAsString();

                            if (lastMessage.startsWith("image_")) {
                                lastMessage = "(사진)";
                            }

                            // 각 채팅방에 대한 새로운 항목 생성 및 추가
                            ChatRoomItem newRoomItem = new ChatRoomItem(otherData, lastMessage, null, 0, currentTime, roomName);
                            chatRoomItems.add(newRoomItem);


                            getOtherProfileImage(otherData);
                            getUnreadMessageCounts(receivedID);
                        }

                        updateChatListUI();

                    }
                } else {
                    Log.e(TAG, "서버 응답 실패");
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                Log.e(TAG, "통신 실패: " + t.getMessage());
            }
        });
    }



    private void getOtherProfileImage(String senderID) {
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
        Call<ImageResponse> call = apiService.getImageFileName(senderID);
        call.enqueue(new Callback<ImageResponse>() {
            @Override
            public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                if (response.isSuccessful()) {
                    ImageResponse imageResponse = response.body();
                    String profileImageFileName = imageResponse.getImagePath();
                    Log.d(TAG, "onResponse: " + profileImageFileName);

                    // ChatRoomItem 객체에 프로필 이미지 경로 설정
                    for (ChatRoomItem room : chatRoomItems) {
                        if (room.getChatRoomName().equals(senderID)) {
                            room.setProfileImage(profileImageFileName);
                            // RecyclerView 갱신
                            chatAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ImageResponse> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
                Toast.makeText(getActivity(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    // UI 업데이트를 담당하는 메서드
    private void updateChatListUI() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // RecyclerView 업데이트
                chatAdapter.notifyDataSetChanged();
                Log.d(TAG, "넌들어오긴함?2");
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        resetUnreadMessageCounts();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this); // EventBus 해제
        Log.d(TAG, "이벤 버스 해체?" );
    }



    private void resetUnreadMessageCounts() {
        for (ChatRoomItem room : chatRoomItems) {
            room.setMessageCount(0);
        }
        chatAdapter.notifyDataSetChanged();
    }

    private void getUnreadMessageCounts(String receivedID) {
        ApiService chatCountApiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
        Call<Map<String, Integer>> call = chatCountApiService.getUnreadMessageCount(receivedID);
        // 이때 userID는 로그인한 사용자의 아이디임!
        call.enqueue(new Callback<Map<String, Integer>>() {
            @Override
            public void onResponse(Call<Map<String, Integer>> call, Response<Map<String, Integer>> response) {
                if (response.isSuccessful()) {
                    Map<String, Integer> unreadCounts = response.body();
                    Log.d(TAG, "안 읽은 메시지 개수: " + unreadCounts);

                    // 응답에서 각 채팅방의 이름과 안 읽은 메시지 개수를 가져와서 UI 업데이트
                    for (Map.Entry<String, Integer> entry : unreadCounts.entrySet()) {
                        String roomName = entry.getKey();
                        Integer unreadCount = entry.getValue();
                        for (int i = 0; i < chatRoomItems.size(); i++) {
                            if (chatRoomItems.get(i).getHideRoomName().equals(roomName)) {
                                Log.d(TAG, "roomename" + roomName);
                                chatRoomItems.get(i).setMessageCount(unreadCount);
                                break;
                            }
                        }
                    }
                    chatAdapter.notifyDataSetChanged(); // UI 업데이트
                } else {
                    Log.e(TAG, "서버 응답 실패");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Integer>> call, Throwable t) {
                Log.e(TAG, "통신 실패: " + t.getMessage());
            }
        });
    }

}
