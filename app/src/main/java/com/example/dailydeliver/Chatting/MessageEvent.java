package com.example.dailydeliver.Chatting;

public class MessageEvent {
    private String senderID;
    private String messageContent;
    private String time;
    private String roomName;



    public MessageEvent(String senderID, String messageContent, String time, String roomName) {
        this.senderID = senderID;
        this.messageContent = messageContent;
        this.time = time;
        this.roomName = roomName;
    }

    public String getSenderID() {
        return senderID;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public String getTime() {
        return time;
    }

    public String getRoomName() {
        return roomName;
    }
}

