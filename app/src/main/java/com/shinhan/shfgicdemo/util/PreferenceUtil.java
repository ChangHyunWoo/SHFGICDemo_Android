package com.shinhan.shfgicdemo.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 *  - Created : preference class
 */
public class PreferenceUtil {
    private static final String TAG = PreferenceUtil.class.getName();

    private Context mContext;
    private SharedPreferences pref;

    private final String PREF_KEY_VERSION               = "PREF_SHFGIC_DEMO";
    public static final String PREF_CI                  = "PREF_CI";

    public static final String PREF_LOGIN               = "PREF_LOGIN";             //true, false
    public static final String PREF_LOGIN_TYPE          = "PREF_LOGIN_TYPE";        //LOGIN_SHFGIC
    public static final String PREF_LOGIN_VERIFYTYPE    = "PREF_LOGIN_VERIFYTYPE";  //통합인증 로그인 장치타입

    /* 통합인증 정보 */
    public static final String PREF_SHFGIC_ICID         = "PREF_SHFGIC_ICID";       //통합아이디
    public static final String PREF_SHFGIC_REG_TYPE     = "PREF_SHFGIC_REG_TYPE";   //통합인증 등록 장치타입 (비밀번호 or 지문)
    public static final String PREF_SHFGIC_LOGIN_TYPE   = "PREF_SHFGIC_LOGIN_TYPE"; // 신한통합인증센터 신한통합인증 로그인 설정 장치타입 (비밀번호 or 지문)

    public static final String PREF_SHFGIC_VERIFY_TYPE  = "PREF_SHFGIC_VERIFY_TYPE";//통합인증 등록가능 장치타입 (비밀번호 or 지문)


    /* 로그인 수단 - 통합인증 */
    public static final String LOGIN_SHFGIC = "LOGIN_SHFGIC";


    public PreferenceUtil(Context context) {
        mContext = context;
        pref = mContext.getSharedPreferences(PREF_KEY_VERSION, Context.MODE_PRIVATE);
    }

    public void put(String key, String value) {
        SharedPreferences.Editor editor = pref.edit();

        LogUtil.e(TAG, "Preferecne put : " + key + ", " + value);

        editor.putString(key, value);
        editor.commit();
    }

    public void put(String key, int value) {
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt(key, value);
        editor.commit();
    }

    public void put(String key, boolean value) {
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean(key, value);
        editor.commit();
    }

    public String getString(String key) {
        LogUtil.e(TAG, "Preferecne getString : " + key + ", " + pref.getString(key, null));
        return pref.getString(key, null);
    }

    public int getInt(String key) {
        return pref.getInt(key, 0);
    }

    public boolean getBoolean(String key) {
        return pref.getBoolean(key, false);
    }

    public void delete(String key) {
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(key);
        editor.commit();
    }
}