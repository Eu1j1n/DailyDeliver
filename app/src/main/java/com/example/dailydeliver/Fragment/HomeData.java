package com.example.dailydeliver.Fragment;

public class HomeData {

    private String image_uri;
    private String title;
    private String location;
    private String send_time;
    private String price;

    private String userName;

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HomeData(String image_uri, String title, String location, String send_time, String price, String userName
    , String description) {
        this.image_uri = image_uri;
        this.title = title;
        this.location = location;
        this.send_time = send_time;
        this.price = price;
        this.userName = userName;
        this.description = description;
    }

    public String getImage_uri() {
        return image_uri;
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
