package com.example.dailydeliver.Fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {

    public static String getTimeAgo(String dateTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date past = format.parse(dateTime);
            Date now = new Date();

            long second = (now.getTime() - past.getTime()) / 1000;

            if (second < 60) {
                return "방금 전";
            }

            long minute = second / 60;
            if (minute < 60) {
                return minute + "분 전";
            }

            long hour = minute / 60;
            if (hour < 24) {
                return hour + "시간 전";
            }

            long day = hour / 24;
            if (day == 1) {
                return "어제";
            }

            return day + "일 전";

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }
}
