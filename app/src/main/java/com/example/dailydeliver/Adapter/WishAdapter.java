package com.example.dailydeliver.Adapter;

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

public class WishAdapter extends RecyclerView.Adapter<WishAdapter.WishViewHolder> {

    private Context mContext;
    private List<WishData> mWishItemList;
    private OnItemClickListener mListener;

    private Map<Integer, CountDownTimer> wishTimersMap = new HashMap<>();


    private String baseUrl = "http://43.201.32.122/postImage/";

    String TAG = "위시 어댑터";


    public interface OnItemClickListener {
        void onItemClick(WishData item);
    }


    // 리스너 설정 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public WishAdapter(Context context, List<WishData> wishItemList) {
        mContext = context;
        mWishItemList = wishItemList;
    }

    @NonNull
    @Override
    public WishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.wishlistitem, parent, false);
        return new WishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishViewHolder holder, int position) {
        WishData wishItem = mWishItemList.get(position);
        holder.wishProductTitle.setText(wishItem.getTitle());
        holder.wishLocationTextView.setText(wishItem.getLocation());
        holder.wishUsernameTextView.setText(wishItem.getUserName());
        holder.wishTimeTextView.setText(wishItem.getSend_time());
        holder.wishPriceTextView.setText("즉시구매가 " + wishItem.getPrice());

        if (wishItem.getState() == 1) {
            holder.wishRemainingTimeTextView.setVisibility(View.VISIBLE);
            holder.wishRemainingTimeTextView.setText("입찰 종료");
            holder.wishBiddingPriceTextView.setText("입찰이 종료되었습니다.");
            holder.wishSoldOutImageView.setVisibility(View.VISIBLE);



        } else if (wishItem.getSaleType().equals("bid")) {
            holder.wishBiddingPriceTextView.setVisibility(View.VISIBLE);
            holder.wishStickerImageView.setVisibility(View.VISIBLE);

            String remainingTime = wishItem.getRemaining_time();

            Log.d(TAG, "remainingTime" + remainingTime);
            holder.wishRemainingTimeTextView.setVisibility(View.VISIBLE);
            holder.wishBiddingPriceTextView.setText("현재입찰가 " + wishItem.getBidPrice());

            if (remainingTime.equals("입찰 종료")) {
                holder.wishRemainingTimeTextView.setText("입찰 종료");
            } else {
                // 시간 형식이 %H:%I:%S일 경우에만 카운트다운 타이머 설정
                if (remainingTime.matches("\\d{2}:\\d{2}:\\d{2}")) { // 정규표현식으로 패턴
                    long millisInFuture = parseRemainingTime(wishItem.getRemaining_time());

                    // 기존 타이머가 있다면 중지
                    if (wishTimersMap.containsKey(position)) {
                        wishTimersMap.get(position).cancel();
                    }

                    // 새로운 타이머 시작
                    CountDownTimer countDownTimer = new CountDownTimer(millisInFuture, 1000) {
                        public void onTick(long millisUntilFinished) {
                            // 남은 시간을 시:분:초 포맷으로 변환
                            long hours = (millisUntilFinished / (1000 * 60 * 60)) % 24;
                            long minutes = (millisUntilFinished / (1000 * 60)) % 60;
                            long seconds = (millisUntilFinished / 1000) % 60;

                            String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                            holder.wishRemainingTimeTextView.setText(timeFormatted);
                        }

                        public void onFinish() {
                            holder.wishRemainingTimeTextView.setText("입찰 종료");
                        }
                    };
                    countDownTimer.start();
                    holder.countDownTimer = countDownTimer;
                    wishTimersMap.put(position, countDownTimer);
                } else {
                    // 시간 형식이 %H:%I:%S가 아닌 경우에도 값을 설정
                    holder.wishRemainingTimeTextView.setText(remainingTime);
                }
            }
        } else {
            holder.wishBiddingPriceTextView.setVisibility(View.GONE);
            holder.wishRemainingTimeTextView.setVisibility(View.GONE);
            holder.wishRemainingTimeTextView.setText(wishItem.getRemaining_time());
            holder.wishStickerImageView.setVisibility(View.GONE);
        }



        // 이미지를 설정
        if (wishItem.getImage_uri() != null) {
            String decodedImageUrl = Uri.decode(baseUrl + wishItem.getImage_uri());
            Glide.with(holder.itemView.getContext())
                    .load(decodedImageUrl)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(holder.wishProductImage);
        } else {
            holder.wishProductImage.setImageResource(R.drawable.ic_camera);
        }




        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition(); // 현재 위치 가져오기
                if (position != RecyclerView.NO_POSITION && mListener != null) {
                    mListener.onItemClick(mWishItemList.get(position)); // 클릭한 아이템의 데이터를 전달
                }
            }
        });




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
    public int getItemCount() {
        return mWishItemList.size();
    }

    public class WishViewHolder extends RecyclerView.ViewHolder {

        public ImageView wishProductImage;

        CountDownTimer countDownTimer;
        public TextView wishProductTitle;
        public TextView wishLocationTextView;
        public TextView wishTimeTextView;
        public TextView wishBiddingPriceTextView;
        public TextView wishUsernameTextView;
        public TextView wishPriceTextView;

        public TextView wishRemainingTimeTextView;

        CircleImageView wishStickerImageView;

        CircleImageView wishSoldOutImageView;

        public WishViewHolder(@NonNull View itemView) {
            super(itemView);


            wishProductImage = itemView.findViewById(R.id.wishProductImage);
            wishProductTitle = itemView.findViewById(R.id.wishProductTitle);
            wishLocationTextView = itemView.findViewById(R.id.wishLocationTextView);
            wishTimeTextView = itemView.findViewById(R.id.wishTimeTextView);
            wishBiddingPriceTextView = itemView.findViewById(R.id.wishBiddingPriceTextView);
            wishUsernameTextView = itemView.findViewById(R.id.wishUsernameTextView);
            wishPriceTextView = itemView.findViewById(R.id.wishPriceTextView);
            wishStickerImageView = itemView.findViewById(R.id.wishStickerImageView);
            wishSoldOutImageView = itemView.findViewById(R.id.wishSoldOutImageView);
            wishRemainingTimeTextView = itemView.findViewById(R.id.wishRemainingTimeTextView);
        }
    }
}
