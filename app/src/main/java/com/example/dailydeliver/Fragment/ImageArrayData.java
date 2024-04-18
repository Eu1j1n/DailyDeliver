package com.example.dailydeliver.Fragment;

import java.util.ArrayList;
import java.util.List;

public class ImageArrayData {
    private List<String> imageUrls;

    public ImageArrayData() {
        imageUrls = new ArrayList<>();
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void addImageUrl(String imageUrl) {
        imageUrls.add(imageUrl);
    }

    public void removeImageUrl(String imageUrl) {
        imageUrls.remove(imageUrl);
    }
}
