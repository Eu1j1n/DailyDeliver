package com.example.dailydeliver.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.dailydeliver.ApiService;
import com.example.dailydeliver.R;
import com.example.dailydeliver.RetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
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

    String baseUri = "http://52.79.88.52/";

    public HomeFragment() {
        homeData = new ArrayList<>();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDIT_POST && resultCode == Activity.RESULT_OK) {
            // EditPostActivity에서 전달된 데이터 가져오기


            // 새로운 HomeData 객체 생성

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 프래그먼트 레이아웃을 인플레이트합니다.
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // RecyclerView를 findViewById를 사용하여 찾습니다.
        recyclerView = view.findViewById(R.id.productRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true); // 데이터를 역순으로 표시
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        postButton = view.findViewById(R.id.postButton);
        homeAdapter = new HomeAdapter(getContext(), homeData);
        recyclerView.setAdapter(homeAdapter);
        progressBar = view.findViewById(R.id.homeProgressBar);





        editPostLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        onActivityResult(REQUEST_CODE_EDIT_POST, Activity.RESULT_OK, data);
                    }
                }
        );

        Bundle bundle = getArguments();
        if (bundle != null) {
            receivedID = bundle.getString("receivedID"); // 전역 변수에 할당
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
                // 새로고침 작업 수행
                fetchDataFromServer();
            }
        });

        fetchDataFromServer();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 화면이 다시 보일 때마다 서버에서 데이터를 다시 받아와서 갱신합니다.
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

                    // 이미지 URI를 로그로 출력
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
                    // 서버에서 받아온 데이터를 홈 프래그먼트에 전달하여 리사이클러뷰에 표시
                } else {
                    Log.d(TAG, "onResponse: 실패냐?");
                    // 서버 응답이 실패했을 때 처리
                }
                // 새로고침 완료
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<HomeData>> call, Throwable t) {
                Log.e(TAG, "에러 6= " + t.getMessage());
                // 새로고침 완료
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }




    private void displayPosts(List<HomeData> posts) {
        // 시간 데이터를 처리하여 변환
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



}
