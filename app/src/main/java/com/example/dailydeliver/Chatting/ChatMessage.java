package com.example.dailydeliver.Chatting;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {
    @SerializedName("Room")
    private String roomName;

    @SerializedName("Sender")
    private String sender;

    @SerializedName("Message")
    private String message;

    @SerializedName("Time")
    private String time;

    @SerializedName("ChatDate")
    private String date;



    @SerializedName("isRead")
    private int isRead;

    @SerializedName("MessageID")
    private String messageID;

    public int getIsRead() {
        return isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }



    public String getRoomName() {
        return roomName;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }


}
