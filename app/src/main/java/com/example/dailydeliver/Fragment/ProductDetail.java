package com.example.dailydeliver.Fragment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
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
import com.example.dailydeliver.profile.CreditResponse;
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

    TextView bidPrice, remainingTime, hopePriceTextView,  currentServerBidPrice;

    RadioButton buyBidButton, buyButton;

    RadioGroup purchaseRadioGroup;

    private String title, location, price, userName;

    int selectedButton = 0;

    private boolean isActivitySwitchInProgress = false;

    private int getCurrentBidPriceByServer;

    private ImageButton likeButton;
    private ImageButton unlikeButton;
    private int isLiked = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);


        findByViewID();

        immediateEditText.setFocusableInTouchMode(false);


        Intent intent = getIntent();

        title = intent.getStringExtra("title");
        location = intent.getStringExtra("location");
        String sendTime = intent.getStringExtra("send_time");
        price = intent.getStringExtra("price");
        userName = intent.getStringExtra("user_name");
        receivedID = intent.getStringExtra("receivedID"); // 현재 로그인한 아이디



        getImageData(title, location, price, userName ,receivedID);
        getProfileImage(userName);

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLiked = 0;
                switchButtons();
                sendUnLikeRequest(title,location,price,userName, receivedID);

            }
        });

        unlikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLiked = 1;
                switchButtons();
                sendLikeRequest(title,location,price,userName, receivedID);
            }
        });



        bidUpdateToServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hopePrice = inputBidPriceEditText.getText().toString().trim(); // 공백 제거

                if (hopePrice.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "희망가를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                hopePrice = hopePrice.replaceAll("[^0-9]", ""); // 숫자만 남기고 제거

                if (hopePrice.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "희망가를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 서버에서 현재 입찰가를 가져오는 메소드 호출
                getcurrenTBidPrice(title, location, price, userName);

                int inputBidPrice = Integer.parseInt(hopePrice);

                Log.d(TAG, "inputBidPrice" + inputBidPrice);


                String immediatePriceStr = immediatePrice.replaceAll("[^\\d.]", "");
                int immediatePurchasePrice = Integer.parseInt(immediatePriceStr);


                // 입력된 값들에 따라 처리
                if (inputBidPrice >= immediatePurchasePrice) {
                    selectedButton = 1;
                    updateButton();
                    radioGroup.check(R.id.buyButton);

                } else {
                    fetchCreditFromServer(receivedID, inputBidPrice);
                }
            }
        });


        immediateBuyToServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다이얼로그 생성
                CustomDialog dialog = new CustomDialog(ProductDetail.this, "정말로 구매하시겠습니까?",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // '예' 버튼이 눌렸을 때의 동작
                                String immediatePrice = immediateEditText.getText().toString().replaceAll("[^\\d.]", "");
                                Log.d(TAG, "imeediatePrice" + immediatePrice);
                                int inputImmediatePrice = Integer.parseInt(immediatePrice);
                                fetchImmediateCreditFromServer(receivedID, inputImmediatePrice);
                            }
                        },
                        null);  // '아니요' 버튼은 아무 동작도 하지 않음

                // 다이얼로그 표시
                dialog.show();
            }
        });




        inputBidPriceEditText.addTextChangedListener(new TextWatcher() { // 희망가입력
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

    private void switchButtons() {
        Log.d(TAG, "이떄의 isLiked" + isLiked);

        switch (isLiked) {

            case 0:

                unlikeButton.setVisibility(View.VISIBLE);
                likeButton.setVisibility(View.GONE);

                break;
            case 1:
                unlikeButton.setVisibility(View.GONE);
                likeButton.setVisibility(View.VISIBLE);

                break;
        }
    }

    private void getImageData(String title, String location, String price, String userName, String receivedID) {
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);

        Call<List<PostDetailData>> call = apiService.getPostDetail(title, location, price, userName, receivedID);


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

    private void updateBidPriceToServer(String title, String location, String price, String userName, int bidPrice, String receivedID) {
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
        Log.d(TAG, "updateBidPriceToserver: " + bidPrice);

        // 현재입찰가, 즉시 구매가 비교
        Call<currentBidPriceData> call = apiService.updateBidPrice(title, location, price, userName, bidPrice, receivedID);

        call.enqueue(new Callback<currentBidPriceData>() {
            @Override
            public void onResponse(Call<currentBidPriceData> call, Response<currentBidPriceData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentBidPriceData updatedBidPrice = response.body();
                    int updatedPrice = updatedBidPrice.getBidPrice();

                    Log.d(TAG, "updatedPrice" + updatedPrice);


                    // 즉시 구매가를 서버에서 처리 후 적절한 비교 로직 적용
                    if (bidPrice >= Integer.parseInt(price.replace("₩", "").replace(",", ""))) {
                        // 즉시 구매가를 초과하였을 때의 처리

                        currentServerBidPrice.setText("현재입찰가 ₩" + String.format("%,d",updatedPrice));
                    } else if (updatedPrice > bidPrice) {
                        // 입찰가가 업데이트 되었으나, 즉시 구매가가 아닌 경우
                        Toast.makeText(getApplicationContext(), "희망하는 가격보다 현재 입찰가가 더 높습니다.", Toast.LENGTH_SHORT).show();
                        currentServerBidPrice.setText("현재입찰가 ₩" + String.format("%,d",updatedPrice));
                    } else {
                        // 입찰가가 성공적으로 업데이트된 경우
                        currentServerBidPrice.setText("현재입찰가 ₩" + String.format("%,d", updatedPrice));

                        String toastText = "입찰가가 업데이트되었습니다.<br/>    새로운 입찰가: ₩" + String.format("%,d", updatedPrice);
                        Toast.makeText(getApplicationContext(), Html.fromHtml("<div align='center'>" + toastText + "</div>"), Toast.LENGTH_SHORT).show();

                    }

                } else {
                    Log.d(TAG, "서버 응답이 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<currentBidPriceData> call, Throwable t) {
                Log.e(TAG, "통신 실패123: " + t.getMessage());
            }
        });
    }

    private void updateImmediateBuyServer(String title, String location, String price, String userName, String receivedID) {
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
        // 즉시 구매 구매 확정 메소드 및 구매 state 업데이트 메소드

        Call<Void> call = apiService.updateImmediateBuy(title, location, price, userName, receivedID);

        Log.d(TAG, "값들" +  title + location + price + userName + receivedID);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    selectedButton = 1;
                    buyBidButton.setEnabled(false);
                    buyButton.setEnabled(false);
                    immediateEditText.setText("낙찰된 제품입니다.");

                    immediateBuyToServerButton.setEnabled(false);
                    Drawable grayButton = ContextCompat.getDrawable(ProductDetail.this, R.drawable.gray_button);
                    immediateBuyToServerButton.setBackground(grayButton);
                    remainingTime.setText("입찰 종료");

                    updateCreditOnServer(receivedID, price);

                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }

                } else {
                    Log.d(TAG, "서버 응답이 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "통신 실패: " + t.getMessage());
            }
        });
    }

    private void updateCreditOnServer(String userId, String price) {
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);

        Call<Void> call = apiService.updateUserCredit(userId, price);

        Log.d(TAG, "userID" + userId + "price" + price);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // 성공적으로 업데이트된 경우
                    Log.d(TAG, "사용자 credit 업데이트 성공");
                } else {
                    Log.d(TAG, "사용자 credit 업데이트 실패");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "통신 실패: " + t.getMessage());
            }
        });
    }








    private void getcurrenTBidPrice(String title, String location, String price, String userName) {
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);

        Log.d(TAG, "title, location, price: " + title + ", " + location + ", " + price + "," + userName);
        Call<currentBidPriceData> call = apiService.getCurrentBidPrice(title, location, price, userName);

        call.enqueue(new Callback<currentBidPriceData>() {
            @Override
            public void onResponse(Call<currentBidPriceData> call, Response<currentBidPriceData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentBidPriceData currentBidPrice = response.body();
                    int currentBid = currentBidPrice.getBidPrice(); // 현재 입찰가

                    // 사용자가 입력한 희망가 가져오기
                    String hopePriceStr = inputBidPriceEditText.getText().toString().trim();
                    int hopePrice = Integer.parseInt(hopePriceStr.replaceAll("[^0-9]", ""));
                    Log.d(TAG, "currentBid" + currentBid);
                    Log.d(TAG, "hopePrice" + hopePrice);

                    // 희망가와 현재 입찰가 비교
                    if (hopePrice <= currentBid) {
                        Log.d(TAG, "여기들어옴?");
                        currentServerBidPrice.setText(String.valueOf(currentBid));

                    } else {
                        //희망가가 현재 보유 크레딧 보다 높으면
                        fetchCreditFromServer(receivedID, hopePrice);
                    }
                } else {
                    Log.d(TAG, "서버 응답이 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<currentBidPriceData> call, Throwable t) {
                Log.e(TAG, "통신 실패: " + t.getMessage());
            }
        });
    }






    private void fetchCreditFromServer(String receiveID, int bidPrice) {
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
        Call<CreditResponse> call = apiService.getCredit(receiveID);

        Log.d(TAG, "fetchCreditFromServer bidPrice" + bidPrice);

        call.enqueue(new Callback<CreditResponse>() {
            @Override
            public void onResponse(Call<CreditResponse> call, Response<CreditResponse> response) {
                if (response.isSuccessful()) {
                    CreditResponse creditResponse = response.body();
                    int currentCredit = creditResponse.getCredit();

                    // 현재 보유 크레딧과 희망가를 비교
                    int difference = currentCredit - bidPrice;
                    if (difference < 0) {
                        // 희망가가 현재 크레딧보다 크면 토스트를 표시
                        Toast.makeText(getApplicationContext(), "보유 크레딧이 부족합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "야이떄 receiveID" + receiveID);
                        updateBidPriceToServer(title, location, price, userName, bidPrice, receiveID);
                    }
                } else {
                    Log.e(TAG, "서버 응답이 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<CreditResponse> call, Throwable t) {
                Log.e(TAG, "통신 실패: " + t.getMessage());
            }
        });
    }

    private void sendUnLikeRequest(String title, String location, String price, String userName, String receivedID) {
        // LikeData 객체 생성
        UnLikeData unLikeData = new UnLikeData();
        unLikeData.setTitle(title);
        unLikeData.setLocation(location);
        unLikeData.setPrice(price);
        unLikeData.setUserName(userName);
        unLikeData.setReceiveID(receivedID);

        // Retrofit 인터페이스를 사용하여 서버에 요청을 보냄
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);

        // 서버에 LikeData 객체를 전송
        Call<Void> call = apiService.sendUnLikeRequest(unLikeData);

        Log.d(TAG, "title location price userName " + title + location + price + userName);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: 어케됨??");

                } else {
                    // 요청이 실패한 경우
                    Log.d(TAG, "서버 응답이 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // 통신이 실패한 경우
                Log.e(TAG, "통신 실패: " + t.getMessage());
            }
        });
    }

    private void sendLikeRequest(String title, String location, String price, String userName, String receivedID) {
        // LikeData 객체 생성
        LikeData likeData = new LikeData();
        likeData.setTitle(title);
        likeData.setLocation(location);
        likeData.setPrice(price);
        likeData.setUserName(userName);
        likeData.setReceiveID(receivedID);

        // Retrofit 인터페이스를 사용하여 서버에 요청을 보냄
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);

        // 서버에 LikeData 객체를 전송
        Call<Void> call = apiService.sendLikeRequest(likeData);

        Log.d(TAG, "title location price userName " + title + location + price + userName);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: 어케됨??");

                } else {
                    // 요청이 실패한 경우
                    Log.d(TAG, "서버 응답이 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // 통신이 실패한 경우
                Log.e(TAG, "통신 실패: " + t.getMessage());
            }
        });
    }


    private void fetchImmediateCreditFromServer(String receiveID, int inputImmediatePrice) {
        // 즉시구매 눌렀을때 유저의 크레딧이 즉시구매가 보다 적은지 높은지 확인하는 메소드
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
        Call<CreditResponse> call = apiService.getCredit(receiveID);

        call.enqueue(new Callback<CreditResponse>() {
            @Override
            public void onResponse(Call<CreditResponse> call, Response<CreditResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "즉시구매 성공 로그임?");
                    CreditResponse creditResponse = response.body();
                    int currentCredit = creditResponse.getCredit();

                    // 현재 보유 크레딧과 희망가를 비교
                    int difference = currentCredit - inputImmediatePrice;
                    if (difference < 0) {
                        // 희망가가 현재 크레딧보다 크면 토스트를 표시
                        Toast.makeText(getApplicationContext(), "보유 크레딧이 부족합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        updateImmediateBuyServer(title,location,price,userName,receivedID);



                    }
                } else {
                    Log.e(TAG, "서버 응답이 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<CreditResponse> call, Throwable t) {
                Log.e(TAG, "통신 실패: " + t.getMessage());
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
            Log.d(TAG, "좋아요값" + imageData.getLikeStatus());

            if (imageData.getLikeStatus() == 1) {
                isLiked = 1; // 좋아요 상태
            } else {
                isLiked = 0; // 좋아요 취소 상태
            }
            switchButtons();


            immediatePrice = imageData.getPrice();

            if (receivedID != null && imageData.getUserName() != null && receivedID.equals(imageData.getUserName())) {
                chatButton.setVisibility(View.GONE);
            } else {
                chatButton.setVisibility(View.VISIBLE);
            }

            if (imageData.getState() == 1) { // 상태 값이 1일 때
                if (imageData.getSaleType().equals("bid")) {
                    // bid 타입인 경우의 처리
                    Log.d(TAG, "handleImageData: bid 타입 처리");
                    Drawable grayButton = ContextCompat.getDrawable(ProductDetail.this, R.drawable.gray_button);
                    inputBidPriceEditText.setText("낙찰된 제품입니다.");
                    bidUpdateToServerButton.setBackground(grayButton);
                    bidUpdateToServerButton.setEnabled(false);
                    immediateBuyToServerButton.setEnabled(false);

                    immediateEditText.setText("낙찰된 제품입니다.");
                    immediateBuyToServerButton.setBackground(grayButton);
                    remainingTime.setText("입찰 종료");
                } else if (imageData.getSaleType().equals("immediate")) {
                    // immediate 타입인 경우의 처리
                    Log.d(TAG, "handleImageData: immediate 타입 처리");
                    buyButton.setChecked(true);
                    buyBidButton.setEnabled(false);
                    inputBidPriceEditText.setEnabled(false);
                    inputBidPriceEditText.setVisibility(View.GONE);
                    priceTextView.setText("즉시구매가 " + imageData.getPrice());
                    bidUpdateToServerButton.setVisibility(View.GONE);
                    currentServerBidPrice.setVisibility(View.GONE);
                    immediateEditText.setVisibility(View.VISIBLE);


                    Drawable grayButton = ContextCompat.getDrawable(ProductDetail.this, R.drawable.gray_button);
                    immediateEditText.setText("판매가 완료되었습니다.");
                    immediateBuyToServerButton.setBackground(grayButton);
                    immediateBuyToServerButton.setEnabled(false);
                    remainingTime.setText("입찰 종료");
                }
            } else { // 상태 값이 0일 때
                Log.d(TAG, "여긴아니지?");
                if (imageData.getSaleType().equals("immediate")) {
                    remainingTime.setVisibility(View.GONE);
                    currentServerBidPrice.setVisibility(View.GONE);
                    hopePriceTextView.setVisibility(View.GONE);
                    inputBidPriceEditText.setVisibility(View.GONE);
                    bidUpdateToServerButton.setVisibility(View.GONE);


                    buyButton.setChecked(true);
                    buyBidButton.setEnabled(false);
                    selectedButton = 1;

                    updateButton();
                    priceTextView.setText("즉시구매가 " + imageData.getPrice() + "원");
                    immediateEditText.setText(imageData.getPrice() + "원");
                } else {
                    remainingTime.setVisibility(View.VISIBLE);
                    currentServerBidPrice.setVisibility(View.VISIBLE);
                    radioGroup.setVisibility(View.VISIBLE);
                    hopePriceTextView.setVisibility(View.VISIBLE);
                    inputBidPriceEditText.setVisibility(View.VISIBLE);
                    bidUpdateToServerButton.setVisibility(View.VISIBLE);

                    priceTextView.setText("즉시구매가 " + imageData.getPrice());

                    // "남은 시간" 값이 카운트다운 타이머 형식인지 확인
                    if (imageData.getRemaining_time().matches("\\d{2}:\\d{2}:\\d{2}")) {
                        // 카운트다운 타이머 시작
                        startCountdownTimer(imageData.getRemaining_time());
                    } else if (imageData.getRemaining_time().equals("입찰 종료")) {
                        bidUpdateToServerButton.setEnabled(false);
                        immediateBuyToServerButton.setEnabled(false);
                        immediateEditText.setHint("입찰이 종료되었습니다.");
                        inputBidPriceEditText.setHint("입찰이 종료되었습니다.");
                        immediateEditText.setEnabled(false);
                        inputBidPriceEditText.setEnabled(false);
                        remainingTime.setText("입찰 종료");
                    } else {
                        // 다른 형식의 경우 그냥 setText로 설정
                        remainingTime.setText(imageData.getRemaining_time());
                    }
                }
            }

            currentServerBidPrice.setText("현재입찰가 " + imageData.getBidPrice());

            String location = imageData.getLocation();
            String formattedTime = TimeUtil.getTimeAgo(imageData.getSend_time());
            String text = location + " · " + formattedTime;
            SpannableString spannableString = new SpannableString(text);

            int startIndex = text.indexOf("·") + 2; // "●" 다음 공백 포함
            RelativeSizeSpan sizeSpan = new RelativeSizeSpan(0.8f);
            spannableString.setSpan(sizeSpan, startIndex, text.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            locationTextView.setText(spannableString);
            titleTextView.setText(imageData.getTitle());
            nicknameTextView.setText(imageData.getUserName());
            descriptionTextView.setText(imageData.getDescription());

            latitude = imageData.getLatitude();
            longitude = imageData.getLongitude();

            if (latitude != null && longitude != null) {
                getAddressFromLocation(latitude, longitude);
            }

            List<String> imageUrls = imageData.getImage_uri(); // 이미지 URI 리스트를 가져옴
            imagePagerAdapter = new ImagePagerAdapter(ProductDetail.this, imageUrls, new ImagePagerAdapter.OnImageClickListener() {
                @Override
                public void onImageClick(int position) {
                    String clickedImageUrl = imageUrls.get(position);
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

        long millisInFuture = parseRemainingTime(remainingTimeStr);

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
                        profileImageView.setImageResource(R.drawable.marketapplogo);
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

    private void findByViewID() {
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
        currentServerBidPrice = findViewById(R.id.currentBidPrice);
        remainingTime = findViewById(R.id.remainingTime);
        radioGroup = findViewById(R.id.purchaseRadioGroup);
        inputBidPriceEditText = findViewById(R.id.inputBidPriceEditText);
        bidUpdateToServerButton = findViewById(R.id.bidUpdateToServerButton);
        hopePriceTextView = findViewById(R.id.hopePriceTextView);
        buyButton = findViewById(R.id.buyButton);
        buyBidButton = findViewById(R.id.buyBidButton);
        immediateEditText = findViewById(R.id.immediateEditText);
        immediateBuyToServerButton = findViewById(R.id.immediateBuyToServerButton);
        likeButton = findViewById(R.id.likeButton);
        unlikeButton = findViewById(R.id.unlikeButton);

    }


    @Override
    public void onImageClick(int position) {

    }
}
