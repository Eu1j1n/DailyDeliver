package com.example.dailydeliver.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    private List<HomeData> homeData;
    private LayoutInflater mInflater;

    private Context mContext;

    private String baseUrl = "http://52.79.88.52/postImage/";



    String TAG = "Adapter 입니당";

    // 데이터와 연결하기 위한 생성자
    HomeAdapter(Context context, List<HomeData> homeData) {
        this.mInflater = LayoutInflater.from(context);
        this.homeData = homeData;
        this.mContext = context;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.postitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 데이터를 뷰에 연결합니다.
        final HomeData item = homeData.get(position);
        holder.productTitle.setText(item.getTitle());
        holder.locationTextView.setText(item.getLocation());
        holder.timeTextView.setText(item.getSend_time());
        holder.priceTextView.setText(item.getPrice());
        holder.userNametextView.setText(item.getUserName());



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 아이템 클릭 시 ProductDetail 액티비티로 이동하고 클릭된 아이템의 정보를 전달
                Intent intent = new Intent(mContext, ProductDetail.class);
                intent.putExtra("imageUri", item.getImage_uri());
                intent.putExtra("title", item.getTitle());
                intent.putExtra("location", item.getLocation());
                intent.putExtra("send_time", item.getSend_time());
                intent.putExtra("price", item.getPrice());

                intent.putExtra("user_name", item.getUserName());
                intent.putExtra("description", item.getDescription());
                mContext.startActivity(intent);
            }
        });

        // 이미지를 설정합니다.
        if (item.getImage_uri() != null) {
            Log.d(TAG, "get productImage" + item.getImage_uri());
            String decodedImageUrl = Uri.decode(baseUrl + item.getImage_uri());
            Log.d(TAG, "decodedImageUrl" + decodedImageUrl);
            Glide.with(holder.itemView.getContext())
                    .load(decodedImageUrl)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)// 이미지 URI 설정
                    .into(holder.productImageView); // 이미지를 표시할 ImageView
        } else {
            Log.d(TAG, "여기로 빠짐?" );
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
            productImageView = itemView.findViewById(R.id.productImage); // 상품 이미지뷰 넣는곳
            productTitle = itemView.findViewById(R.id.productTitle);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            userNametextView = itemView.findViewById(R.id.usernameTextView);
        }
    }
}
