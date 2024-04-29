package com.example.dailydeliver.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.dailydeliver.ApiService;
import com.example.dailydeliver.R;
import com.example.dailydeliver.RetrofitClient;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RechargeCreditDialogFragment extends DialogFragment {

    public void setCreditUpdateListener(CreditUpdateListener listener) {
        this.creditUpdateListener = listener;
    }


    public interface CreditUpdateListener {
        void onCreditUpdated(String receiveID);
    }

    private CheckBox option1CheckBox, option2CheckBox, option3CheckBox, option4CheckBox, option5CheckBox, option6CheckBox;
    private Button kakaoPayButton;

    String TAG = "충전 다이얼로그";

    String baseUri = "http://43.201.32.122/";

    String receivedID; // 아이디 값을 담을 변수
    private CreditUpdateListener creditUpdateListener;

    public static RechargeCreditDialogFragment newInstance(String receiveID) {
        RechargeCreditDialogFragment fragment = new RechargeCreditDialogFragment();
        Bundle args = new Bundle();
        args.putString("receiveID", receiveID);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_recharge_credit_dialog, null);

        // onCreateDialog 내에서는 getArguments()가 null을 반환할 수 있으므로, onCreate에서 getArguments()를 호출하여 변수에 저장합니다.
        Bundle args = getArguments();
        if (args != null) {
            receivedID = args.getString("receiveID");
            Log.d(TAG, "receivedID: " + receivedID);
        }

        // 체크박스 및 버튼 가져오기
        option1CheckBox = view.findViewById(R.id.checkbox_option1);
        option2CheckBox = view.findViewById(R.id.checkbox_option2);
        option3CheckBox = view.findViewById(R.id.checkbox_option3);
        option4CheckBox = view.findViewById(R.id.checkbox_option4);
        option5CheckBox = view.findViewById(R.id.checkbox_option5);
        option6CheckBox = view.findViewById(R.id.checkbox_option6);
        kakaoPayButton = view.findViewById(R.id.kakaoPayButton);

        // 각 체크박스를 클릭할 때 다른 체크박스들의 선택 상태 초기화 및 현재 클릭한 체크박스 선택 상태 설정
        option1CheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllCheckExcept(option1CheckBox);
                // 9900원 결제

            }
        });

        option2CheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllCheckExcept(option2CheckBox);
                // 19900원 결제
            }
        });

        option3CheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllCheckExcept(option3CheckBox);
                // 29900원 결
            }
        });

        option4CheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllCheckExcept(option4CheckBox);
                // 49900원 결제
            }
        });

        option5CheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllCheckExcept(option5CheckBox);
                // 99000원 결
            }
        });

        option6CheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllCheckExcept(option6CheckBox);
                //199,000원 결제
            }
        });



        // 카카오페이 버튼 클릭 시 선택한 옵션에 따라 처리
        kakaoPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int updatedCredit = getPrice();
                // CreditUpdateRequest 객체를 생성하여 Retrofit을 통해 서버에 전송
                CreditUpdateRequest request = new CreditUpdateRequest(receivedID, updatedCredit);
                ApiService apiService = RetrofitClient.getClient(baseUri).create(ApiService.class);
                Call<ResponseBody> call = apiService.updateCredit(request);
                Log.d(TAG, "receivedID" + receivedID);
                Log.d(TAG, "updatedCredit" + updatedCredit);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            if (creditUpdateListener != null) {
                                creditUpdateListener.onCreditUpdated(receivedID);
                            }

                            Log.d(TAG, "크레딧 업데이트 성공");
                        } else {
                            // 크레딧 업데이트 실패
                            Log.e(TAG, "크레딧 업데이트 실패");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        // 네트워크 오류 등으로 요청 실패
                        Log.e(TAG, "크레딧 업데이트 요청 실패", t);
                    }
                });

                // 다이얼로그를 닫기
                dismiss();
            }
        });
        builder.setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 다이얼로그를 닫기
                        dismiss();
                    }
                });

        return builder.create();
    }

    private int getPrice() {
        // 각 체크박스의 가격을 리턴
        if (option1CheckBox.isChecked()) {
            return 9900;
        } else if (option2CheckBox.isChecked()) {
            return 19900;
        } else if (option3CheckBox.isChecked()) {
            return 29900;
        } else if (option4CheckBox.isChecked()) {
            return 49900;
        } else if (option5CheckBox.isChecked()) {
            return 99000;
        } else if (option6CheckBox.isChecked()) {
            return 199000;
        } else {
            return 0; // 가격이 없는 경우
        }
    }

    // 선택된 체크박스를 제외 모든 체크박스의 선택 상태 초기화
    private void clearAllCheckExcept(CheckBox selectedCheckBox) {
        if (selectedCheckBox != option1CheckBox) {
            option1CheckBox.setChecked(false);
        }
        if (selectedCheckBox != option2CheckBox) {
            option2CheckBox.setChecked(false);
        }
        if (selectedCheckBox != option3CheckBox) {
            option3CheckBox.setChecked(false);
        }
        if (selectedCheckBox != option4CheckBox) {
            option4CheckBox.setChecked(false);
        }
        if (selectedCheckBox != option5CheckBox) {
            option5CheckBox.setChecked(false);
        }
        if (selectedCheckBox != option6CheckBox) {
            option6CheckBox.setChecked(false);
        }
    }
}
