package com.example.dailydeliver.Chatting;

public class ChatRoomItem {
    private String chatRoomName;
    private String lastMessage;
    private String profileImage;

    private int messageCount;

    private String messageTime;

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }

    public ChatRoomItem(String chatRoomName, String lastMessage, String profileUri, int messageCount, String messageTime) {
        this.chatRoomName = chatRoomName;
        this.lastMessage = lastMessage;
        this.profileImage = profileImage;
        this.messageCount = messageCount;
        this.messageTime = messageTime;
    }

    public String getChatRoomName() {
        return chatRoomName;
    }

    public void setChatRoomName(String chatRoomName) {
        this.chatRoomName = chatRoomName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }



    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
