package com.shinhan.shfgicdemo.webview;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.webkit.JavascriptInterface;

import com.shinhan.shfgicdemo.util.LogUtil;
import com.shinhan.shfgicdemo.view.join.InformationUseDetailActivity;
import com.shinhan.shfgicdemo.view.join.TermsDetailActivity;

public class webJSInterface {
    private static final String TAG = webJSInterface.class.getName();

    private Context mContext;
    private final Handler handler = new Handler();

    public webJSInterface(Context context) {
        this.mContext = context;
    }

    // 페이지 이동
    @JavascriptInterface
    public void goUrl(final int pType) {
        LogUtil.d(TAG, "goUrl : " + pType);
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (pType == 3) {
                    intent = new Intent(mContext, TermsDetailActivity.class);
                } else {
                    intent = new Intent(mContext, InformationUseDetailActivity.class);
                    intent.putExtra("pType", pType);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                mContext.startActivity(intent);
            }
        });
    }

}
