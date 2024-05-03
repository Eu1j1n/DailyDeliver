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
import android.graphics.Color;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.Manifest;

import org.checkerframework.checker.nullness.qual.NonNull;


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

private TextInputEditText editPassword, checkPassword, accountNumber, name, id,
        remainAddress, phoneNumberEditText,  confirmPhoneNumberEditText;

private EditText address;
private TextView noMatchPasswordMessage, matchPasswordMessage ,pwcheckTextView;
private Button registerButton, checkIdButton, postCodeButton, phoneNumberCheckButton,
        confirmPhoneNumberButton;

private ImageButton backButton;



String regex = "^[a-zA-Z0-9]+$";

private Spinner bankSpinner;



private  static  final int SEARCH_ADDRESS_ACTIVITY = 1002;

private TextInputLayout confirmPhoneNumberLayout;

String baseUri = "http://43.201.32.122/";

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_join);

    findViewBYID();


    coincidePasswordCheck();

    setBankSpinner();

    idChangeCheck();


    phoneNumberCheckButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // 전화번호 EditText에서 전화번호를 가져옵니다.
            String phoneNumber = phoneNumberEditText.getText().toString().trim();
            String koreaPhoneNumber = "+82" + phoneNumber;

            // 전화번호가 올바른지 확인합니다.
            if (isValidPhoneNumber(phoneNumber)) {
                // 인증 코드를 생성합니다.
                String verificationCode = generateVerificationCode();

                // FCM을 이용하여 인증 코드를 전송합니다.
                sendVerificationCode(koreaPhoneNumber, verificationCode);
            } else {
                Toast.makeText(JoinActivity.this, "유효하지 않은 전화번호입니다.", Toast.LENGTH_SHORT).show();
            }
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
            String receivePhoneNumber = phoneNumberEditText.getText().toString();
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

                Toast.makeText(JoinActivity.this, "비밀번호: 8~16자의 영문 대/소문자, 숫자, 특수문자를\n" +
                        "                 사용해 주세요.", Toast.LENGTH_SHORT).show();
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
            String hashPassword = HashPassword.sha256(receivePassword);


            ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
            Log.d(TAG, "여기들어옴?");

            // Retrofit을 사용하여 회원가입 정보 전송
            Call<ResponseBody> call = apiService.registerUser(
                    receiveName, hashPassword, receiveAddress,
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

                                    Intent intent = new Intent(JoinActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
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


    checkIdButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String receiveID = id.getText().toString();
            Log.d(TAG, "receiveID의 값: " + receiveID);

            // ID가 영문자와 숫자의 조합이며, 13자 이하인 경우에만 서버 요청을 진행
            if (isValidID(receiveID) && receiveID.length() <= 13) {
                ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
                Log.d(TAG, "중복확인 apiService: " + apiService);

                // Retrofit을 사용하여 ID 중복 확인 요청 전송
                Call<ResponseBody> call = apiService.checkIdAvailability(receiveID);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String responseBody = response.body().string();
                                if ("success".equals(responseBody)) {
                                    // 성공 처리 (ID 사용 가능)
                                    Toast.makeText(JoinActivity.this, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                                    checkIdButton.setEnabled(false);
                                } else if ("fail".equals(responseBody)) {
                                    // 실패 처리 (ID 중복)
                                    Toast.makeText(JoinActivity.this, "이미 사용 중인 아이디입니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // 기타 응답 처리
                                }
                            } else {
                                // 실패 처리 (서버 응답이 실패인 경우)
                                Toast.makeText(JoinActivity.this, "서버 응답 실패", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            // 예외 처리
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        // 네트워크 오류 등 실패 처리
                        Log.e(TAG, "중복확인 에러: " + t.getMessage());
                        Toast.makeText(JoinActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // 아이디가 조건을 충족하지 않는 경우
                Log.d(TAG, "onClick: 들어옴?");
                Toast.makeText(JoinActivity.this, "영문 숫자 조합 13자 이하로 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        }
    });
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

    private void sendVerificationCode(String phoneNumber, String verificationCode) {

        RemoteMessage message = new RemoteMessage.Builder(phoneNumber)
                .addData("verificationCode", verificationCode)
                .build();

        FirebaseMessaging.getInstance().send(message);

        Log.d(TAG, "메시지 전송 완료");
    }










    protected  void findViewBYID() { // findViewBYID 목록
    editPassword = findViewById(R.id.editPassword);  //비밀번호 입력하는곳
    checkPassword = findViewById(R.id.checkPassword); //다시한번 더비밀번호 확인 하는 뷰
    registerButton = findViewById(R.id.registerButton);
    checkIdButton = findViewById(R.id.checkIdButton);
    pwcheckTextView = findViewById(R.id.pwCheckTextView);
    noMatchPasswordMessage = findViewById(R.id.noMatchPasswordMessage);
    matchPasswordMessage = findViewById(R.id.matchPasswordMessage);
    backButton = findViewById(R.id.backbtn);
    bankSpinner = findViewById(R.id.bankSpinner);
    accountNumber = findViewById(R.id.account);
    name = findViewById(R.id.editUsername);
    id = findViewById(R.id.editID);
    address = findViewById(R.id.editAddress);
    remainAddress = findViewById(R.id.remainaddress);
    phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
    imageView =findViewById(R.id.imageView);
    phoneNumberCheckButton =findViewById(R.id.phoneNumberCheckButton);
    confirmPhoneNumberEditText = findViewById(R.id.confirmPhoneNumberEditText);
    confirmPhoneNumberButton = findViewById(R.id.confirmPhoneNumberButton);
    confirmPhoneNumberLayout = findViewById(R.id.confirmPhoneNumberLayout);




}

    private boolean isValidPhoneNumber(String phoneNumber) {
        // 여기에 전화번호 형식을 확인하는 로직을 작성하세요.
        // 정규식을 사용하거나 다른 방법을 사용하여 전화번호의 유효성을 검사하세요.
        // 이 예제에서는 간단하게 전화번호가 10자리 이상이면 유효하다고 가정합니다.
        return phoneNumber.length() >= 10;
    }

    // 인증 코드를 생성하는 메서드
    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000; // 6자리 숫자 생성
        return String.valueOf(code);
    }


protected void coincidePasswordCheck() {
    TextWatcher passwordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String password = editPassword.getText().toString();
            String checkPwText = checkPassword.getText().toString();

            // 비밀번호 일치 여부 확인
            if (password.equals(checkPwText)) {
                matchPasswordMessage.setVisibility(View.VISIBLE);
                matchPasswordMessage.setText("비밀번호가 일치합니다.");
                noMatchPasswordMessage.setVisibility(View.GONE);
            } else {
                noMatchPasswordMessage.setVisibility(View.VISIBLE);
                noMatchPasswordMessage.setText("비밀번호가 일치하지 않습니다.");
                matchPasswordMessage.setVisibility(View.GONE);
            }

            // 비밀번호 유효성 검사
            if (!isValidPassword(password)) {
                // 비밀번호가 유효하지 않은 경우
                pwcheckTextView.setVisibility(View.VISIBLE);
                pwcheckTextView.setText("비밀번호는 8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해야 합니다.");
                pwcheckTextView.setTextColor(Color.RED); // 빨간색으로 설정
            } else {
                // 비밀번호가 유효한 경우
                pwcheckTextView.setVisibility(View.VISIBLE);
                pwcheckTextView.setText("비밀번호가 유효합니다.");
                pwcheckTextView.setTextColor(Color.BLUE); // 파란색으로 설정
            }

        }
    };

    editPassword.addTextChangedListener(passwordTextWatcher);
    checkPassword.addTextChangedListener(passwordTextWatcher);
}

public boolean isValidID(String id) {
    Log.d(TAG, "isValidID: " + id);
    // 영문자와 숫자의 조합, 최소 한 개의 영문자와 숫자 포함, 13자 이하인지 확인하는 정규 표현식
    String regex = "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$";

    // ID가 정규 표현식에 매칭되는지 확인
    boolean matches = id.matches(regex);

    // ID가 비어있지 않고, 13자 이하인지 확인
    return matches && !id.isEmpty() && id.length() <= 13;
}


private boolean isValidPassword(String password) {
    // 비밀번호: 8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해야 함
    String regex = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]{8,16}$";
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
























}








