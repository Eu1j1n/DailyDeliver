package com.example.dailydeliver.Fragment;

import java.util.List;

public class PostDetailData {

    private List<String> image_uri;
    private String title;
    private String location;
    private String send_time;
    private String price;
    private String userName;
    private String description;

    private Double latitude;

    private Double longitude;

    private String saleType;

    private String bidPrice;

    private String remaining_time;

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

    public String getRemaining_time() {
        return remaining_time;
    }

    public void setRemaining_time(String remaining_time) {
        this.remaining_time = remaining_time;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public PostDetailData(List<String> image_uri, String title, String location, String send_time, String price, String userName
            , String description, Double latitude, Double longitude, String saleType, String bidPrice, String remaining_time) {
        this.image_uri = image_uri;
        this.title = title;
        this.location = location;
        this.send_time = send_time;
        this.price = price;
        this.userName = userName;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.saleType = saleType;
        this.bidPrice = bidPrice;
        this.remaining_time = remaining_time;
    }

    public List<String> getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(List<String> image_uri) {
        this.image_uri = image_uri;
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

    public String getSend_time() {
        return send_time;
    }

    public void setSend_time(String send_time) {
        this.send_time = send_time;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
