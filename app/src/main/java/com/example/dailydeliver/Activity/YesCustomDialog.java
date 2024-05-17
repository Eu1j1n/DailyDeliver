package com.example.dailydeliver.Activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.dailydeliver.R;

public class YesCustomDialog extends Dialog {

    private String message;
    private View.OnClickListener yesClickListener;


    public YesCustomDialog(@NonNull Context context, String message,
                        View.OnClickListener yesClickListener,
                        View.OnClickListener noClickListener) {
        super(context);
        this.message = message;
        this.yesClickListener = yesClickListener;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yes_custom_dialog);

        // 다이얼로그 메시지 설정
        TextView confirmTextView = findViewById(R.id.confirmTextView);
        confirmTextView.setText(message);

        // 확인 버튼 설정
        Button yesButton = findViewById(R.id.yesButton);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (yesClickListener != null) {
                    yesClickListener.onClick(v);
                }
                dismiss(); // 다이얼로그 닫기
            }
        });

        // 취소 버튼 설정


    }
}