package com.shinhan.shfgicdemo.shfgic;

import com.shinhan.shfgicdemo.util.LogUtil;

import java.net.CookieManager;
import java.util.HashMap;

/**
 * 통합인증 연동 설정정보
 */
public class SHFGICProperty {
    private static final String TAG = SHFGICProperty.class.getName();

    private HashMap<String, String> mProperty;

    private static SHFGICProperty mShicProperty;

    private static CookieManager mCookieManager = null;

    public static SHFGICProperty getInstance() {
        if (mShicProperty == null) {
            mShicProperty = new SHFGICProperty();
        }

        return mShicProperty;
    }

    private SHFGICProperty() {
        mProperty = null;
    }

    public void setProperty(HashMap<String, String> property) {
        mProperty = property;
    }

    //그룹사앱 설정 정보 가져오기
    public String getPropertyInfo(String key) {
        String str = mProperty.get(key);
        LogUtil.d(TAG, "getAppConfigInfo : " + str + " // " + mProperty);

        return str;
    }

    public static CookieManager getCookieManager() {
        if (null == mCookieManager)
            mCookieManager = new CookieManager();

        return mCookieManager;
    }
}
