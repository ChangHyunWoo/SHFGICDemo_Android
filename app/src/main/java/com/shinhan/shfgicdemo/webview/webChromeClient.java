package com.shinhan.shfgicdemo.webview;

import android.content.Context;
import android.content.DialogInterface;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.common.BaseActivity;
import com.shinhan.shfgicdemo.util.LogUtil;

public class webChromeClient extends WebChromeClient {
    private static final String TAG = webChromeClient.class.getName();

    private Context mContext;

    public webChromeClient(Context context) {
        this.mContext = context;
    }

    public void onProgressChanged(WebView view, int progress) {
        LogUtil.d(TAG, "onProgressChanged : " + progress);
        if (progress >= 70)
            ((BaseActivity) mContext).dismissProgressDialog();
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        LogUtil.d(TAG, "onJsAlert : " + url + " // " + message + " // " + result);
        ((BaseActivity) mContext).showAlertDialog("", message
                , mContext.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        //dialogInterface.dismiss();
                        result.confirm();
                    }
                });
        return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
        LogUtil.d(TAG, "onJsConfirm : " + url + " // " + message + " // " + result);
        return super.onJsConfirm(view, url, message, result);
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        LogUtil.e(TAG, "onConsoleMessage : " + consoleMessage.message() + '\n' + consoleMessage.messageLevel() + '\n' + consoleMessage.sourceId());
        return super.onConsoleMessage(consoleMessage);
    }
}
