package com.example.dailydeliver.profile;

import com.google.gson.annotations.SerializedName;

public class CreditUpdateRequest {
    @SerializedName("receiveID")
    private String receiveID;

    @SerializedName("credit")
    private int credit;

    public CreditUpdateRequest(String receiveID, int credit) {
        this.receiveID = receiveID;
        this.credit = credit;
    }
}
