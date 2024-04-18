package com.example.dailydeliver.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.dailydeliver.ApiService;
import com.example.dailydeliver.R;
import com.example.dailydeliver.RetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements HomeAdapter.OnItemClickListener {
    private List<HomeData> homeData;
    private RecyclerView recyclerView;
    private HomeAdapter homeAdapter;

    private ProgressBar progressBar;

    private static final int REQUEST_CODE_EDIT_POST = 1001;
    private ActivityResultLauncher<Intent> editPostLauncher;

    SwipeRefreshLayout swipeRefreshLayout;
    String TAG = "홈 프래그먼트";
    FloatingActionButton postButton;
    private String receivedID;
    String baseUri = "http://43.201.32.122/";

    public HomeFragment() {
        homeData = new ArrayList<>();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_POST && resultCode == getActivity().RESULT_OK) {
            // EditPostActivity에서 전달된 데이터 가져오기
            // 새로운 HomeData 객체 생성
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        recyclerView = view.findViewById(R.id.productRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        postButton = view.findViewById(R.id.postButton);
        homeAdapter = new HomeAdapter(getContext(), homeData, this); // 클릭 리스너 추가
        recyclerView.setAdapter(homeAdapter);
        progressBar = view.findViewById(R.id.homeProgressBar);

        editPostLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        Intent data = result.getData();
                        onActivityResult(REQUEST_CODE_EDIT_POST, getActivity().RESULT_OK, data);
                    }
                }
        );

        Bundle bundle = getArguments();
        if (bundle != null) {
            receivedID = bundle.getString("receivedID");
            Log.d(TAG, "Received ID: " + receivedID);
        }

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditPostActivity.class);
                intent.putExtra("receivedID", receivedID);
                editPostLauncher.launch(intent);
            }
        });

        swipeRefreshLayout = view.findViewById(R.id.swiperefreshlayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchDataFromServer();
            }
        });

        fetchDataFromServer();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchDataFromServer();
    }

    public void fetchDataFromServer() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
        Call<List<HomeData>> call = apiService.getPosts();

        call.enqueue(new Callback<List<HomeData>>() {
            @Override
            public void onResponse(Call<List<HomeData>> call, Response<List<HomeData>> response) {
                if (response.isSuccessful()) {
                    List<HomeData> posts = response.body();
                    for (HomeData data : posts) {
                        String imageUrl = data.getImage_uri();
                        String title = data.getTitle();
                        String time = data.getSend_time();
                        String userName = data.getUserName();
                        String description = data.getDescription();
                        Log.d(TAG, "Image URI: " + imageUrl);
                        Log.d(TAG, "onResponse: title" + title);
                        Log.d(TAG, "onResponse: " + time);
                        Log.d(TAG, "userName" + userName);
                        Log.d(TAG, "onResponse: description" + description);
                    }

                    displayPosts(posts);
                    progressBar.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "onResponse: 실패냐?");
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<HomeData>> call, Throwable t) {
                Log.e(TAG, "에러 6= " + t.getMessage());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void displayPosts(List<HomeData> posts) {
        for (HomeData data : posts) {
            String sendTime = data.getSend_time();
            String timeAgo = TimeUtil.getTimeAgo(sendTime);
            data.setSend_time(timeAgo);
        }

        homeData.clear();
        homeData.addAll(posts);
        homeAdapter.notifyDataSetChanged();

        int lastIndex = homeAdapter.getItemCount() - 1;
        recyclerView.scrollToPosition(lastIndex);
    }

    // 클릭 이벤트 처리
    @Override
    public void onItemClick(HomeData item) {
        // 클릭된 아이템의 정보를 받아올 수 있습니다.
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
}
