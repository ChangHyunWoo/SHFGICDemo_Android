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
import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;
import com.shinhan.shfgicdemo.util.WebViewSettingUtil;
import com.shinhan.shfgicdemo.webview.webChromeClient;
import com.shinhan.shfgicdemo.webview.webJSInterface;

/**
 * 신한통합인증 약관 및 이용동의 화면
 * - 웹뷰
 */
public class TermsActivity extends BaseActivity {

    private ImageView btnTopBack, btnTopMenu;
    private TextView tvTopTitle;
    private WebView mWebView;
    private TextView btnCancel, btnConfirm;

    private String mRequestType = SHFGICConfig.CodeRequestType.REGIST_NEW.getValue();
    private String mIcId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_KEY_REQUEST_TYPE)) {
            mRequestType = intent.getStringExtra(INTENT_KEY_REQUEST_TYPE);
        }

        if (intent.hasExtra(INTENT_KEY_ICID)) {
            mIcId = intent.getStringExtra(INTENT_KEY_ICID);
        }

        btnTopBack = findViewById(R.id.btnTopBack);
        btnTopMenu = findViewById(R.id.btnTopMenu);
        tvTopTitle = findViewById(R.id.tvTopTitle);

        tvTopTitle.setText(R.string.title_terms);
        btnTopBack.setOnClickListener(this);
        btnTopMenu.setOnClickListener(this);

        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

        mWebView = findViewById(R.id.webView);
        WebViewSettingUtil.setWebSetting(mWebView);

        mWebView.getSettings().setJavaScriptEnabled(true);
        //mWebView.addJavascriptInterface(new webJSInterface(this), "hybrid");
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new webChromeClient(this));
        mWebView.loadUrl(webUrl_terms);
        showProgressDialog();
    }

    public void onResume() {
        super.onResume();
    }

    public void onClick(View v) {
        super.onClick(v);
        Intent intent;
        switch (v.getId()) {
            case R.id.btnConfirm:
                //최초등록/재가입 인 경우 비밀번호 등록화면으로 이동
                if (mRequestType.equals(SHFGICConfig.CodeRequestType.REGIST_NEW.getValue())
                        || mRequestType.equals(SHFGICConfig.CodeRequestType.REGIST_REENTRANCE.getValue())) {
                    intent = new Intent(this, JoinPasswordActivity.class);
                } else {    //타사가입/재등록 인 경우 비밀번호 확인화면으로 이동
                    intent = new Intent(this, JoinPasswordConfirmActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(INTENT_KEY_REQUEST_TYPE, mRequestType);
                intent.putExtra(INTENT_KEY_ICID, mIcId);
                startActivity(intent);
                finish();
                break;
        }
    }
}
