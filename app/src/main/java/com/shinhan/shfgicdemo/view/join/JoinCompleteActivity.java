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
import com.shinhan.shfgicdemo.util.LogUtil;
import com.shinhan.shfgicdemo.util.WebViewSettingUtil;
import com.shinhan.shfgicdemo.view.certification.IntergratedCertificationActivity;
import com.shinhan.shfgicdemo.webview.webChromeClient;
import com.shinhan.shfgicdemo.webview.webJSInterface;


/**
 * 신한통합인증 가입완료 화면
 * - 웹뷰
 */
public class JoinCompleteActivity extends BaseActivity {
    private static final String TAG = JoinCompleteActivity.class.getName();

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

        tvTopTitle.setText(R.string.title_join_complete);
        btnTopBack.setVisibility(View.INVISIBLE);
        btnTopMenu.setOnClickListener(this);

        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel.setVisibility(View.GONE);
        btnConfirm.setOnClickListener(this);

        mWebView = findViewById(R.id.webView);
        WebViewSettingUtil.setWebSetting(mWebView);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new webChromeClient(this));
        mWebView.loadUrl(webUrl_join_complete);
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

        switch (v.getId()) {
            case R.id.btnConfirm:
                Intent intent = new Intent(this, IntergratedCertificationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                break;
        }
    }
}
