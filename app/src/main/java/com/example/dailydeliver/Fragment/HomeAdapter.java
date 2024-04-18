package com.example.dailydeliver.Fragment;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.dailydeliver.R;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    private List<HomeData> homeData;
    private LayoutInflater mInflater;
    private OnItemClickListener itemClickListener; // 클릭 리스너 인터페이스

    private String baseUrl = "http://43.201.32.122/postImage/";

    // 클릭 이벤트 처리를 위한 인터페이스
    public interface OnItemClickListener {
        void onItemClick(HomeData item);
    }

    HomeAdapter(Context context, List<HomeData> homeData, OnItemClickListener listener) {
        this.mInflater = LayoutInflater.from(context);
        this.homeData = homeData;
        this.itemClickListener = listener; // 클릭 리스너 초기화
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.postitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final HomeData item = homeData.get(position);
        holder.productTitle.setText(item.getTitle());
        holder.locationTextView.setText(item.getLocation());
        holder.timeTextView.setText(item.getSend_time());
        holder.priceTextView.setText(item.getPrice());
        holder.userNametextView.setText(item.getUserName());



        // 아이템 클릭 이벤트 처리
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(item); // 클릭된 아이템 전달
                }
            }
        });

        // 이미지를 설정
        if (item.getImage_uri() != null) {
            String decodedImageUrl = Uri.decode(baseUrl + item.getImage_uri());
            Glide.with(holder.itemView.getContext())
                    .load(decodedImageUrl)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(holder.productImageView);
        } else {
            holder.productImageView.setImageResource(R.drawable.ic_camera);
        }
    }

    @Override
    public int getItemCount() {
        return homeData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView productTitle;
        TextView locationTextView;
        TextView timeTextView;
        TextView priceTextView;
        TextView userNametextView;
        ImageView productImageView;

        ViewHolder(View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImage);
            productTitle = itemView.findViewById(R.id.productTitle);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            userNametextView = itemView.findViewById(R.id.usernameTextView);
        }
    }
}
