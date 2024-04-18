package com.example.dailydeliver.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.example.dailydeliver.R;

public class ImageDetail extends AppCompatActivity {

    private ImageButton imageDetailBackButton;
    private SubsamplingScaleImageView imageDetailImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        imageDetailBackButton = findViewById(R.id.imageDetailBackButton);
        imageDetailImageView = findViewById(R.id.imageDetailImageView);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("imageUrl")) {
            String imageUrl = intent.getStringExtra("imageUrl");

            // Glide를 사용하여 이미지를 비트맵으로 로드
            Glide.with(this)
                    .asBitmap()
                    .load(imageUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            // 비트맵을 SubsamplingScaleImageView에 설정
                            imageDetailImageView.setImage(ImageSource.bitmap(resource));
                            imageDetailImageView.setMaxScale(2.0F);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // Implement if needed
                        }
                    });
        }

        imageDetailBackButton.setOnClickListener(v -> finish());
    }
}
