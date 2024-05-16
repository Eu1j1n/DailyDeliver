package com.example.dailydeliver.Adapter;

public class WishData {

    private String image_uri;
    private String title;
    private String location;
    private String send_time;
    private String price;

    private String userName;

    private String description;

    private String saleType;

    private String bidPrice;

    private String remaining_time;

    private int state;



    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSaleType() {
        return saleType;
    }

    public void setSaleType(String saleType) {
        this.saleType = saleType;
    }

    public String getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(String bidPrice) {
        this.bidPrice = bidPrice;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public WishData(String image_uri, String title, String location, String send_time, String price, String userName
            , String description, String saleType, String bidPrice, String remaining_time, int state) {
        this.image_uri = image_uri;
        this.title = title;
        this.location = location;
        this.send_time = send_time;
        this.price = price;
        this.userName = userName;
        this.description = description;
        this.saleType = saleType;
        this.bidPrice = bidPrice;
        this.remaining_time = remaining_time;
        this.state = state;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public String getRemaining_time() {
        return remaining_time;
    }

    public void setRemaining_time(String remaining_time) {
        this.remaining_time = remaining_time;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }



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

    public String getSend_time() {
        return send_time;
    }

    public void setSend_time(String send_time) {
        this.send_time = send_time;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}