package com.example.dailydeliver.profile;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.dailydeliver.Activity.LoginActivity;
import com.example.dailydeliver.ApiService;
import com.example.dailydeliver.Fragment.CustomDialog;
import com.example.dailydeliver.ImageResponse;
import com.example.dailydeliver.R;
import com.example.dailydeliver.RetrofitClient;
import com.kakao.sdk.user.UserApiClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileFragment extends Fragment implements RechargeCreditDialogFragment.CreditUpdateListener {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int REQUEST_CODE = 1;

    private String receiveID;
    private Uri selectedImageUri;
    private Bitmap bitmap;
    private CircleImageView profile;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    String baseUri = "http://43.201.32.122";
    static String TAG = "프로필 액티비티 프래그먼트";
    private String loginType = ""; // 로그인 타입 변수 추가

    private TextView remainCredit;


    public UserProfileFragment() {
// Required empty public constructor
    }


    public static UserProfileFragment newInstance(String loginType, String receivedID, String kakaoNickName, String profileImageUrl) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putString("loginType", loginType);
        args.putString("아이디 값", receivedID);
        args.putString("nickName", kakaoNickName);
        args.putString("profileImageUrl", profileImageUrl);
        Log.d(TAG, "loginType " + loginType);
        Log.d(TAG, "nickname" + kakaoNickName);
        Log.d(TAG, "profileImageUrl" + profileImageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void uploadProfileImage(Uri imageUri, String receiveID, View view) {
        if (imageUri != null) {
            ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
            Bundle args = getArguments();
            CircleImageView profileImage = view.findViewById(R.id.profileImage);
            receiveID = args.getString("아이디 값");

            // 이미지를 임시 파일로 저장
            File imageFile = saveImageToTempFile(imageUri);

            String timestamp = String.valueOf(System.currentTimeMillis());
            String imageFileName = receiveID + "_" + timestamp + ".jpg";

            Log.d(TAG, "imageFileName" + imageFileName);

            Log.d(TAG, "receiveID 값" + receiveID);

            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("uploaded_file", imageFileName, requestBody);
            RequestBody userId = RequestBody.create(MediaType.parse("text/plain"), receiveID);

            Call<ResponseBody> call = apiService.editImage(imagePart, userId);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        // 이미지 업로드 성공 시 동작
                        Glide.with(getActivity())
                                .clear(profileImage);
                        Glide.with(getActivity())
                                .load(imageUri)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(profileImage);

                        Log.d(TAG, "이미지 업로드 성공 !");
                    } else {
                        // 이미지 업로드 실패 시 동작
                        Log.d(TAG, "이미지 업로드 실패 !");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "에러 = " + t.getMessage());
                    Toast.makeText(getActivity(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private File saveImageToTempFile(Uri imageUri) {
// 이미지를 임시 파일로 저장
        File tempFile = null;
        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
            tempFile = File.createTempFile("temp_image", ".jpg", getActivity().getCacheDir());
            OutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);

        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }

        return null;
    }

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        Bundle args = getArguments();

        if (args != null) {
            loginType = args.getString("loginType");
            Log.d(TAG, "진짜 로그인타입 값" + loginType);
            receiveID = args.getString("아이디 값"); // 전역 변수에 할당
            String profileImageUrl = args.getString("profileImageUrl");
            String imagePathFromServer = "";

        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }

        CircleImageView profileImage = view.findViewById(R.id.profileImage);
        TextView nickName = view.findViewById(R.id.nickname);
        Button kakaologoutButton = view.findViewById(R.id.kakaoLogout);
        Button logoutButton = view.findViewById(R.id.Logout);
        Button rechargeCredit = view.findViewById(R.id.rechargeCredit);
        remainCredit = view.findViewById(R.id.remainingCredit);


        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        selectedImageUri = intent.getData();
                        if (selectedImageUri != null) {
                            // 앨범에서 선택한 이미지인 경우
                            // 이미지를 서버에 업로드
                            CustomDialog dialog = new CustomDialog(getActivity(),
                                    "프로필 사진을 변경하시겠습니까?",
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            // '예' 버튼이 클릭된 경우
                                            uploadProfileImage(selectedImageUri, receiveID, view);

                                        }
                                    },
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            // '아니오' 버튼이 클릭된 경우

                                        }
                                    });
                            dialog.show();
                        } else {
                            // 카메라로 찍은 이미지인 경우
                            // 이미지를 파일로 저장하고 해당 파일의 Uri를 가져옵니다.
                            File imageFile = saveImageFromCamera(intent);
                            if (imageFile != null) {
                                selectedImageUri = Uri.fromFile(imageFile);
                                // 파일로 저장한 이미지를 서버에 업로드하기 전에 사용자에게 물어봅니다.
                                CustomDialog dialog = new CustomDialog(getActivity(),
                                        "프로필 사진을 변경하시겠습니까?",
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                // '예' 버튼이 클릭된 경우
                                                uploadProfileImage(selectedImageUri, receiveID, view);

                                            }
                                        },
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                // '아니오' 버튼이 클릭된 경우

                                            }
                                        });
                                dialog.show();
                            }
                        }
                    }
                }
            }
        });



        if (args != null) {

            Log.d(TAG, "진짜 로그인타입 값" + loginType);

            String profileImageUrl = args.getString("profileImageUrl");
            String imagePathFromServer = "";
            if (!"kakao".equals(loginType)) {
                ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
                Call<ImageResponse> call = apiService.getImageFileName(receiveID);
                call.enqueue(new Callback<ImageResponse>() {
                    @Override
                    public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                        if (response.isSuccessful()) {
                            ImageResponse imageResponse = response.body();
                            String profileImageFileName = imageResponse.getImagePath();
                            Log.d(TAG, "onResponse: " + profileImageFileName);
                            String imagePathFromServer = profileImageFileName;
                            Log.d(TAG, "이때의 imagePathFromServer 값" + imagePathFromServer);
                            Glide.with(getActivity())
                                    .load(imagePathFromServer)
                                    .error(R.drawable.profile)
                                    .into(profileImage);
                        }
                    }

                    @Override
                    public void onFailure(Call<ImageResponse> call, Throwable t) {
                        Log.e(TAG, "에러 = " + t.getMessage());
                        Toast.makeText(getActivity(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            int defaultImageResource = R.drawable.profile;

            if ("kakao".equals(loginType)) {
                Log.d(TAG, "카카오 로그인 들어옴?");
                kakaologoutButton.setVisibility(View.VISIBLE);
                logoutButton.setVisibility(View.GONE);
                Glide.with(this)
                        .load(profileImageUrl)
                        .into(profileImage);
                String nickname = args.getString("아이디 값");
                Log.d(TAG, "카카오 프로필" + profileImageUrl);
                Log.d(TAG, "nickname 값" + nickname);
                nickName.setText(nickname);
            } else {
                Log.d(TAG, "일반 로그인일 경우");
                kakaologoutButton.setVisibility(View.GONE);
                logoutButton.setVisibility(View.VISIBLE);
                Log.d(TAG, "일반로그인 imagePath" + imagePathFromServer);
                Glide.with(getActivity())
                        .load(imagePathFromServer)
                        .error(R.drawable.profile)
                        .into(profileImage);
                String nickname = receiveID;
                nickName.setText(nickname);
            }
        }


        fetchCreditFromServer(receiveID, remainCredit);


        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("kakao".equals(loginType)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("프로필 사진 변경 불가")
                            .setMessage("카카오 계정으로 로그인한 경우 프로필을 변경할 수 없습니다.")
                            .setPositiveButton("확인", null)
                            .show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("프로필 사진 등록")
                            .setItems(new String[]{"카메라로 촬영", "앨범에서 선택", "기본 이미지로 변경"}, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            // 카메라로 촬영하는 경우
                                            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                                            } else {
                                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                                activityResultLauncher.launch(cameraIntent);
                                            }
                                            break;
                                        case 1:
                                            // 앨범에서 선택하는 경우
                                            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
                                            }
                                            Intent intent = new Intent(Intent.ACTION_PICK);
                                            intent.setType("image/*");
                                            intent.setAction(Intent.ACTION_PICK);
                                            activityResultLauncher.launch(intent);
                                            break;
                                        case 2:
                                            // 기본 이미지로 변경하는 경우
                                            // 기본 이미지로 변경할지 다시 한 번 확인하는 다이얼로그 표시
                                            AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(getActivity());
                                            confirmationDialog.setTitle("기본 이미지로 변경")
                                                    .setMessage("기본 이미지로 변경하시겠습니까?")
                                                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            // 기본 이미지로 변경하는 API 호출 및 이미지 설정
                                                            ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
                                                            Call<Void> call = apiService.updateBasicImage(receiveID);
                                                            call.enqueue(new Callback<Void>() {
                                                                @Override
                                                                public void onResponse(Call<Void> call, Response<Void> response) {
                                                                    if (response.isSuccessful()) {
                                                                        // 이미지 업데이트 성공
                                                                        selectedImageUri = null;
                                                                        profileImage.setImageResource(R.drawable.profile);
                                                                        Toast.makeText(getActivity(), "프로필 이미지가 기본 이미지로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                                                    } else {
                                                                        // 이미지 업데이트 실패
                                                                        Toast.makeText(getActivity(), "프로필 이미지 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call<Void> call, Throwable t) {
                                                                    // 네트워크 오류 발생
                                                                    Toast.makeText(getActivity(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    })
                                                    .setNegativeButton("아니오", null)
                                                    .show();
                                            break;
                                    }
                                }
                            });
                    builder.show();
                }
            }
        });


        kakaologoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
                    @Override
                    public Unit invoke(Throwable throwable) {
                        if (throwable == null) {
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        } else {
                            Log.e(TAG, "로그아웃 실패: " + throwable.getMessage());
                        }
                        return null;
                    }
                });
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        rechargeCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String receiveID = getArguments().getString("아이디 값");
                Log.d(TAG, "onClick: 여긴" + receiveID);


                RechargeCreditDialogFragment dialogFragment = RechargeCreditDialogFragment.newInstance(receiveID);
                dialogFragment.setCreditUpdateListener(UserProfileFragment.this);
                dialogFragment.show(getFragmentManager(), "recharge_dialog");

            }
        });


        return view;
    }


    private File saveImageFromCamera(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            // 카메라로부터 받은 이미지를 비트맵으로 변환합니다.
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            // 비트맵을 파일로 저장합니다.
            return saveBitmapToFile(imageBitmap);
        }
        return null;
    }


    private File saveBitmapToFile(Bitmap bitmap) {
// 외부 저장소에 저장할 파일명 생성
        String filename = "image_" + System.currentTimeMillis() + ".jpg";

// 외부 저장소에 파일을 저장하기 위한 File 객체 생성
        File externalFilesDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(externalFilesDir, filename);

        try {
            // 파일 출력 스트림 생성
            FileOutputStream outputStream = new FileOutputStream(imageFile);

            // Bitmap을 JPEG 형식으로 압축하여 파일에 저장
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            // 파일 출력 스트림 닫기
            outputStream.close();

            // 저장된 파일의 Uri를 반환
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void fetchCreditFromServer(String receiveID, TextView remainCredit) {
        // Retrofit을 사용하여 서버에 요청을 보내 크레딧 값을 가져오는 메서드
        ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
        Call<CreditResponse> call = apiService.getCredit(receiveID);

        Log.d(TAG, "receiveID1234" + receiveID);
        call.enqueue(new Callback<CreditResponse>() {
            @Override
            public void onResponse(Call<CreditResponse> call, Response<CreditResponse> response) {
                if (response.isSuccessful()) {
                    CreditResponse creditResponse = response.body();
                    Log.d(TAG, "onResponse: ss" + creditResponse.getCredit());
                    if (creditResponse != null) {

                        remainCredit.setText(String.valueOf(creditResponse.getCredit()));
                    } else {
                        // 서버 응답이 null인 경우 처리
                        Log.e(TAG, "서버 응답이 null입니다.");
                    }
                } else {
                    // 서버 응답이 실패인 경우 처리
                    Log.e(TAG, "서버 응답이 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<CreditResponse> call, Throwable t) {
                // 통신 실패 시 처리
                Log.e(TAG, "통신 실패123: " + t.getMessage());

            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 카메라 권한이 부여된 경우
            } else {
                // 카메라 권한이 거부된 경우
            }
        } else if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 외부 저장소 읽기 권한이 부여된 경우
            } else {
                // 외부 저장소 읽기 권한이 거부된 경우
            }
        }
    }

    @Override
    public void onCreditUpdated(String receiveID) {
        fetchCreditFromServer(receiveID, remainCredit);
    }
}


