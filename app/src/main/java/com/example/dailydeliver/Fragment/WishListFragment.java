package com.example.dailydeliver.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.dailydeliver.Adapter.WishAdapter;
import com.example.dailydeliver.Adapter.WishData;
import com.example.dailydeliver.ApiService;
import com.example.dailydeliver.R;
import com.example.dailydeliver.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishListFragment extends Fragment {

    private static final String ARG_RECEIVED_ID = "receivedID";

    private RecyclerView wishProductRecyclerView;
    private WishAdapter wishAdapter;

    ImageView blankImageview;

    String baseUri = "http://43.201.32.122/";

    SwipeRefreshLayout swipeRefreshLayout;

    List<WishData> wishDataList = new ArrayList<>();

    static String TAG = "위시 리스트";
    private String receivedID;

    public WishListFragment() {

    }

    public static WishListFragment newInstance(String receivedID) {
        WishListFragment fragment = new WishListFragment();
        Bundle args = new Bundle();
        args.putString("receivedID", receivedID);
        Log.d(TAG, "newInstance:" + receivedID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            receivedID = getArguments().getString(ARG_RECEIVED_ID);
            Log.d(TAG, "receivedID" + receivedID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wish_list, container, false);

        wishProductRecyclerView = view.findViewById(R.id.wishProductRecyclerView);
        blankImageview = view.findViewById(R.id.blankImageView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        wishProductRecyclerView.setLayoutManager(layoutManager);

        wishAdapter = new WishAdapter(getContext(), wishDataList);
        wishProductRecyclerView.setAdapter(wishAdapter);

        swipeRefreshLayout = view.findViewById(R.id.swiperefreshlayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchDataFromServer();
            }
        });

        // 어댑터에 아이템 클릭 리스너 설정
        // 어댑터에 아이템 클릭 리스너 설정
        wishAdapter.setOnItemClickListener(new WishAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(WishData item) {

                Intent intent = new Intent(getActivity(), ProductDetail.class);
                intent.putExtra("imageUri", item.getImage_uri());
                intent.putExtra("title", item.getTitle());
                intent.putExtra("location", item.getLocation());
                intent.putExtra("send_time", item.getSend_time());
                intent.putExtra("price", item.getPrice());
                intent.putExtra("user_name", item.getUserName());
                intent.putExtra("description", item.getDescription());
                intent.putExtra("receivedID", receivedID);
                Log.d(TAG, "홈프래그먼트 리시브 아이디" + receivedID);

                startActivity(intent);

            }
        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Add onStart lifecycle code here
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchDataFromServer();
    }

    public void fetchDataFromServer() {

        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
        Call<List<WishData>> call = apiService.getWishDataList(receivedID);
        Log.d(TAG, "fetchDataFromServer: receivedID" +receivedID);

        call.enqueue(new Callback<List<WishData>>() {
            @Override
            public void onResponse(Call<List<WishData>> call, Response<List<WishData>> response) {
                if (response.isSuccessful()) {
                    List<WishData> posts = response.body();
                    for (WishData data : posts) {
                        String imageUrl = data.getImage_uri();
                        String title = data.getTitle();
                        String time = data.getSend_time();
                        String userName = data.getUserName();
                        String description = data.getDescription();
                        String saleType = data.getSaleType();
                        String bidPrice = data.getBidPrice();
                        String remaining_time = data.getRemaining_time();
                        int state = data.getState();



                    }

                    displayPosts(posts);
                    swipeRefreshLayout.setRefreshing(false);

                } else {
                    Log.d(TAG, "onResponse: 실패냐?");
                    swipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void onFailure(Call<List<WishData>> call, Throwable t) {
                Log.e(TAG, "에러 6= " + t.getMessage());
                swipeRefreshLayout.setRefreshing(false);
                wishProductRecyclerView.setVisibility(View.GONE);
                blankImageview.setVisibility(View.VISIBLE);
                wishDataList.clear();

            }
        });
    }

    private void displayPosts(List<WishData> posts) {
        for (WishData data : posts) {
            String sendTime = data.getSend_time();
            Log.d(TAG, "sendTiem" + sendTime);
            String timeAgo = TimeUtil.getTimeAgo(sendTime);
            data.setSend_time(timeAgo);
        }

        wishDataList.clear();
       wishDataList.addAll(posts);
      wishAdapter.notifyDataSetChanged();

        int lastIndex = wishAdapter.getItemCount() - 1;
       wishProductRecyclerView.scrollToPosition(lastIndex);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Add onPause lifecycle code here
    }

    @Override
    public void onStop() {
        super.onStop();
        // Add onStop lifecycle code here
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Add onDestroyView lifecycle code here
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Add onDestroy lifecycle code here
    }
}
