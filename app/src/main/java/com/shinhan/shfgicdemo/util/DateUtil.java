package com.shinhan.shfgicdemo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by sangheun on 2018. 3. 19..
 */

public class DateUtil {
    public static final String TAG = DateUtil.class.getName();

    private static final SimpleDateFormat defaultDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);

    public static String getNowDate() {
        Date currentTime = new Date();
        String nowDate = defaultDateFormat.format(currentTime);

        LogUtil.d(TAG, "getNowDate : " + nowDate);
        return nowDate;
    }

    public static String getDateMonthAdd(String strDate, int month) {
        try {
            Date date = defaultDateFormat.parse(strDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MONTH, month);
            String returnDate = defaultDateFormat.format(calendar.getTime());

            LogUtil.d(TAG, "getDateAdd : " + strDate + " , " + returnDate);
            return returnDate;

        } catch (ParseException e) {

        }
        return "";
    }

    public static long getDiffDays(String strDate) {
        long diffTime = getDate(strDate) - getDate(getNowDate());
        long diffDays = TimeUnit.MILLISECONDS.toDays(diffTime);

        LogUtil.d(TAG, "getDiffDays : " + strDate + " // " + diffDays);
        return diffDays;
    }

    private static Long getDate(String strDate) {
        try {
            Date date = defaultDateFormat.parse(strDate);
            long timeMillis = date.getTime();

            LogUtil.d(TAG, "getDate : " + strDate + " // " + timeMillis);
            return timeMillis;
        } catch (ParseException e) {

        }
        return 0l;
    }

    public static String getExpiryDate(String strDate) {
        if (strDate.length() == 8) {
            strDate = strDate.substring(0, 4) + "." + strDate.substring(4, 6) + "." + strDate.substring(6, 8);
            return strDate;
        }
        return strDate;
    }
}
