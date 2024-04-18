package com.example.dailydeliver.Chatting;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.dailydeliver.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<Message> messageList;

    String TAG = "메세지어댑터 액티비티";

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull

    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case Message.TYPE_MY_MESSAGE:
                view = LayoutInflater.from(context).inflate(R.layout.mymessageitem, parent, false);
                break;
            case Message.TYPE_OTHER_MESSAGE:
                view = LayoutInflater.from(context).inflate(R.layout.othermessageitem, parent, false);
                break;
            case Message.TYPE_MY_PICTURE_MESSAGE:
                view = LayoutInflater.from(context).inflate(R.layout.mypicturemessageitem, parent, false);
                break;
            case Message.TYPE_OTHER_PICTURE_MESSAGE:
                view = LayoutInflater.from(context).inflate(R.layout.otherpicturemessageitem, parent, false);
                break;
            default:
                // 기본적으로 mymessageitem을 사용합니다.
                view = LayoutInflater.from(context).inflate(R.layout.mymessageitem, parent, false);
                break;
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        // Null 체크 추가
        if (holder != null) {
            Message message = messageList.get(position);

            if (message.getType() == Message.TYPE_OTHER_MESSAGE) {
                holder.otherID.setText(message.getSender());
                holder.messageText.setText(message.getText());
                holder.messageTime.setText(message.getTime());

                if (message.getReadStatus() == 2) {
                    //  읽은 경우
                    Log.d(TAG, "내가 상대 메세지 읽은 경우야들어오긴함?");
                    holder.otherTextShown.setVisibility(View.GONE); // 안 읽은 상태를 나타내는 TextView를 보이게 설정
                } else {
                    Log.d(TAG, "내가 상대껄 안읽은경우야");
                    holder.otherTextShown.setVisibility(View.VISIBLE); // 읽은 상태를 나타내는 TextView를 숨김
                }




                if (message.getProfileImageUrl() != null && !message.getProfileImageUrl().isEmpty()) {
                    Glide.with(context).load(message.getProfileImageUrl())
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(holder.circleImageView);
                } else {

                    Glide.with(context).load(R.drawable.profile)
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(holder.circleImageView);
                }
            } else if (message.getType() == Message.TYPE_MY_MESSAGE) {
                holder.myID.setText(message.getSender());
                holder.messageText.setText(message.getText());
                holder.messageTime.setText(message.getTime());

                if (message.getReadStatus() == 2) {

                    Log.d(TAG, "상대가 내 메세지 읽은 경우야들어오긴함?");
                    holder.myTextShown.setVisibility(View.GONE); // 안 읽은 상태를 나타내는 TextView를 보이게 설정
                } else {
                    // 상대가 내 메세지를 읽은경우
                    Log.d(TAG, "상대가 내 메세지 안읽은 경우");
                    holder.myTextShown.setVisibility(View.VISIBLE); // 읽은 상태를 나타내는 TextView를 숨김
                }
            }

            // 이미지 메시지 처리 추가
            if (message.getType() == Message.TYPE_MY_PICTURE_MESSAGE) {
                holder.chatMyImageText.setText(message.getText());
                holder.chatMyImageTime.setText(message.getTime());
                // 내가 보낸 이미지 메시지인 경우
                Glide.with(context).load(message.getChatImageUrl()).into(holder.myChatImageView);
                holder.myImgTextShown.setVisibility(message.getReadStatus() == 2 ? View.GONE : View.VISIBLE);



            } else if (message.getType() == Message.TYPE_OTHER_PICTURE_MESSAGE) {

                if (message.getProfileImageUrl() != null && !message.getProfileImageUrl().isEmpty()) {
                    Glide.with(context).load(message.getProfileImageUrl())
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(holder.circleChatImageView);
                } else {

                    Glide.with(context).load(R.drawable.profile)
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(holder.circleChatImageView);
                }

                Glide.with(context).load(message.getChatImageUrl()).into(holder.otherChatImageView);
                holder.otherImageChatID.setText(message.getSender());
                holder.otherImageChatTime.setText(message.getTime());
                holder.blank.setText(message.getText());
                holder.otherImgTextShown.setVisibility(message.getReadStatus() == 2 ? View.GONE : View.VISIBLE);



            }
        }
    }




    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);

        return message.getType();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView messageTime;
        TextView otherID;
        TextView myID;
        CircleImageView circleImageView;

        ImageView myChatImageView;

        TextView chatMyImageText;

        TextView chatMyImageTime;

        ImageView otherChatImageView;

        TextView otherImageChatID;

        TextView otherImageChatTime;

        CircleImageView circleChatImageView;

        TextView blank;

        TextView myTextShown;  //내 메세지 말풍선 옆 1 써져있는아이템

        TextView otherTextShown; // 상대 메세지 말풍선 옆 1 써져있는아이템

        TextView myImgTextShown;

        TextView otherImgTextShown;


        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.txt_message);
            messageTime = itemView.findViewById(R.id.txt_date);
            myTextShown = itemView.findViewById(R.id.mytxt_isShown);
            otherID = itemView.findViewById(R.id.txt_name);
            myID = itemView.findViewById(R.id.my_name);
            circleImageView = itemView.findViewById(R.id.chatProfileImage);
            myChatImageView = itemView.findViewById(R.id.my_img_message);
            chatMyImageText = itemView.findViewById(R.id.my_img_text);
            chatMyImageTime = itemView.findViewById(R.id.my_img_txt_date);
            otherChatImageView = itemView.findViewById(R.id.other_img_message);
            otherImageChatID = itemView.findViewById(R.id.other_txt_name);
            otherImageChatTime = itemView.findViewById(R.id.other_txt_date);
            circleChatImageView = itemView.findViewById(R.id.chatImageProfileImage);
            blank = itemView.findViewById(R.id.blank);
            otherTextShown = itemView.findViewById(R.id.other_txt_isShown);
            myImgTextShown = itemView.findViewById(R.id.my_img_txt_isShown);
            otherImgTextShown = itemView.findViewById(R.id.other_img_txt_isShown);


        }
    }
}
