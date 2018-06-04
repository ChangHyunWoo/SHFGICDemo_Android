package com.shinhan.shfgicdemo.view.join;

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

public class TermsDetailActivity extends BaseActivity {

    private ImageView btnTopBack, btnTopMenu;
    private TextView tvTopTitle;
    private WebView mWebView;
    private TextView btnCancel, btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        btnTopBack = findViewById(R.id.btnTopBack);
        btnTopMenu = findViewById(R.id.btnTopMenu);
        tvTopTitle = findViewById(R.id.tvTopTitle);

        tvTopTitle.setText(R.string.title_terms);
        btnTopBack.setOnClickListener(this);
        btnTopMenu.setOnClickListener(this);

        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel.setVisibility(View.GONE);
        btnConfirm.setOnClickListener(this);

        mWebView = findViewById(R.id.webView);
        WebViewSettingUtil.setWebSetting(mWebView);

        mWebView.getSettings().setJavaScriptEnabled(true);
        //mWebView.addJavascriptInterface(new webJSInterface(this), "hybrid");
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new webChromeClient(this));
        mWebView.loadUrl(webUrl_terms_detail);
        showProgressDialog();
    }

    public void onResume() {
        super.onResume();
    }

    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btnConfirm:
                finish();
                break;
        }
    }
}
