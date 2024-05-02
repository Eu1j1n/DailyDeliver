package com.example.dailydeliver.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.dailydeliver.R;

public class CustomDialog extends Dialog {

    private String message;
    private View.OnClickListener yesClickListener;
    private View.OnClickListener noClickListener;

    public CustomDialog(@NonNull Context context, String message,
                        View.OnClickListener yesClickListener,
                        View.OnClickListener noClickListener) {
        super(context);
        this.message = message;
        this.yesClickListener = yesClickListener;
        this.noClickListener = noClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customdialog);

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
        Button noButton = findViewById(R.id.noButton);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noClickListener != null) {
                    noClickListener.onClick(v);
                }
                dismiss(); // 다이얼로그 닫기
            }
        });
    }
}
