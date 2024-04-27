package com.example.dailydeliver.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.Log;
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
import java.util.Locale;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    private List<HomeData> homeData;
    private LayoutInflater mInflater;
    private OnItemClickListener itemClickListener; // 클릭 리스너 인터페이스

    String TAG = "홈 어댑터";

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
        holder.priceTextView.setText("즉시구매가 " +item.getPrice());
        holder.userNametextView.setText(item.getUserName());

        if(item.getSaleType().equals("bid")) {
            holder.biddingPriceTextView.setVisibility(View.VISIBLE);
            holder.remainingTimeTextView.setVisibility(View.VISIBLE);
            holder.biddingPriceTextView.setText("현재입찰가 " + item.getBidPrice());
            holder.remainingTimeTextView.setText(item.getRemaining_time());

            Log.d(TAG, "remaining time" + item.getRemaining_time());

            long millisInFuture = parseRemainingTime(item.getRemaining_time());

            // 기존 타이머가 있다면 취소
            if (holder.countDownTimer != null) {
                holder.countDownTimer.cancel();
            }

            holder.countDownTimer = new CountDownTimer(millisInFuture, 1000) {
                public void onTick(long millisUntilFinished) {
                    // 남은 시간을 시:분:초 포맷으로 변환
                    long hours = (millisUntilFinished / (1000 * 60 * 60)) % 24;
                    long minutes = (millisUntilFinished / (1000 * 60)) % 60;
                    long seconds = (millisUntilFinished / 1000) % 60;

                    String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                    holder.remainingTimeTextView.setText(timeFormatted);
                }

                public void onFinish() {
                    holder.remainingTimeTextView.setText("00:00:00");
                }
            }.start();


        }else {
            holder.biddingPriceTextView.setVisibility(View.GONE);
            holder.remainingTimeTextView.setVisibility(View.GONE);
        }



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
        CountDownTimer countDownTimer;
        TextView locationTextView;
        TextView timeTextView;
        TextView priceTextView;
        TextView userNametextView;
        ImageView productImageView;

        TextView biddingPriceTextView;

        TextView remainingTimeTextView;

        ViewHolder(View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImage);
            productTitle = itemView.findViewById(R.id.productTitle);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            userNametextView = itemView.findViewById(R.id.usernameTextView);
            biddingPriceTextView = itemView.findViewById(R.id.biddingPriceTextView);
            remainingTimeTextView = itemView.findViewById(R.id.remainingTimeTextView);
        }
    }

    private long parseRemainingTime(String remainingTime) {
        String[] parts = remainingTime.split(":");
        if (parts.length != 3) return 0;

        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        return (hours * 3600 + minutes * 60 + seconds) * 1000L; // 밀리초로 변환
    }

}
