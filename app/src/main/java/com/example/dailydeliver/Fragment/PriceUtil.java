package com.example.dailydeliver.Fragment;

import java.util.Locale;

public class PriceUtil {

    public static String formatPrice(String price) {
        // 쉼표(,) 제거
        String formattedPrice = price.replace(",", "");
        // ₩ 기호 제거
        formattedPrice = formattedPrice.replace("₩", "");
        // 숫자 형식으로 변환
        double priceValue = Double.parseDouble(formattedPrice);
        // 원 단위로 포맷팅
        String formattedPriceText = String.format(Locale.getDefault(), "%,.0f", priceValue) + "원";
        return formattedPriceText;
    }
}

