package com.example.dailydeliver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class RechargeCreditDialogFragment extends DialogFragment {

    private boolean option1Selected = false;
    private boolean option2Selected = false;
    private boolean option3Selected = false;
    private boolean option4Selected = false;
    private boolean option5Selected = false;
    private boolean option6Selected = false;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_recharge_credit_dialog, null);

        // 체크박스 가져오기
        CheckBox option1CheckBox = view.findViewById(R.id.checkbox_option1);
        CheckBox option2CheckBox = view.findViewById(R.id.checkbox_option2);
        CheckBox option3CheckBox = view.findViewById(R.id.checkbox_option3);
        CheckBox option4CheckBox = view.findViewById(R.id.checkbox_option4);
        CheckBox option5CheckBox = view.findViewById(R.id.checkbox_option5);
        CheckBox option6CheckBox = view.findViewById(R.id.checkbox_option6);
        Button kakaoPayButton = view.findViewById(R.id.kakaoPayButton);

        // 각 체크박스를 클릭할 때 선택 상태 저장
        option1CheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                option1Selected = option1CheckBox.isChecked();
            }
        });

        option2CheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                option2Selected = option2CheckBox.isChecked();
            }
        });

        option3CheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                option3Selected = option3CheckBox.isChecked();
            }
        });

        option4CheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                option4Selected = option4CheckBox.isChecked();
            }
        });

        option5CheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                option5Selected = option5CheckBox.isChecked();
            }
        });

        option6CheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                option6Selected = option6CheckBox.isChecked();
            }
        });

        // 카카오페이 버튼 클릭 시 선택한 옵션에 따라 처리
        kakaoPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (option1Selected) {
                    // option1에 해당하는 처리
                }
                if (option2Selected) {
                    // option2에 해당하는 처리
                }
                if (option3Selected) {
                    // option3에 해당하는 처리
                }
                if (option4Selected) {
                    // option4에 해당하는 처리
                }
                if (option5Selected) {
                    // option5에 해당하는 처리
                }
                if (option6Selected) {
                    // option6에 해당하는 처리
                }

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
}
