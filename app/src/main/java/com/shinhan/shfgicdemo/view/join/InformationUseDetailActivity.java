package com.shinhan.shfgicdemo.view.join;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.common.BaseActivity;
import com.shinhan.shfgicdemo.util.WebViewSettingUtil;
import com.shinhan.shfgicdemo.webview.webChromeClient;
import com.shinhan.shfgicdemo.webview.webJSInterface;

public class InformationUseDetailActivity extends BaseActivity {
    private static final String TAG = InformationUseDetailActivity.class.getName();

    private TextView tvTopTitle;
    private ImageView btnTopClose;
    private WebView mWebView;
    private TextView btnConfirm;

    private int pType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_detail);

        Intent intent = getIntent();
        pType = intent.getIntExtra("pType", 1);

        tvTopTitle = findViewById(R.id.tvTopTitle);
        btnTopClose = findViewById(R.id.btnTopClose);
        btnTopClose.setOnClickListener(this);

        String topTitle = "", webViewUrl = "";
        switch (pType) {
            case 1: //지문 이용안내
                topTitle = getString(R.string.title_information_finger);
                webViewUrl = webUrl_use_finger;
                break;

            case 2: //비번 이용안내
                topTitle = getString(R.string.title_information_password);
                webViewUrl = webUrl_use_pw;
                break;
        }
        tvTopTitle.setText(topTitle);

        btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(this);

        mWebView = findViewById(R.id.webView);
        WebViewSettingUtil.setWebSetting(mWebView);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new webChromeClient(this));
        mWebView.loadUrl(webViewUrl);
        showProgressDialog();
    }

    public void onResume() {
        super.onResume();
    }

    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btnTopClose:
            case R.id.btnConfirm:
                finish();
                break;
        }
    }
}
