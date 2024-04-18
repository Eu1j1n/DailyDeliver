package com.example.dailydeliver.Fragment;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dailydeliver.R;
import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Uri> mImageUriList;

    private OnImageDeleteListener mImageDeleteListener;

    public interface OnImageDeleteListener {
        void onImageDeleted(int position);
    }

    private void deleteImage(int position) {
        mImageUriList.remove(position);
        notifyDataSetChanged();
        // 인터페이스를 통해 삭제 이벤트를 액티비티로 전달
        if (mImageDeleteListener != null) {
            mImageDeleteListener.onImageDeleted(position);
        }
    }

    public ImageAdapter(Context context, List<Uri> imageUriList, OnImageDeleteListener listener) {
        mContext = context;
        mImageUriList = imageUriList;
        mImageDeleteListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.postimageitemview, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = mImageUriList.get(position);
        holder.bind(imageUri);
    }

    @Override
    public int getItemCount() {
        return mImageUriList.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private ImageButton imageDeleteButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.itemImageView);
            imageDeleteButton = itemView.findViewById(R.id.imageDeleteButton);

            imageDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        mImageUriList.remove(position);
                        notifyItemRemoved(position);

                        if (mImageDeleteListener != null) {
                            mImageDeleteListener.onImageDeleted(position);
                        }

                    }
                }
            });
        }

        public void bind(Uri imageUri) {
            imageView.setImageURI(imageUri);
        }


    }
}
