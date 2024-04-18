package com.example.dailydeliver.Activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dailydeliver.ApiService;
import com.example.dailydeliver.R;
import com.example.dailydeliver.RetrofitClient;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.Manifest;


public class JoinActivity extends AppCompatActivity {
    String TAG = "회원 가입 액티비티";

    ImageView imageView;

    private Uri cameraImageUri;

    Bitmap bitmap;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private static final int CAMERA_REQUEST_CODE = 101;

    private static final int REQUEST_CODE = 1;


    private ActivityResultLauncher<Intent> activityResultLauncher;



    CircleImageView profile;
    private Uri selectedImageUri;

    private EditText editPassword, checkPassword, accountNumber, name, id, address,
            remainAddress, phoneNumber;
    private TextView noMatchPasswordMessage, matchPasswordMessage;
    private Button registerButton, checkIdButton, postCodeButton;

    private ImageButton backButton;

    String regex = "^[a-zA-Z0-9]+$";

    private Spinner bankSpinner;

    private  static  final int SEARCH_ADDRESS_ACTIVITY = 1002;

    String baseUri = "http://52.79.88.52/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        findViewBYID();


        coincidePasswordCheck();

        setBankSpinner();

        idChangeCheck();


        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == RESULT_OK) {
                            Intent intent = result.getData();

                            selectedImageUri = intent.getData();
                            Log.d(TAG, "카메라나 앨범 사진 등록 selectedImageUri 값: " + selectedImageUri); // 선택한 이미지의 URI를 저장
                            if (selectedImageUri != null) {
                                try {
                                    String imagePath = getRealPathFromURI(selectedImageUri);
                                    Log.d(TAG, "imagepath의 값" + imagePath);
                                    if (imagePath != null) {
                                        File imageFile = new File(imagePath);
                                        //파일을 만들어주는 이유 : 서버에다가 이미지 파일을 올리려고 .
                                        if (imageFile.exists()) {
                                            Glide.with(JoinActivity.this)
                                                    .load(imagePath).into(profile);


                                            Log.d(TAG, "이미지뷰에 이미지가 성공적으로 설정.");
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bundle extras = result.getData().getExtras();
                            if (extras != null) {
                                bitmap = (Bitmap) extras.get("data"); // Bitmap으로 캐스트
                                profile.setImageBitmap(bitmap);
                            }
                        }

                    }
                }
        );



        // profile 클릭 이벤트
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(JoinActivity.this);
                builder.setTitle("프로필 사진 등록")
                        .setItems(new String[]{"카메라로 촬영", "앨범에서 선택", "기본 이미지로 변경"}, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: // 카메라로 촬영
                                        // 권한이 없는 경우 권한 요청
                                        if (ContextCompat.checkSelfPermission(
                                                JoinActivity.this, Manifest.permission.CAMERA)
                                                != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(
                                                    JoinActivity.this,
                                                    new String[]{Manifest.permission.CAMERA},
                                                    CAMERA_PERMISSION_CODE);
                                        } else {
                                            // 권한이 이미 부여된 경우 카메라 앱을 호출합니다.
                                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                            activityResultLauncher.launch(cameraIntent);
                                        }
                                        break;
                                    case 1: // 앨범에서 선택
                                        if (ContextCompat.checkSelfPermission(JoinActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(JoinActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
                                        }

                                        Intent intent = new Intent(Intent.ACTION_PICK);
                                        intent.setType("image/*");
                                        intent.setAction(Intent.ACTION_PICK);
                                        activityResultLauncher.launch(intent);
                                        break;
                                    case 2: //기본이미지
                                        profile.setImageResource(R.drawable.profile);String receiveID = id.getText().toString();

                                        // 아이디 유효성 검사

                                }
                            }
                        });
                builder.show();
            }
        });

        id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            String receiveID = s.toString();

            if (isValidID(receiveID) && receiveID.length() <= 13) {
                checkIdButton.setEnabled(true);
            }else {
                checkIdButton.setEnabled(false);
            }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });




        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DaumAddressActivity.class);

                startActivityForResult(intent, SEARCH_ADDRESS_ACTIVITY);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JoinActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String receiveName = name.getText().toString();
                String selectedBank = bankSpinner.getSelectedItem().toString();

                String receiveID = id.getText().toString();
                String receiveAddress = address.getText().toString();
                String receiveRemainAddress = remainAddress.getText().toString();
                String receivePhoneNumber = phoneNumber.getText().toString();
                String receivePassword = editPassword.getText().toString();
                String receiveCheckPassWord = checkPassword.getText().toString();
                String receiveAccount = accountNumber.getText().toString();
                String fullAddress = receiveAddress + " " + receiveRemainAddress;

                Log.d(TAG, "receiveID 값" + receiveID);

                if (receiveName.isEmpty()) { // 이름이 비어있을 때
                    Toast.makeText(JoinActivity.this, "이름을 입력하세요!", Toast.LENGTH_SHORT).show();

                    return;
                }

                if(receiveID.isEmpty()) {

                    Toast.makeText(JoinActivity.this, "아이디를 입력하세요!", Toast.LENGTH_SHORT).show();

                    return;
                }


                if (!receiveID.matches(regex)) {
                    Log.d(TAG, "isvalidID receiveID: " + receiveID);
                    Toast.makeText(JoinActivity.this, "아이디는 영문과 숫자 조합이어야 합니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onClick: 들어옴?");
                    return; //
                }

                if (!isValidPassword(receiveCheckPassWord)) {

                    Toast.makeText(JoinActivity.this, "비밀번호는 대문자 하나, 영문, 특수문자, 숫자 포함 9자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!receivePassword.equals(receiveCheckPassWord)) {

                    Toast.makeText(JoinActivity.this, "비밀번호 확인이 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (receiveAddress.isEmpty()) {

                    Toast.makeText(JoinActivity.this, "주소를 입력하세요", Toast.LENGTH_SHORT).show();

                    return;
                }

                if (receivePhoneNumber.isEmpty()) {
                    Toast.makeText(JoinActivity.this, "핸드폰 번호를 입력하세요", Toast.LENGTH_SHORT).show();

                    return;
                }

                if (receiveAccount.isEmpty()) {
                    Toast.makeText(JoinActivity.this, "계좌 번호를 입력하세요", Toast.LENGTH_SHORT).show();

                    return;
                }

                if (checkIdButton.isEnabled()) {
                    Toast.makeText(JoinActivity.this, "아이디 중복확인을 해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }



                receiveAddress = fullAddress;

                if (receiveName.isEmpty() || receiveID.isEmpty() || !isValidID(receiveID) || !isValidPassword(receiveCheckPassWord) ||
                        !receivePassword.equals(receiveCheckPassWord) || receiveAddress.isEmpty() ||
                        receivePhoneNumber.isEmpty() || receiveAccount.isEmpty()) {
                    Toast.makeText(JoinActivity.this, "모든 입력칸을 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
                    // "회원가입" 버튼 비활성화
                    return;
                }


                ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
                Log.d(TAG, "여기들어옴?");

                // Retrofit을 사용하여 회원가입 정보 전송
                Call<ResponseBody> call = apiService.registerUser(
                        receiveName, receivePassword, receiveAddress,
                        receivePhoneNumber, selectedBank, receiveAccount,
                        receiveID);
                Log.d(TAG, "여기들어옴?2");

                call.enqueue(new Callback<ResponseBody>() {

                    @Override

                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {

                            // 성공 처리
                            AlertDialog.Builder builder = new AlertDialog.Builder(JoinActivity.this);
                            builder.setTitle("회원가입 성공")
                                    .setMessage("회원가입이 성공적으로 완료되었습니다.")
                                    .setPositiveButton("확인", (dialog, which) -> {
                                        uploadProfileImage(selectedImageUri, receiveID);

                                        Log.d(TAG, "회원가입을 했을때 . selectedImageUri의 값" + selectedImageUri);
                                        Intent intent = new Intent(JoinActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        dialog.dismiss();
                                    })
                                    .show();
                        } else {
                            // 실패 처리
                            Log.d(TAG, "응답 실패. 응답 코드: " + response.code());
                            Toast.makeText(JoinActivity.this, "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        // 네트워크 오류 등 실패 처리
                        Log.e(TAG,"에러 = "+ t.getMessage());
                        Toast.makeText(JoinActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });





        //  id중복확인버튼
        // id 중복확인 버튼의 클릭 이벤트 처리
        checkIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String receiveID = id.getText().toString();
                Log.d(TAG, "receiveID의 값" + receiveID);

                if (!isValidID(receiveID) || receiveID.length() > 13) {
                    // 아이디가 조건을 충족하지 않는 경우
                    Log.d(TAG, "onClick: 들어옴?");
                    Toast.makeText(JoinActivity.this, "영문 숫자 조합 13자 이하로 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
                Log.d(TAG, "중복확인 apiService" + apiService);

                // Retrofit을 사용하여 ID 중복 확인 요청 전송
                Call<ResponseBody> call = apiService.checkIdAvailability(receiveID);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {
                                String responseBody = response.body().string();
                                if (responseBody.equals("success")) {
                                    // 성공 처리 (ID 사용 가능)
                                    Toast.makeText(JoinActivity.this, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                                    checkIdButton.setEnabled(false);
                                } else if (responseBody.equals("fail")) {
                                    // 실패 처리 (ID 중복)
                                    Toast.makeText(JoinActivity.this, "이미 사용 중인 아이디입니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // 기타 응답 처리
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                // 예외 처리
                            }
                        } else {
                            // 실패 처리 (서버 응답이 실패인 경우)
                            Toast.makeText(JoinActivity.this, "서버 응답 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        // 네트워크 오류 등 실패 처리
                        Log.e(TAG, "중복확인 에러 = " + t.getMessage());
                        Toast.makeText(JoinActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });












    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 부여되면 카메라 앱을 호출합니다.
                startCamera();
            } else {

                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case SEARCH_ADDRESS_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    String data = intent.getExtras().getString("data");
                    if (data != null) {
                        // data의 정보를 각각 우편번호와 실주소로 나누어 EditText에 표시
                        address.setText(data.substring(7));
                    }
                }
                break;

        }
    }





    protected  void setBankSpinner() {
        bankSpinner = findViewById(R.id.bankSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.bank_names, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bankSpinner.setAdapter(adapter);

    }







    protected  void findViewBYID() { // findViewBYID 목록
        editPassword = findViewById(R.id.editPassword);  //비밀번호 입력하는곳
        checkPassword = findViewById(R.id.checkPassword); //다시한번 더비밀번호 확인 하는 뷰
        registerButton = findViewById(R.id.registerButton);
        checkIdButton = findViewById(R.id.checkIdButton);
        noMatchPasswordMessage = findViewById(R.id.noMatchPasswordMessage);
        matchPasswordMessage = findViewById(R.id.matchPasswordMessage);
        backButton = findViewById(R.id.backbtn);
        bankSpinner = findViewById(R.id.bankSpinner);
        accountNumber = findViewById(R.id.account);
        name = findViewById(R.id.editUsername);
        id = findViewById(R.id.editID);
        address = findViewById(R.id.editaddress);
        remainAddress = findViewById(R.id.remainaddress);
        phoneNumber = findViewById(R.id.phoneNumber);
        imageView =findViewById(R.id.imageView);
        profile = findViewById(R.id.profileImage);



    }


    protected  void coincidePasswordCheck() { //비밀번호 가 서로 일치하는지

        TextWatcher passwordTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String password = editPassword.getText().toString();
                String checkPwText = checkPassword.getText().toString();

                if (password.equals(checkPwText)) {
                    matchPasswordMessage.setVisibility(View.VISIBLE);
                    matchPasswordMessage.setText("비밀번호가 일치합니다.");
                    noMatchPasswordMessage.setVisibility(View.GONE);
                } else {
                    noMatchPasswordMessage.setVisibility(View.VISIBLE);
                    noMatchPasswordMessage.setText("비밀번호가 일치하지 않습니다.");
                    matchPasswordMessage.setVisibility(View.GONE);
                }
            }
        };

        editPassword.addTextChangedListener(passwordTextWatcher);
        checkPassword.addTextChangedListener(passwordTextWatcher);

    }

    private boolean isValidID(String receiveID) {
        // 영문자와 숫자의 조합인지 확인하는 정규표현식
        String regex = "^[a-zA-Z0-9]+$";

        return receiveID.matches(regex);
    }

    private boolean isValidPassword(String password) {
        // 대문자 하나, 영문, 특수문자, 숫자 포함 9자 이상인지 확인하는 정규표현식
        String regex = "^(?=.*[A-Z])(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{9,}$";
        return password.matches(regex);
    }

    private void idChangeCheck() {
        id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkIdButton.setEnabled(true);


            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private boolean allFieldsAreFilled() {
        String receiveName = name.getText().toString();
        String receiveID = id.getText().toString();
        String receiveCheckPassWord = checkPassword.getText().toString();
        String receiveAddress = address.getText().toString();
        String receivePassword = editPassword.getText().toString();
        String receiveRemainAddress = remainAddress.getText().toString();
        String receivePhoneNumber = phoneNumber.getText().toString();
        String receiveAccount = accountNumber.getText().toString();

        return !receiveName.isEmpty() && isValidID(receiveID) && isValidPassword(receiveCheckPassWord)
                && receivePassword.equals(receiveCheckPassWord) && !receiveAddress.isEmpty()
                && !receivePhoneNumber.isEmpty() && !receiveAccount.isEmpty();
    }

    private void startCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // 카메라로 찍은 이미지를 저장할 파일을 생성합니다.
            File imageFile = createImageFile();
            Log.d(TAG, "imageFile의 값 " + imageFile);

            if (imageFile != null) {
                cameraImageUri = FileProvider.getUriForFile(this,
                        "com.example.myapp.fileprovider", // FileProvider의 authority를 사용하세요.
                        imageFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                activityResultLauncher.launch(cameraIntent);
            }
        }
    }

    // 이미지 파일을 생성하는 함수
    private File createImageFile() {
        String imageFileName = "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            Log.d(TAG, "imageFile" + imageFile);
            cameraImageUri = Uri.fromFile(imageFile); // 이미지 파일의 Uri를 저장
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }




    private String getRealPathFromURI(Uri contentUri) {   //절대경로로 바꿔주는
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }

    private void uploadProfileImage(Uri imageUri, String receiveID) {
        Log.d(TAG, "uploadProfileImage: 이미지 업로드 시작");

        if (imageUri != null) {
            ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);

            // 이미지 파일을 가져오기
            File imageFile = new File(getRealPathFromURI(imageUri));

            // 회원가입한 ID와 시간 정보를 사용하여 파일 이름 생성
            String timestamp = String.valueOf(System.currentTimeMillis());
            String imageFileName = receiveID + "_" + timestamp + ".jpg";

            // 이미지 파일을 RequestBody로 변환
            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("uploaded_file", imageFileName, requestBody);
            RequestBody userId = RequestBody.create(MediaType.parse("text/plain"), receiveID);

            Log.d(TAG, "userID: " + userId.toString());


            // 이미지 업로드 요청
            Call<ResponseBody> call = apiService.uploadImage(imagePart, userId);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        // 이미지 업로드 성공
                        Log.d(TAG, "이미지 업로드 성공 !");
                        // 이미지 업로드가 성공한 후에 회원가입 처리를 수행하거나 다른 작업을 수행할 수 있습니다.
                    } else {
                        // 이미지 업로드 실패
                        Log.d(TAG, "이미지 업로드 실패 !");
                        // 실패 처리
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // 네트워크 오류 등 실패 처리
                    Log.e(TAG, "에러 = " + t.getMessage());
                    Toast.makeText(JoinActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }











}








