package com.example.dailydeliver.Fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.dailydeliver.R;
import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {
    private List<String> imageUrls;
    private Context context;

    private String serverImageUri = "http://52.79.88.52/postImage/";

    // 이미지 클릭을 처리하는 리스너 인터페이스 정의
    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    private OnImageClickListener onImageClickListener; // 리스너 멤버 변수

    public ImagePagerAdapter(Context context, List<String> imageUrls, OnImageClickListener onImageClickListener) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.onImageClickListener = onImageClickListener; // 리스너 초기화
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_image_itemview, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(context).load(serverImageUri + imageUrl).into(holder.imageView);

        // 이미지 클릭 이벤트 처리
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭된 이미지의 위치(position)를  전달
                if (onImageClickListener != null) {
                    int clickedPosition = holder.getAdapterPosition();
                    if (clickedPosition != RecyclerView.NO_POSITION) {
                        onImageClickListener.onImageClick(clickedPosition);
                    }
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
