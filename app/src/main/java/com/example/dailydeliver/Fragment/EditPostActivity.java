package com.example.dailydeliver.Fragment;

import android.app.Activity;
import android.content.Context;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import com.example.dailydeliver.R;
import com.example.dailydeliver.RetrofitClient;
import com.naver.maps.map.MapView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private ImageView postImageView;
    private RecyclerView imageRecyclerView;
    private EditText titleEditText;
    private EditText priceEditText;
    private EditText descriptionEditText;
    private TextView locationTextView;
    private Button completeButton;

    private TextView imageCountTextView;

    private String TAG = "글쓰는 액티비티";

    private ProgressBar progressBar;

    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;


    private String userName;

    private double latitude;
    private double longitude;



    private String baseUri = "http://43.201.32.122/";

    String ImageUri = "http://43.201.32.122/postImage/";

    private static final int MAX_IMAGE_COUNT = 10;



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
        priceEditText.addTextChangedListener(new TextWatcher() {
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

                // 숫자를 쉼표 형식으로 포맷팅
                DecimalFormat formatter = new DecimalFormat("#,###");
                try {
                    String formattedString = formatter.format(Long.parseLong(cleanString));

                    // 텍스트가 비어있지 않은 경우
                    if (!formattedString.isEmpty()) {
                        // 입력된 텍스트를 bold체로 설정하고 ₩를 붙여서 표시
                        if (!formattedString.startsWith("₩")) {
                            formattedString = "₩" + formattedString;
                        }
                        priceEditText.setTypeface(null, Typeface.BOLD);
                    } else {
                        // 입력된 텍스트가 비어있는 경우
                        // 원래 hint 문구로 복원
                        formattedString = "₩ 금액을 입력하세요";
                        priceEditText.setTypeface(null, Typeface.NORMAL);
                    }

                    // 결과를 EditText에 설정
                    priceEditText.setText(formattedString);
                    priceEditText.setSelection(priceEditText.getText().length());
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }

                priceEditText.addTextChangedListener(this);
            }
        });


        completeButton.setOnClickListener(new View.OnClickListener() {
            private boolean isProcessing = false;

            @Override
            public void onClick(View v) {
                // 이미 처리 중인 경우 더 이상 처리하지 않음

                // 나머지 입력 데이터 가져오기
                String title = titleEditText.getText().toString();
                String location = locationTextView.getText().toString();
                String sendTime = dateTime;
                String price = priceEditText.getText().toString();
                String description = descriptionEditText.getText().toString();

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

                // 선택된 모든 이미지를 서버에 업로드
                uploadImagesToServer(selectedImageUris, userName);

                sendDataToServer(selectedImageUris, userName, title, location, sendTime, price, description, latitude, longitude);
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
    ,double latitude, double longitude) {
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
                longitude
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


    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // 여러 이미지 선택 가능하도록 설정
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
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
