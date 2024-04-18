package com.example.dailydeliver.Chatting;

public class Message {


    private String text;  // 메시지 텍스트
    private String time;  // 메시지 시간
    private int type;  // 메시지가 내 메시지인지 여부
    private String sender;

    private String profileImageUrl;

    private String date;

    private String chatImageUrl;

    private String messageID;

    private int readStatus;



    public static final int TYPE_MY_MESSAGE = 1;
    public static final int TYPE_OTHER_MESSAGE = 2;

    public static final int TYPE_MY_PICTURE_MESSAGE = 3;
    public static final int TYPE_OTHER_PICTURE_MESSAGE = 4;




    public Message(String text, String time, int type, String sender, String profileImageUrl, String date, String chatImageUrl
    , String messageID, int readStatus) {
        this.text = text;
        this.time = time;
        this.type = type;
        this.sender = sender;
        this.profileImageUrl = profileImageUrl;
        this.date = date;
        this.chatImageUrl = chatImageUrl;
        this.messageID = messageID;
        this.readStatus = readStatus;
    }

    public String getMessageID() {
        return messageID;
    }

    public int getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getChatImageUrl() {
        return chatImageUrl;
    }

    public void setChatImageUrl(String chatImageUrl) {
        this.chatImageUrl = chatImageUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}

