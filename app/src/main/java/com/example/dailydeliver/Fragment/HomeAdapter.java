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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

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

    private Map<Integer, CountDownTimer> timersMap = new HashMap<>();

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
        holder.soldOutImageView.setVisibility(View.GONE);
        holder.locationTextView.setText(item.getLocation());
        holder.timeTextView.setText(item.getSend_time());
        Log.d(TAG, "time" + item.getSend_time());
        holder.priceTextView.setText("즉시구매가 " + item.getPrice());
        holder.userNametextView.setText(item.getUserName());

        if (item.getState() == 1) {
            holder.remainingTimeTextView.setVisibility(View.VISIBLE);
            holder.remainingTimeTextView.setText("입찰 종료");
            holder.biddingPriceTextView.setText("입찰이 종료되었습니다.");
            holder.soldOutImageView.setVisibility(View.VISIBLE);



        } else if (item.getSaleType().equals("bid")) {
            holder.biddingPriceTextView.setVisibility(View.VISIBLE);
            holder.stickerImageView.setVisibility(View.VISIBLE);

            String remainingTime = item.getRemaining_time();

            Log.d(TAG, "remainingTime" + remainingTime);
            holder.remainingTimeTextView.setVisibility(View.VISIBLE);
            holder.biddingPriceTextView.setText("현재입찰가 " + item.getBidPrice());

            if (remainingTime.equals("입찰 종료")) {
                holder.remainingTimeTextView.setText("입찰 종료");
            } else {
                // 시간 형식이 %H:%I:%S일 경우에만 카운트다운 타이머 설정
                if (remainingTime.matches("\\d{2}:\\d{2}:\\d{2}")) { // 정규표현식으로 패턴
                    long millisInFuture = parseRemainingTime(item.getRemaining_time());

                    // 기존 타이머가 있다면 중지
                    if (timersMap.containsKey(position)) {
                        timersMap.get(position).cancel();
                    }

                    // 새로운 타이머 시작
                    CountDownTimer countDownTimer = new CountDownTimer(millisInFuture, 1000) {
                        public void onTick(long millisUntilFinished) {
                            // 남은 시간을 시:분:초 포맷으로 변환
                            long hours = (millisUntilFinished / (1000 * 60 * 60)) % 24;
                            long minutes = (millisUntilFinished / (1000 * 60)) % 60;
                            long seconds = (millisUntilFinished / 1000) % 60;

                            String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                            holder.remainingTimeTextView.setText(timeFormatted);
                        }

                        public void onFinish() {
                            holder.remainingTimeTextView.setText("입찰 종료");
                        }
                    };
                    countDownTimer.start();
                    holder.countDownTimer = countDownTimer;
                    timersMap.put(position, countDownTimer);
                } else {
                    // 시간 형식이 %H:%I:%S가 아닌 경우에도 값을 설정
                    holder.remainingTimeTextView.setText(remainingTime);
                }
            }
        } else {
            holder.biddingPriceTextView.setVisibility(View.GONE);
            holder.remainingTimeTextView.setVisibility(View.GONE);
            holder.remainingTimeTextView.setText(item.getRemaining_time());
            holder.stickerImageView.setVisibility(View.GONE);
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

        CircleImageView stickerImageView;

        CircleImageView soldOutImageView;



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
            stickerImageView = itemView.findViewById(R.id.stickerImageView);
            soldOutImageView = itemView.findViewById(R.id.soldOutImageView);
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

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);

        if (holder.countDownTimer != null) {
            holder.countDownTimer.cancel();
        }
    }
}
