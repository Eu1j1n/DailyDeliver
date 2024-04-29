package com.example.dailydeliver.profile;

import com.google.gson.annotations.SerializedName;

public class CreditResponse {
    @SerializedName("credit")
    private int credit;

    public int getCredit() {
        return credit;
    }
}

