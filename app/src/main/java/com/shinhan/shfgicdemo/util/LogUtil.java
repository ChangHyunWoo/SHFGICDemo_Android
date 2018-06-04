package com.shinhan.shfgicdemo.util;

import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;

/**
 * <p>
 * Log 를 컨트롤 하기 위해서 만든 클래스
 * <p>
 * 로그의 사용 유무를 결정한다.
 * </p>
 */
public class LogUtil {
    private static final int LOG_MIN_SIZE = 0;

    public static void print(String tag, Object... obj) {
        if (IS_LOGGING()) {
            StringBuffer sb = new StringBuffer();
            for (Object message : obj) {
                sb.append(String.valueOf(message)).append(" / ");
            }
            d(tag, sb.toString());
        }
    }

    public static void e(String tag, String message) {
        if (IS_LOGGING() && message != null && message.length() > LOG_MIN_SIZE) {
            android.util.Log.e(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable tr) {
        if (IS_LOGGING() && message != null && message.length() > LOG_MIN_SIZE) {
            android.util.Log.e(tag, message, tr);
        }
    }

    public static void e(String tag, Throwable tr) {
        e(tag, "", tr);
    }

    public static void d(String tag, String message) {
        if (IS_LOGGING() && message != null && message.length() > LOG_MIN_SIZE) {
            android.util.Log.d(tag, message);
        }
    }

    public static void d(String tag, String message, Throwable tr) {
        if (IS_LOGGING() && message != null && message.length() > LOG_MIN_SIZE) {
            android.util.Log.d(tag, message, tr);
        }
    }

    public static void w(String tag, String message) {
        if (IS_LOGGING() && message != null && message.length() > LOG_MIN_SIZE) {
            android.util.Log.w(tag, message);
        }
    }

    public static void w(String tag, String message, Throwable tr) {
        if (IS_LOGGING() && message != null && message.length() > LOG_MIN_SIZE) {
            android.util.Log.w(tag, message, tr);
        }
    }

    public static void i(String tag, String message) {
        if (IS_LOGGING() && message != null && message.length() > LOG_MIN_SIZE) {
            android.util.Log.i(tag, message);
        }
    }

    public static void i(String tag, String message, Throwable tr) {
        if (IS_LOGGING() && message != null && message.length() > LOG_MIN_SIZE) {
            android.util.Log.i(tag, message, tr);
        }
    }

    public static void trace(Exception e) {
        if (IS_LOGGING()) {
            e.printStackTrace();
        }
    }

    public static boolean IS_LOGGING() {
        return SHFGICConfig.isLogging;
    }
}