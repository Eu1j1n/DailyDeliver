package com.example.dailydeliver.Fragment;

import com.google.gson.annotations.SerializedName;

public class UnLikeData {
    @SerializedName("title")
    private String title;

    @SerializedName("location")
    private String location;

    public String getReceiveID() {
        return receiveID;
    }

    public void setReceiveID(String receiveID) {
        this.receiveID = receiveID;
    }

    @SerializedName("price")
    private String price;

    @SerializedName("userName")
    private String userName;
    @SerializedName("receiveID")
    private String receiveID;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
