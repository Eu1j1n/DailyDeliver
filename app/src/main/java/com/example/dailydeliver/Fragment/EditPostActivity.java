package com.example.dailydeliver.Fragment;

import android.app.Activity;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.dailydeliver.ApiService;

import com.example.dailydeliver.Chatting.Chatting;
import com.example.dailydeliver.R;
import com.example.dailydeliver.RetrofitClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class EditPostActivity extends AppCompatActivity implements ImageAdapter.OnImageDeleteListener {

    private ImageButton closeButton;
    private ImageView postImageView, downButtonImageView;

    private  TextView priceTextView, deadline, deadlineTextView;
    private RecyclerView imageRecyclerView;
    private EditText titleEditText;
    private EditText priceEditText;
    private EditText descriptionEditText;
    private TextView locationTextView, editPostTextView;
    private Button completeButton;

    private TextView imageCountTextView;

    private BottomSheetDialog editPostDialog;

    private String TAG = "글쓰는 액티비티";



    private ProgressBar progressBar;

    private EditText bidPriceEditText;

    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;


    private String userName;

    private TextView bidPriceTextView;
    private double latitude;
    private double longitude;

    int selectedButton = 0; // 기본값으로 구매 입찰 버튼값



    private String baseUri = "http://43.201.32.122/";

    String ImageUri = "http://43.201.32.122/postImage/";

    private static final int MAX_IMAGE_COUNT = 10;

    RadioButton bidButton, sellButton;

    TextView warningLOW;



    private List<Uri> selectedImageUris = new ArrayList<>();
    private static final int REQUEST_IMAGE_GET = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        // EditText 및 관련 요소 초기화
        closeButton = findViewById(R.id.closeButton);
        imageRecyclerView = findViewById(R.id.imageRecyclerView);
        titleEditText = findViewById(R.id.titleEditText);
        priceEditText = findViewById(R.id.priceEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        locationTextView = findViewById(R.id.postlocationTextView);
        completeButton = findViewById(R.id.completeButton);
        postImageView = findViewById(R.id.postImageView);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateTime = sdf.format(new Date());
        imageCountTextView = findViewById(R.id.imageCount);
        progressBar = findViewById(R.id.progressBar);
        bidButton = findViewById(R.id.bidButton);
        sellButton = findViewById(R.id.sellButton);
        bidPriceEditText = findViewById(R.id.bidPriceEditText);
        bidPriceTextView = findViewById(R.id.bidPriceTextView);
        priceTextView = findViewById(R.id.priceTextView);
        editPostTextView = findViewById(R.id.editPostTextView);

        warningLOW = findViewById(R.id.warningLow);

        deadline = findViewById(R.id.deadLine);
        deadlineTextView = findViewById(R.id.deadLineTextView);

        downButtonImageView = findViewById(R.id.downButtonImageView);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_MONTH, 1);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String formattedDate = dateFormat.format(calendar.getTime());


        deadlineTextView.setText("1일 (" + formattedDate + " 마감)");


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없는 경우 권한 요청 다이얼로그를 표시
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            // 이미 권한이 있는 경우에는 기능을 실행
            // 이미지 선택 기능 실행 등
        }




        Intent intent = getIntent();
        if (intent != null) {
            userName= intent.getStringExtra("receivedID");
            Log.d(TAG, "Received ID: " + userName);
        }
        else {
            Log.e(TAG, "No data ");
        }

        postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });


        View.OnClickListener myClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();

            }
        };


        deadlineTextView.setOnClickListener(myClickListener);
        downButtonImageView.setOnClickListener(myClickListener);






        bidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedButton = 0; // 구매 입찰 버튼을 선택한 상태로 변경
                updateButtonBackgrounds();
            }
        });


        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedButton = 1; // 즉시 판매 버튼을 선택한 상태로 변경
                updateButtonBackgrounds();
            }
        });





        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        //EditPostActivity 에서 로케이션 버튼

        locationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditPostActivity.this, GoggleMabView.class);
                // EditPostActivity로 선택한 주소 전달
                locationResultLauncher.launch(intent);
            }
        });


        //  텍스트 변경을 감지하는 TextWatcher
        priceEditText.addTextChangedListener(new TextWatcher(){ // 즉시구매가
            private boolean isDeleting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                isDeleting = count > after;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                priceEditText.removeTextChangedListener(this);

                String originalString = s.toString();

                // 숫자와 쉼표만 남기고 모든 문자 제거
                String cleanString = originalString.replaceAll("[₩,]", "");

                if (cleanString.isEmpty() && originalString.contains("₩")) {
                    // '₩' 문자만 남아있는 경우, 텍스트를 비우고 힌트를 표시
                    priceEditText.setText("");
                    priceEditText.setHint("₩ 금액을 입력하세요");
                    priceEditText.setTypeface(null, Typeface.NORMAL);
                } else {
                    // 숫자를 쉼표 형식으로 포맷팅
                    DecimalFormat formatter = new DecimalFormat("#,###");
                    try {
                        String formattedString = formatter.format(Long.parseLong(cleanString));

                        // 입력된 텍스트를 bold체로 설정하고 ₩를 붙여서 표시
                        formattedString = "₩" + formattedString;
                        priceEditText.setTypeface(null, Typeface.BOLD);

                        // 결과를 EditText에 설정
                        priceEditText.setText(formattedString);
                        priceEditText.setSelection(priceEditText.getText().length());
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                    }
                }

                checkAndShowWarning();

                priceEditText.addTextChangedListener(this);
            }

        });

         bidPriceEditText.addTextChangedListener(new TextWatcher() { //최소 입찰가
            private boolean isbidDeleting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                isbidDeleting = count > after;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

             @Override
             public void afterTextChanged(Editable s) {
                 bidPriceEditText.removeTextChangedListener(this);

                 String originalString = s.toString();

                 // 숫자와 쉼표만 남기고 모든 문자 제거
                 String cleanString = originalString.replaceAll("[₩,]", "");

                 if (cleanString.isEmpty() && originalString.contains("₩")) {
                     // '₩' 문자만 남아있는 경우, 텍스트를 비우고 힌트를 표시
                     bidPriceEditText.setText("");
                     bidPriceEditText.setHint("₩ 최저 입찰가를 입력하세요");
                     bidPriceEditText.setTypeface(null, Typeface.NORMAL);
                 } else {
                     // 숫자를 쉼표 형식으로 포맷팅
                     DecimalFormat formatter = new DecimalFormat("#,###");
                     try {
                         String formattedString = formatter.format(Long.parseLong(cleanString));

                         // 입력된 텍스트를 bold체로 설정하고 ₩를 붙여서 표시
                         formattedString = "₩" + formattedString;
                         bidPriceEditText.setTypeface(null, Typeface.BOLD);

                         // 결과를 EditText에 설정
                         bidPriceEditText.setText(formattedString);
                         bidPriceEditText.setSelection(bidPriceEditText.getText().length());
                     } catch (NumberFormatException nfe) {
                         nfe.printStackTrace();
                     }
                 }

                 checkAndShowWarning();

                 bidPriceEditText.addTextChangedListener(this);
             }

         });


        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 이미 처리 중인 경우 더 이상 처리하지 않음
                String imageCount = imageCountTextView.getText().toString();
                // 나머지 입력 데이터 가져오기
                String title = titleEditText.getText().toString();
                String location = locationTextView.getText().toString();
                String sendTime = dateTime;
                String price = priceEditText.getText().toString();
                String description = descriptionEditText.getText().toString();
                String bidPrice = bidPriceEditText.getText().toString();
                String deadlineText = deadlineTextView.getText().toString();
                String deadlineDate = "";

                String[] parts = deadlineText.split("일");
                if (parts.length > 0) {
                    deadlineDate = parts[0];
                }

                long immediatePrice = getNumberFromString(priceEditText.getText().toString());
                long longBidPrice = getNumberFromString(bidPriceEditText.getText().toString());

                if (title.isEmpty()) {
                    Toast.makeText(EditPostActivity.this, "제목을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (price.isEmpty()) {
                    Toast.makeText(EditPostActivity.this, "가격을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (description.isEmpty()) {
                    Toast.makeText(EditPostActivity.this, "설명을 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (location.isEmpty()) {
                    Toast.makeText(EditPostActivity.this, "위치를 고르세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(selectedButton == 0 && bidPrice.isEmpty()) {
                    Toast.makeText(EditPostActivity.this, "최저입찰가를 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (immediatePrice < longBidPrice) {
                    Toast.makeText(EditPostActivity.this, "즉시 입찰가가 최소 입찰가보다 같거나 낮습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(imageCount.equals("0/10")) {
                    Toast.makeText(EditPostActivity.this, "이미지를 한장 이상 선택하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String saleType = selectedButton == 0 ? "bid" : "immediate";

                // 선택된 모든 이미지를 서버에 업로드
                uploadImagesToServer(selectedImageUris, userName);

                sendDataToServer(selectedImageUris, userName, title, location, sendTime, price, description, latitude, longitude, saleType, bidPrice, deadlineDate);
            }
        });





    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    // 선택된 이미지 수가 10장 이하인 경우
                    if (data.getClipData().getItemCount() <= MAX_IMAGE_COUNT - selectedImageUris.size()) {
                        for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            selectedImageUris.add(imageUri);
                        }
                        // 이미지가 추가될 때마다 어댑터를 업데이트하여 리사이클러뷰에 반영
                        updateImageRecyclerView();
                    } else {
                        Toast.makeText(this, "최대 " + MAX_IMAGE_COUNT + "장의 사진을 선택할 수 있습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    // 선택된 이미지 수가 10장 이하인 경우에만 처리
                    if (selectedImageUris.size() < MAX_IMAGE_COUNT) {
                        selectedImageUris.add(imageUri);
                        // 이미지가 추가될 때마다 어댑터를 업데이트하여 리사이클러뷰에 반영
                        updateImageRecyclerView();
                    } else {
                        Toast.makeText(this, "최대 " + MAX_IMAGE_COUNT + "장의 사진을 선택할 수 있습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private final ActivityResultLauncher<Intent> locationResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // EditPostActivity에서 전달한 데이터를 받기
                    Intent data = result.getData();
                    if (data != null) {
                        String fullAddress = data.getStringExtra("FULLADDRESS");
                        String dong = data.getStringExtra("DONG");
                        latitude = data.getDoubleExtra("LATITUDE", 0.0);
                        longitude = data.getDoubleExtra("LONGITUDE", 0.0);
                        Log.d(TAG, "latitude" + latitude);
                        Log.d(TAG, "longitude" + longitude);
                        Log.d(TAG, "fullAddress" + fullAddress);
                        // 쉼표가 있는 경우 쉼표를 제거하고 텍스트뷰에 설정
                        if (dong.contains(",")) {
                            dong = dong.replace(",", "");
                        }
                        // 선택한 주소를 텍스트뷰에 설정
                        locationTextView.setText(dong);
                    }
                }
            });



    private void sendDataToServer(List<Uri> imageUris, String userName, String title, String location, String sendTime, String price, String description
    ,double latitude, double longitude, String saleType, String bidPrice, String deadLineDate) {
        // 이미지 파일 이름 목록 생성
        Map<String, String> imageNames = new LinkedHashMap<>();


        for (int i = 0; i < imageUris.size(); i++) {
            // 각 이미지에 대한 파일 이름 생성 (순서대로)
            String fileName = generateImageFileName(userName, i + 1); // 순서를 1부터 시작하도록 변경
            Log.d(TAG, "fileName" + fileName);
            imageNames.put("image" + (i + 1), fileName); // 순서대로 맵에 추가
        }


        // Retrofit을 사용하여 서버에 데이터 전송
        Retrofit retrofit = RetrofitClient.getClient(baseUri);
        ApiService apiService = retrofit.create(ApiService.class);
        Log.d(TAG, "imageNames" + imageNames);
        Log.d(TAG, "이떄의 latitude" + latitude);

        // 이미지 파일 이름과 다른 데이터를 함께 서버로 전송
        Call<Void> call = apiService.sendPost(
                imageNames,
                title,
                location,
                sendTime,
                price,
                userName,
                description,
                latitude,
                longitude,
                saleType,
                bidPrice,
                deadLineDate
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {


                    // 처리가 완료되면 버튼을 다시 활성화
                    completeButton.setEnabled(false);
                } else {
                    // 데이터 전송 실패
                    Log.e(TAG, "Failed to send data to server");
                    completeButton.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // 통신 실패
                Log.e(TAG, "Communication failure: " + t.getMessage());
                completeButton.setEnabled(true);
            }
        });
    }


    private void showBottomSheetDialog() {
        editPostDialog = new BottomSheetDialog(EditPostActivity.this);
        View contentView = getLayoutInflater().inflate(R.layout.deadlinebottomdialog, null);
        editPostDialog.setContentView(contentView);

        ImageButton closeButton = contentView.findViewById(R.id.btnClose);
        Button btnOneDay = contentView.findViewById(R.id.btnOneDay);
        Button btnThreeDays = contentView.findViewById(R.id.btnThreeDays);
        Button btnSevenDays = contentView.findViewById(R.id.btnSevenDays);
        Button btnOneMonth = contentView.findViewById(R.id.btnOneMonth);
        Button btnTwoMonth = contentView.findViewById(R.id.btnTwoMonth);
        Button btnThreeMonth = contentView.findViewById(R.id.btnThreeMonth);

        btnOneDay.setOnClickListener(new View.OnClickListener() { // 1일
            @Override
            public void onClick(View v) {
                setDeadlineText(1);
            }
        });

        // 3일 버튼 클릭 이벤트 !
        btnThreeDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDeadlineText(3);
                editPostDialog.dismiss();
            }
        });

        // 7일 버튼 클릭 이벤트!
        btnSevenDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDeadlineText(7);
                editPostDialog.dismiss();
            }
        });

        btnOneMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDeadlineText(30);
                editPostDialog.dismiss();
            }
        });

        btnTwoMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDeadlineText(60);
                editPostDialog.dismiss();
            }
        });

        btnThreeMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDeadlineText(90);
                editPostDialog.dismiss();
            }
        });



        closeButton.setOnClickListener(new View.OnClickListener() { // 닫기 !
            @Override
            public void onClick(View v) {
                editPostDialog.dismiss(); // 다이얼로그 닫기 !
            }
        });



        editPostDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);

                    // 최대 높이 설정
                    behavior.setPeekHeight(contentView.getHeight());
                }
            }
        });

        editPostDialog.show();
    }

    private void setDeadlineText(int daysToAdd) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd); // 주어진 일수만큼 날짜를 더함

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String formattedDate = dateFormat.format(calendar.getTime());

        // deadlineTextView에 날짜 정보 설정
        deadlineTextView.setText(daysToAdd + "일 (" + formattedDate + " 마감)");
    }


    private void updateButtonBackgrounds() {
        switch (selectedButton) {
            case 0: // 판매 입찰 선택

                bidPriceEditText.setVisibility(View.VISIBLE);
                bidPriceTextView.setVisibility(View.VISIBLE);
                priceTextView.setText("즉시판매가");
                editPostTextView.setText("판매 입찰");
                deadline.setVisibility(View.VISIBLE);
                deadlineTextView.setVisibility(View.VISIBLE);
                downButtonImageView.setVisibility(View.VISIBLE);
                break;
            case 1: // 즉시 판매 선택

                bidPriceEditText.setVisibility(View.GONE);
                bidPriceTextView.setVisibility(View.GONE);
                priceTextView.setText("가격");
                editPostTextView.setText("즉시 판매");
                deadline.setVisibility(View.GONE);
                deadlineTextView.setVisibility(View.GONE);
                downButtonImageView.setVisibility(View.GONE);
                break;
        }
    }


    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 여러 이미지 선택 가능하도록 설정
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

    private void checkAndShowWarning() {
        // 즉시구매가와 최소 입찰가를 숫자 형태로 변환
        long immediatePrice = getNumberFromString(priceEditText.getText().toString());
        long bidPrice = getNumberFromString(bidPriceEditText.getText().toString());

        // 즉시구매가가 최소 입찰가보다 낮거나 같은 경우 경고 메시지 표시
        if (immediatePrice <= bidPrice) {
            warningLOW.setVisibility(View.VISIBLE);
        } else {
            warningLOW.setVisibility(View.GONE);
        }
    }




    // 문자열에서 숫자만 추출하여
    private long getNumberFromString(String str) {
        str = str.replaceAll("[^0-9]", ""); // 숫자가 아닌 모든 문자 제거
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return 0; // 변환 실패 시 0 반환
        }
    }


    private void updateImageRecyclerView() {
        // ImageAdapter 객체를 생성할 때 세 번째 매개변수로 OnImageDeleteListener를 전달
        ImageAdapter adapter = new ImageAdapter(this, selectedImageUris, new ImageAdapter.OnImageDeleteListener() {
            @Override
            public void onImageDeleted(int position) {
                // 이미지가 삭제되었을 때 수행할 작업을 정의
                int totalCount = selectedImageUris.size();
                imageCountTextView.setText(totalCount + "/10");
            }
        });
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageRecyclerView.setAdapter(adapter);

        // 이미지가 추가될 때마다 totalCount를 증가시키고, imageCountTextView를 업데이트
        int totalCount = selectedImageUris.size();
        imageCountTextView.setText(totalCount + "/10");

        imageRecyclerView.scrollToPosition(totalCount - 1);
    }


    private String generateImageFileName(String userId, int index) {
        String title = titleEditText.getText().toString();
        String location = locationTextView.getText().toString();

        // 사용자 ID, 인덱스 및 시간 정보를 결합하여 파일 이름을 생성
        return "postImage_" + userId + "_" + title + "_" + location + "_" + index + ".jpg";
    }



    @Override
    public void onImageDeleted(int position) {
        int totalCount = selectedImageUris.size();

        totalCount--;
        imageCountTextView.setText(totalCount + "/10");
    }

    private void uploadImagesToServer(List<Uri> imageUris, String userName) {
        // 선택된 이미지들을 서버에 업로드
        for (int i = 0; i < imageUris.size(); i++) {
            Uri imageUri = imageUris.get(i);
            // 각 이미지에 대한 파일 이름을 생성할 때 인덱스 값을 전달합니다.
            uploadImageToServer(imageUri, userName, i + 1);
        }
    }

    private void uploadImageToServer(Uri imageUri, String userName, int index) {
        // Glide를 사용하여 이미지를 로드하고 서버로 업로드합니다.
        Glide.with(this)
                .asBitmap()
                .load(imageUri)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // 이미지를 비트맵으로 성공적으로 로드한 후에 여기에서 서버로 업로드할 수 있습니다.
                        uploadBitmapToServer(resource, userName, index);
                    }
                });
    }

    // 비트맵을 서버로 업로드하는 메서드
    private void uploadBitmapToServer(Bitmap bitmap, String userName, int index) {
        // 비트맵을 파일로 변환
        File file = bitmapToFile(bitmap);
        String fileName = generateImageFileName(userName, index);

        // 파일이 올바르게 생성되었는지 확인
        if (file != null) {
            // 이미지 파일을 RequestBody로 변환
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);

            // RequestBody를 MultipartBody.Part로 변환
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", fileName, requestFile);

            // userName을 RequestBody로 변환
            RequestBody userNameRequest = RequestBody.create(MediaType.parse("text/plain"), userName);

            // Retrofit 객체 생성
            Retrofit retrofit = RetrofitClient.getClient(baseUri);

            progressBar.setVisibility(View.VISIBLE);

            // 서비스 인터페이스 생성
            ApiService apiService = retrofit.create(ApiService.class);

            // 서버로 이미지 업로드 요청 보내기
            Call<ResponseBody> call = apiService.uploadPostImage(body, userNameRequest);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    // 성공적으로 업로드된 경우
                    if (response.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);

                        finish();
                        Log.d(TAG, "Image uploaded successfully");
                    } else {
                        // 업로드 실패
                        Log.e(TAG, "Failed to upload image: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // 통신 실패
                    Log.e(TAG, "Communication failure: " + t.getMessage());
                }
            });
        }
    }

    // 비트맵을 파일로 변환하는 메서드
    private File bitmapToFile(Bitmap bitmap) {
        try {
            // 비트맵을 파일로 저장하기 위해 임시 파일을 생성
            File tempFile = File.createTempFile("temp_image", ".jpg", getCacheDir());

            // 파일 출력 스트림을 생성하여 비트맵을 파일에 씀
            FileOutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }






}
