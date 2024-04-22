package com.example.dailydeliver.Chatting;

public class ChatRoomItem {
    // 채팅방 채팅 목록에 들어갈 내용들 넣는곳 !
    private String chatRoomName;
    private String lastMessage;
    private String profileImage;

    private int messageCount;

    private String messageTime;

    private String hideRoomName;

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

    public ChatRoomItem(String chatRoomName, String lastMessage, String profileUri, int messageCount, String messageTime
    ,String hideRoomName) {
        this.chatRoomName = chatRoomName; // 여기는 roomName을 to 기준으로 문자열 짤라서 뒤에 단어를 둘거임
        this.lastMessage = lastMessage;
        this.profileImage = profileImage;
        this.messageCount = messageCount;
        this.messageTime = messageTime;
        this.hideRoomName = hideRoomName; // 이건 찐 roomName 넣을거임
    }

    public String getHideRoomName() {
        return hideRoomName;
    }

    public void setHideRoomName(String hideRoomName) {
        this.hideRoomName = hideRoomName;
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
