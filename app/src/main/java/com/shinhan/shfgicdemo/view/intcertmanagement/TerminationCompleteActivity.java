package com.shinhan.shfgicdemo.view.intcertmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.shinhan.shfgicdemo.MainActivity;
import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.common.BaseActivity;
import com.shinhan.shfgicdemo.util.LogUtil;
import com.shinhan.shfgicdemo.util.PreferenceUtil;
import com.shinhan.shfgicdemo.util.WebViewSettingUtil;
import com.shinhan.shfgicdemo.webview.webChromeClient;
import com.shinhan.shfgicdemo.webview.webJSInterface;

/**
 * 신한통합인증 해지 완료 화면
 * - 웹뷰
 */
public class TerminationCompleteActivity extends BaseActivity {
    private static final String TAG = TerminationCompleteActivity.class.getName();

    private ImageView btnTopBack, btnTopMenu;
    private TextView tvTopTitle;
    private WebView mWebView;
    private TextView btnCancel, btnConfirm;

    private boolean isLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        btnTopBack = findViewById(R.id.btnTopBack);
        btnTopMenu = findViewById(R.id.btnTopMenu);
        tvTopTitle = findViewById(R.id.tvTopTitle);

        tvTopTitle.setText(R.string.title_termination_complete);
        btnTopBack.setVisibility(View.INVISIBLE);
        btnTopMenu.setOnClickListener(this);

        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnCancel.setVisibility(View.GONE);
        btnConfirm.setOnClickListener(this);

        isLogin = getPreferenceUtil().getBoolean(PreferenceUtil.PREF_LOGIN);

        mWebView = findViewById(R.id.webView);
        WebViewSettingUtil.setWebSetting(mWebView);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new webChromeClient(this));
        mWebView.loadUrl(webUrl_termination_complete);
        showProgressDialog();
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        LogUtil.d(TAG, "onBackPressed ! ");
        //super.onBackPressed();
        return;
    }

    public void onClick(View v) {
        super.onClick(v);
        Intent intent;
        switch (v.getId()) {
            case R.id.btnConfirm:
                intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                break;
        }
    }
}
