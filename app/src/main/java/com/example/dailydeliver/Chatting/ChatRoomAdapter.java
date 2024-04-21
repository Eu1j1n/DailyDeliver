package com.example.dailydeliver.Chatting;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailydeliver.R;

import java.util.List;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    private List<ChatRoomItem> chatRoomItems;
    private OnItemClickListener listener;
    private Context context;

    public ChatRoomAdapter(Context context, List<ChatRoomItem> chatRoomItems) {
        this.chatRoomItems = chatRoomItems;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatroomitem, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoomItem chatRoomItem = chatRoomItems.get(position);

        holder.txtChatName.setText(chatRoomItem.getChatRoomName());
        holder.txtLastMessage.setText(chatRoomItem.getLastMessage());
       holder.messageTime.setText(chatRoomItem.getMessageTime());

        if (chatRoomItem.getMessageCount() == 0) {
            holder.messageCount.setVisibility(View.GONE);
        } else {
            holder.messageCount.setVisibility(View.VISIBLE);
            holder.messageCount.setText(String.valueOf(chatRoomItem.getMessageCount()));
        }

        // 아이템 클릭 이벤트 처리
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = holder.getAdapterPosition();
                if (listener != null) {
                    listener.onItemClick(clickedPosition); // 콜백 호출
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRoomItems.size();
    }

    public class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView txtChatName;
        TextView txtLastMessage;
        TextView messageTime;

        TextView messageCount;

        TextView hideRealRoomName;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            txtChatName = itemView.findViewById(R.id.chatName);
            txtLastMessage = itemView.findViewById(R.id.lastMessage);
            messageCount = itemView.findViewById(R.id.messageCount);
            messageTime = itemView.findViewById(R.id.messageTime);
            hideRealRoomName = itemView.findViewById(R.id.hideRealRoomName);

        }
    }
}

