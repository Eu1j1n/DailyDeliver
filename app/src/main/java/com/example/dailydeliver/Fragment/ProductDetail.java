package com.example.dailydeliver.Fragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.os.CountDownTimer;
import android.widget.Toast;

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


import java.io.IOException;
import java.text.NumberFormat;
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


// CountDownTimer 객체 선언
private CountDownTimer countDownTimer;

private String globalBidPrice;

// 각 위젯들을 참조하기 위한 변수 선언
ImageButton backButton, expansionMapButton;
ViewPager2 viewPager;
CircleImageView profileImageView;
TextView nicknameTextView, locationTextView, titleTextView, timeTextView, descriptionTextView, priceTextView, detailAddress;
Button chatButton, bidUpdateToServerButton, immediateBuyToServerButton;

EditText inputBidPriceEditText, immediateEditText;
RadioGroup radioGroup;

MapView productDetailMapView;

private GoogleMap mMap;

Double latitude, longitude;

private String immediatePrice;
private String receivedID;

String TAG = "디테일 액티비티";

private ImagePagerAdapter imagePagerAdapter;

String baseUri = "http://43.201.32.122/";

String imageUri = "http://43.201.32.122/postImage/";

CircleIndicator3 circleIndicator;

TextView bidPrice, remainingTime, hopePriceTextView;

RadioButton buyBidButton, buyButton;

RadioGroup purchaseRadioGroup;

int selectedButton = 0;

private boolean isActivitySwitchInProgress = false;

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
    descriptionTextView = findViewById(R.id.descriptionTextView);
    priceTextView = findViewById(R.id.priceTextView);
    chatButton = findViewById(R.id.chatButton);
    circleIndicator = findViewById(R.id.circleIndicator);
    productDetailMapView = findViewById(R.id.productDetailMapView);
    detailAddress = findViewById(R.id.detailAdress);
    expansionMapButton = findViewById(R.id.expansionMapButton);
    bidPrice = findViewById(R.id.currentBidPrice);
    remainingTime = findViewById(R.id.remainingTime);
    radioGroup = findViewById(R.id.purchaseRadioGroup);
    inputBidPriceEditText = findViewById(R.id.inputBidPriceEditText);
    bidUpdateToServerButton = findViewById(R.id.bidUpdateToServerButton);
    hopePriceTextView = findViewById(R.id.hopePriceTextView);
    buyButton = findViewById(R.id.buyButton);
    buyBidButton = findViewById(R.id.buyBidButton);
    immediateEditText= findViewById(R.id.immediateEditText);
    immediateBuyToServerButton = findViewById(R.id.immediateBuyToServerButton);

    immediateEditText.setFocusableInTouchMode(false);




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

    bidUpdateToServerButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String inputBidPriceStr = inputBidPriceEditText.getText().toString().replaceAll("[^\\d.]", "");
            int inputBidPrice = Integer.parseInt(inputBidPriceStr);

            String globalBidPriceStr = globalBidPrice.replaceAll("[^\\d.]", "");
            int currentBidPrice = Integer.parseInt(globalBidPriceStr);

            String immediatePriceStr = immediatePrice.replaceAll("[^\\d.]", "");
            int immediatePurchasePrice = Integer.parseInt(immediatePriceStr);

            if (inputBidPrice <= currentBidPrice) {
                Toast.makeText(getApplicationContext(), "희망가가 입찰가보다 낮습니다.", Toast.LENGTH_SHORT).show();
            } else if (inputBidPrice >= immediatePurchasePrice) {
                selectedButton = 1;
                updateButton();


                radioGroup.check(R.id.buyButton);
                Toast.makeText(getApplicationContext(), "희망가가 즉시 구매가보다 높거나 같습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    });


    inputBidPriceEditText.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // 이 메소드는 텍스트가 변경되기 전에 호출됩니다.
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // 이 메소드는 텍스트가 변경될 때 호출됩니다.
        }

        @Override
        public void afterTextChanged(Editable s) {
            inputBidPriceEditText.removeTextChangedListener(this);

            try {
                String originalString = s.toString();
                // '원' 문자를 제거
                originalString = originalString.replace("원", "");

                // 콤마와 소수점을 제거
                String cleanString = originalString.replaceAll("[,\\.]", "");

                if (!cleanString.isEmpty()) {
                    // 숫자 형태로 파싱
                    long parsed = Long.parseLong(cleanString);

                    // 숫자를 해당 지역의 통화 포맷으로 변환
                    String formatted = NumberFormat.getNumberInstance(Locale.KOREA).format(parsed);

                    // 변환된 텍스트에 '원'을 붙히기
                    inputBidPriceEditText.setText(formatted + "원");
                    // 커서 원 글자 앞으로 ㄱㄱ
                    inputBidPriceEditText.setSelection(formatted.length());
                } else {
                    // 입력 한거 없음 힌트
                    inputBidPriceEditText.setText("");
                    inputBidPriceEditText.setHint("희망가 입력");
                }

                // 텍스트를 볼드체로 ~
                inputBidPriceEditText.setTypeface(null, Typeface.BOLD);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            inputBidPriceEditText.addTextChangedListener(this);
        }

    });

    buyBidButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectedButton = 0;
            updateButton();

        }
    });





    buyButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectedButton = 1;
            updateButton();

        }
    });

    chatButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String firstUserID = receivedID; // 첫 번째 사용자 ID
            String secondUserID = nicknameTextView.getText().toString(); // 두 번째 사용자 ID

            // 사용자 ID를 알파벳 순서로 정렬하여 대화방 이름 생성
            String sortedRoomName;
            if (firstUserID.compareTo(secondUserID) < 0) {
                sortedRoomName = firstUserID + "01072047094" + secondUserID;
            } else {
                sortedRoomName = secondUserID + "01072047094" + firstUserID;
            }

            // 대화방으로 전달할 데이터 설정
            Intent chatIntent = new Intent(ProductDetail.this, Chatting.class);
            chatIntent.putExtra("roomName", sortedRoomName);
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
                Log.d(TAG, "imageData" + imageDataList);
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

