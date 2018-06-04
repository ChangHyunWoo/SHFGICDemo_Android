package com.shinhan.shfgicdemo.util;

import java.util.HashMap;

/**
 * StringUtil String 이 Null인지 아닌지 체크한다.
 */
public class StringUtil {
    private static final String TAG = StringUtil.class.getName();

    /**
     * 문자를 null로 리턴되지 않도록 한다.
     *
     * @param str
     * @return
     */
    public static String notNullString(String str) {
        if (str == null || "null".equals(str)) {
            return "";
        } else {
            return str;
        }
    }

    /**
     * 문자를 null로 리턴되지 않도록 한다.
     *
     * @param str
     * @return
     */
    public static String notNullString(Object str) {
        if (str == null) {
            return "";
        } else {
            return str.toString();
        }
    }

    public static Object notNullObject(Object obj) {
        if (obj == null) {
            return "";
        } else {
            return obj;
        }
    }

    /**
     * 문자를 null로 리턴하지 않는다.
     *
     * @param obj
     * @param defString
     * @return
     */
    public static String notNullString(Object obj, String defString) {
        String retString = notNullString(obj);
        if (retString.equals("")) {
            return defString;
        } else {
            return retString;
        }
    }

    /**
     * 문자를 null로 리턴하지 않는다.
     *
     * @param str
     * @param defString
     * @return
     */
    public static String notNullString(String str, String defString) {
        String retString = notNullString(str);
        if (retString.equals("")) {
            return defString;
        } else {
            return retString;
        }
    }

    /**
     * 문자열이 널인지를 판단한다.
     *
     * @param str
     * @return
     */
    public static boolean isEmptyString(String str) {
        return notNullString(str).equals("") || notNullString(str).equals("null") || str.trim().length() == 0;
    }

    public static boolean isEmptyString(CharSequence str) {
        return isEmptyString(str.toString());
    }

    public static boolean isEmptyObject(Object obj) {
        return notNullObject(obj).equals("");
    }

    public static String getString(HashMap<String, String> item, String key, String defaultValue) {
        if (item == null)
            return defaultValue;
        if (StringUtil.isEmptyString(item.get(key))) {
            return defaultValue;
        }
        return item.get(key);
    }

    public static boolean isEmpty(Object obj) {
        if (obj instanceof String) {
            if (obj == null || ((String) obj).trim().isEmpty() || "null".equals(obj)) {
                return true;
            }
        } else {
            if (obj == null) {
                return true;
            }
        }
        return false;
    }

    public static String getString(String value) {
        return getString(value, "");
    }

    public static String getString(String value, String defaultValue) {
        if (isEmpty(value)) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public static String getStringToArr(String arr, int key) {
        String str = "";
        if (arr.contains(":")) {
            String[] arrCi = arr.split(":");
            str = arrCi[key].trim();
        }

        return str;
    }
}
