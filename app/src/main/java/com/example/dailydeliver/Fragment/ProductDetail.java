package com.example.dailydeliver.Fragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.dailydeliver.ApiService;
import com.example.dailydeliver.Chatting.Chatting;
import com.example.dailydeliver.ImageResponse;
import com.example.dailydeliver.R;
import com.example.dailydeliver.RetrofitClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.naver.maps.geometry.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import me.relex.circleindicator.CircleIndicator;
import me.relex.circleindicator.CircleIndicator3;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetail extends AppCompatActivity implements ImagePagerAdapter.OnImageClickListener {
    // ProductDetail 클래스 코드 생략

    // 각 위젯들을 참조하기 위한 변수 선언
    ImageButton backButton, expansionMapButton;
    ViewPager2 viewPager;
    CircleImageView profileImageView;
    TextView nicknameTextView, locationTextView, titleTextView, timeTextView, descriptionTextView, priceTextView, detailAddress;
    Button chatButton;

    MapView productDetailMapView;

    private GoogleMap mMap;

    Double latitude, longitude;

    private String receivedID;

    String TAG = "디테일 액티비티";

    private ImagePagerAdapter imagePagerAdapter;

    String baseUri = "http://43.201.32.122/";

    String imageUri = "http://43.201.32.122/postImage/";

    CircleIndicator3 circleIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);


        backButton = findViewById(R.id.backButton);
        viewPager = findViewById(R.id.viewPager);
        profileImageView = findViewById(R.id.profileImageView);
        nicknameTextView = findViewById(R.id.nicknameTextView);
        locationTextView = findViewById(R.id.locationTextView);
        titleTextView = findViewById(R.id.titleTextView);
        timeTextView = findViewById(R.id.timeTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        priceTextView = findViewById(R.id.priceTextView);
        chatButton = findViewById(R.id.chatButton);
        circleIndicator = findViewById(R.id.circleIndicator);
        productDetailMapView = findViewById(R.id.productDetailMapView);
        detailAddress = findViewById(R.id.detailAdress);
        expansionMapButton = findViewById(R.id.expansionMapButton);

        Intent intent = getIntent();

        String title = intent.getStringExtra("title");
        String location = intent.getStringExtra("location");
        String sendTime = intent.getStringExtra("send_time");
        String price = intent.getStringExtra("price");
        String userName = intent.getStringExtra("user_name");
        receivedID = intent.getStringExtra("receivedID");




        // 이미지 데이터 가져오기
        getImageData(title, location, price, userName);
        getProfileImage(userName);

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(ProductDetail.this, Chatting.class);
                chatIntent.putExtra("roomName",receivedID + "01072047094" + nicknameTextView.getText().toString());
                chatIntent.putExtra("id", receivedID);
                startActivity(chatIntent);
            }
        });




        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        productDetailMapView.onCreate(savedInstanceState);
        productDetailMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                // 마커 추가
                if (latitude != null && longitude != null) {
                    Log.d(TAG, "onMapReady: 여기들어왔니?");
                    // 구글 지도의 LatLng 클래스를 사용
                    com.google.android.gms.maps.model.LatLng location = new com.google.android.gms.maps.model.LatLng(latitude, longitude);
                    mMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions().position(location).title("거래 위치"));
                    mMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(location, 17));
                }
            }
        });


        expansionMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ShowLocation 액티비티로 이동하는 인텐트 생성
                Intent showLocationIntent = new Intent(ProductDetail.this, ShowLocation.class);

                showLocationIntent.putExtra("latitude", latitude);
                showLocationIntent.putExtra("longitude", longitude);
                // 액티비티 이동
                startActivity(showLocationIntent);
            }
        });




    }

    private void getImageData(String title, String location, String price, String userName){
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);

        // 해당하는 title, location, price, userName 값을 쿼리 파라미터로 전달하여 서버에 요청합니다.
        Call<List<PostDetailData>> call = apiService.getPostDetail(title, location, price, userName);


        call.enqueue(new Callback<List<PostDetailData>>() {
            @Override
            public void onResponse(Call<List<PostDetailData>> call, Response<List<PostDetailData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 응답을 처리하는 코드
                    List<PostDetailData> imageDataList = response.body();
                    // 데이터를 받아와서 처리
                    handleImageData(imageDataList);
                } else {
                    Log.d(TAG, "오류임?");
                }
            }

            @Override
            public void onFailure(Call<List<PostDetailData>> call, Throwable t) {
                Log.e(TAG, "Communication failure: " + t.getMessage());
            }
        });
    }

    private void handleImageData(List<PostDetailData> imageDataList) {
        // 이미지 데이터 목록이 비어있는지 확인
        if (imageDataList != null && !imageDataList.isEmpty()) {
            // 첫 번째 이미지 데이터 가져오기
            PostDetailData imageData = imageDataList.get(0);
            Log.d(TAG, "");

            if (receivedID != null && imageData.getUserName() != null && receivedID.equals(imageData.getUserName())) {
                chatButton.setVisibility(View.GONE);
            } else {
                chatButton.setVisibility(View.VISIBLE);
            }

            // 각 TextView에 데이터 설정
            titleTextView.setText(imageData.getTitle());
            locationTextView.setText(imageData.getLocation());
            String formattedTime = TimeUtil.getTimeAgo(imageData.getSend_time());
            timeTextView.setText(formattedTime);

            Log.d(TAG, "imageData.getSendTime" + imageData.getSend_time());
            String formattedPrice = PriceUtil.formatPrice(imageData.getPrice());
            priceTextView.setText(formattedPrice);
            nicknameTextView.setText(imageData.getUserName());
            descriptionTextView.setText(imageData.getDescription());

            latitude = imageData.getLatitude();
            longitude = imageData.getLongitude();

            if (latitude != null && longitude != null) {
                getAddressFromLocation(latitude, longitude);
            }

            Log.d(TAG, "latitude" + imageData.getLatitude());
            Log.d(TAG, "longitude" + imageData.getLongitude());
            Log.d(TAG, "imageData.getImage+uri" + imageData.getImage_uri());

            List<String> imageUrls = imageData.getImage_uri(); // 이미지 URI 리스트를 가져옴

            imagePagerAdapter = new ImagePagerAdapter(ProductDetail.this, imageUrls, new ImagePagerAdapter.OnImageClickListener() {
                @Override
                public void onImageClick(int position) {
                    String clickedImageUrl = imageUrls.get(position);
                    Log.d(TAG, "clickedImageUri" + clickedImageUrl);
                    Intent intent = new Intent(ProductDetail.this, ImageDetail.class);
                    intent.putExtra("imageUrl", imageUri + clickedImageUrl);
                    startActivity(intent);

                }
            });
            viewPager.setAdapter(imagePagerAdapter);
            circleIndicator.setViewPager(viewPager);
        }
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String detailAddressText = address.getAddressLine(0); // 첫 번째 주소를 가져오기
                detailAddressText = detailAddressText.replace("대한민국", "");
                detailAddress.setText("상세 주소 :" + detailAddressText);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void getProfileImage(String receiveID) {
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);

        // receiveID 값을 서버에 전송하여 프로필 이미지 링크를 받아오는 요청
        Call<ImageResponse> call = apiService.getImageFileName(receiveID);
        call.enqueue(new Callback<ImageResponse>() {
            @Override
            public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 응답을 처리하는 코드
                    ImageResponse imageResponse = response.body();
                    // 받아온 이미지 링크를 Glide를 사용하여 ImageView에 표시
                    if (imageResponse.getImagePath() != null && !imageResponse.getImagePath().isEmpty()) {
                        Glide.with(ProductDetail.this)
                                .load(imageResponse.getImagePath())
                                .into(profileImageView);
                    } else {
                        // 링크가 없는 경우 기본 이미지로 설정
                        profileImageView.setImageResource(R.drawable.applogo);
                    }
                } else {
                    Log.d(TAG, "응답 실패");
                }
            }

            @Override
            public void onFailure(Call<ImageResponse> call, Throwable t) {
                Log.e(TAG, "통신 실패: " + t.getMessage());
            }
        });
    }


    @Override
    public void onImageClick(int position) {

    }
}