private void updateButton() {
    Log.d(TAG, "updateButton: 들어오고 " + selectedButton);
    switch (selectedButton) {
        case 0: // 판매 입찰 선택
            hopePriceTextView.setText("구매 희망가");
            bidUpdateToServerButton.setVisibility(View.VISIBLE);
            inputBidPriceEditText.setVisibility(View.VISIBLE);
            immediateEditText.setVisibility(View.GONE);
            immediateBuyToServerButton.setVisibility(View.GONE);
            bidUpdateToServerButton.setVisibility(View.VISIBLE);



            break;
        case 1: // 즉시 판매 선택

            hopePriceTextView.setText("즉시 구매가");
            inputBidPriceEditText.setVisibility(View.GONE);
            bidUpdateToServerButton.setVisibility(View.GONE);
            immediateEditText.setVisibility(View.VISIBLE);
            String priceWithoutSymbol = immediatePrice.replace("₩", "");
            immediateBuyToServerButton.setVisibility(View.VISIBLE);
            bidUpdateToServerButton.setVisibility(View.GONE);

            immediateEditText.setText(priceWithoutSymbol + "원");


            break;
    }
}

private void handleImageData(List<PostDetailData> imageDataList) {
    // 이미지 데이터 목록이 비어있는지 확인
    if (imageDataList != null && !imageDataList.isEmpty()) {
        // 첫 번째 이미지 데이터 가져오기
        PostDetailData imageData = imageDataList.get(0);
        globalBidPrice = imageData.getBidPrice();


        Log.d(TAG, "입찰가 " + imageData.getBidPrice());
        Log.d(TAG, "타입" + imageData.getSaleType());
        Log.d(TAG, "남은 시간" + imageData.getRemaining_time());

        immediatePrice = imageData.getPrice();


        if (receivedID != null && imageData.getUserName() != null && receivedID.equals(imageData.getUserName())) {
            chatButton.setVisibility(View.GONE);
        } else {
            chatButton.setVisibility(View.VISIBLE);
        }

        if(imageData.getSaleType().equals("immediate")) {
            remainingTime.setVisibility(View.GONE);
            bidPrice.setVisibility(View.GONE);

            hopePriceTextView.setVisibility(View.GONE);
            inputBidPriceEditText.setVisibility(View.GONE);
            bidUpdateToServerButton.setVisibility(View.GONE);


            buyButton.setChecked(true);
            buyBidButton.setEnabled(false);
            selectedButton = 1;

            updateButton();


            immediateEditText.setText(imageData.getPrice() + "원");
        } else {
            remainingTime.setVisibility(View.VISIBLE);
            bidPrice.setVisibility(View.VISIBLE);
            radioGroup.setVisibility(View.VISIBLE);
            hopePriceTextView.setVisibility(View.VISIBLE);
            inputBidPriceEditText.setVisibility(View.VISIBLE);
            bidUpdateToServerButton.setVisibility(View.VISIBLE);


            priceTextView.setText("즉시구매가 " + imageData.getPrice());

            // "남은 시간" 값이 카운트다운 타이머 형식인지 확인
            if (imageData.getRemaining_time().matches("\\d{2}:\\d{2}:\\d{2}")) {
                // 카운트다운 타이머 시작
                startCountdownTimer(imageData.getRemaining_time());
            }else if(imageData.getRemaining_time().equals("입찰 종료")) {
                bidUpdateToServerButton.setEnabled(false);
                immediateBuyToServerButton.setEnabled(false);
                immediateEditText.setHint("입찰이 종료되었습니다.");
                inputBidPriceEditText.setHint("입찰이 종료되었습니다.");
                immediateEditText.setEnabled(false);
                inputBidPriceEditText.setEnabled(false);
                remainingTime.setText("입찰 종료");


            }else {
                // 다른 형식의 경우 그냥 setText로 설정
                remainingTime.setText(imageData.getRemaining_time());
            }
        }

        bidPrice.setText("현재입찰가 " + imageData.getBidPrice());

        String location = imageData.getLocation();
        String formattedTime = TimeUtil.getTimeAgo(imageData.getSend_time());
        String text = location + " · " + formattedTime;

        SpannableString spannableString = new SpannableString(text);


        int startIndex = text.indexOf("·") + 2; // "●" 다음 공백 포함


        RelativeSizeSpan sizeSpan = new RelativeSizeSpan(0.8f);


        spannableString.setSpan(sizeSpan, startIndex, text.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);


        locationTextView.setText(spannableString);

        titleTextView.setText(imageData.getTitle());

        Log.d(TAG, "imageData.getSendTime" + imageData.getSend_time());

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

private void startCountdownTimer(String remainingTimeStr) {
    // 시간을 밀리초로 변환하여 카운트다운 타이머 설정
    long millisInFuture = parseRemainingTime(remainingTimeStr);
    // 기존 타이머가 있다면 취소
    if (countDownTimer != null) {
        countDownTimer.cancel();
    }
    countDownTimer = new CountDownTimer(millisInFuture, 1000) {
        public void onTick(long millisUntilFinished) {
            // 시:분:초 형식으로 변환하여 TextView에 설정

            long hours = (millisUntilFinished / (1000 * 60 * 60)) % 24;
            long minutes = (millisUntilFinished / (1000 * 60)) % 60;
            long seconds = (millisUntilFinished / 1000) % 60;
            String timeFormatted = String.format(Locale.getDefault(), "%02d: %02d: %02d", hours, minutes, seconds);
            remainingTime.setText(timeFormatted);
        }

        public void onFinish() {
            bidUpdateToServerButton.setEnabled(false);
            immediateBuyToServerButton.setEnabled(false);
            immediateEditText.setHint("입찰이 종료되었습니다.");
            inputBidPriceEditText.setHint("입찰이 종료되었습니다.");
            immediateEditText.setEnabled(false);
            inputBidPriceEditText.setEnabled(false);
            remainingTime.setText("입찰 종료");

        }
    }.start();
}

// "남은 시간"을 밀리초로 변환하는 메서드
private long parseRemainingTime(String remainingTimeStr) {
    String[] parts = remainingTimeStr.split(":");
    long hours = Long.parseLong(parts[0]);
    long minutes = Long.parseLong(parts[1]);
    long seconds = Long.parseLong(parts[2]);
    return (hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000);
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
