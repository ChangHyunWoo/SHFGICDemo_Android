package com.shinhan.shfgicdemo.util;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.File;

public class WebViewSettingUtil {
    /**
     * setWebSetting 주로 사용하는 WebViewSetting을 셋팅한다.
     * <p>
     * Default : UserAgent = "" , UseCache = false
     * </p>
     *
     * @param webview
     * @see {@link #setWebSetting(WebView, boolean)}<br>
     * {@link #setWebSetting(WebView, String, boolean)}
     */
    public static void setWebSetting(@NonNull WebView webview) {
        setWebSetting(webview, "", false);
    }

    /**
     * setWebSetting 주로 사용하는 WebViewSetting을 셋팅한다.
     * <p>
     * Default : UserAgent = ""
     * </p>
     *
     * @param webview
     * @param useCache
     * @see {@link #setWebSetting(WebView)}<br>
     * {@link #setWebSetting(WebView, String, boolean)}
     */
    public static void setWebSetting(@NonNull WebView webview, boolean useCache) {
        setWebSetting(webview, "", useCache);
    }

    /**
     * setWebSetting 주로 사용하는 WebViewSetting을 셋팅한다.
     *
     * @param webview
     * @param userAgent
     * @param useCache
     * @see {@link #setWebSetting(WebView)}<br>
     * {@link #setWebSetting(WebView, boolean)}
     */
    @SuppressLint({"NewApi", "SetJavaScriptEnabled"})
    public static void setWebSetting(@NonNull WebView webview, String userAgent, boolean useCache) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        WebSettings webSettings = webview.getSettings();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            CookieManager.getInstance().setAcceptThirdPartyCookies(webview, true);

        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setSaveFormData(false);

        webSettings.setAllowFileAccess(false);

        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(false);

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
            webSettings.setDisplayZoomControls(false);

        if (useCache) {
            webSettings.setAppCacheEnabled(true);
            webSettings.setAppCachePath(Environment.getDataDirectory().getAbsolutePath() + File.separator + webview.getContext().getPackageName() + File.separator + ".cache");
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            webSettings.setAppCacheEnabled(false);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        }

        // M까지는 아직 지원되는듯 보이며 에러를 뱉어내지 않는다.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)
            webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        if (!StringUtil.isEmptyString(userAgent))
            webSettings.setUserAgentString(webSettings.getUserAgentString() + " " + userAgent);
    }

    public static void onDestroy(@NonNull WebView webview, String interfaceName) {
        if (webview != null) {
            webview.clearHistory();
            webview.clearFocus();
            webview.clearFormData();
            webview.clearAnimation();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && !StringUtil.isEmptyString(interfaceName)) {
                webview.removeJavascriptInterface(interfaceName);
            }
            webview.setWebChromeClient(null);
            webview.setWebViewClient(null);
            webview = null;
        }
    }

    public static void onClearCache(@NonNull WebView webview, boolean includeDiskFiles) {
        if (webview != null) {
            webview.clearCache(includeDiskFiles);
            webview.clearFormData();
            webview.clearHistory();
        }
    }

}
