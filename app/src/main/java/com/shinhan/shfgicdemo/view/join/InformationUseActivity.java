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
import com.shinhan.shfgicdemo.util.PreferenceUtil;
import com.shinhan.shfgicdemo.util.WebViewSettingUtil;
import com.shinhan.shfgicdemo.view.authorization.AddAuthorizationActivity;
import com.shinhan.shfgicdemo.view.authorization.AuthorizationActivity;
import com.shinhan.shfgicdemo.webview.webChromeClient;
import com.shinhan.shfgicdemo.webview.webJSInterface;

/**
 * 신한통합인증 이용안내 화면
 * - 웹뷰
 * - 지문 지원가능 단말여부 체크
 * - 로그인한 회원인 경우 (CI 값 보유, 신한통합인증 비로그인) 사용자 유효성 체크(가입여부 판단)
 */
public class InformationUseActivity extends BaseActivity {
    private static final String TAG = InformationUseActivity.class.getName();

    private ImageView btnTopBack, btnTopMenu;
    private TextView tvTopTitle;
    private WebView mWebView;
    private TextView btnCancel, btnConfirm;

    private boolean isLogin = false;
    private String loadUrl = "", mRequestType = "", mIcId = "";

    private int mModeType = INTENT_VALUE_MODE_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_KEY_MODE_TYPE))
            mModeType = intent.getIntExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_DEFAULT);

        btnTopBack = findViewById(R.id.btnTopBack);
        btnTopMenu = findViewById(R.id.btnTopMenu);
        tvTopTitle = findViewById(R.id.tvTopTitle);

        tvTopTitle.setText(R.string.title_information_use);
        btnTopBack.setOnClickListener(this);
        btnTopMenu.setOnClickListener(this);

        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnCancel.setVisibility(View.GONE);
        btnConfirm.setOnClickListener(this);

        isLogin = getPreferenceUtil().getBoolean(PreferenceUtil.PREF_LOGIN);
        btnConfirm.setText(R.string.shfgic_join);
        loadUrl = webUrl_use;

        mWebView = findViewById(R.id.webView);
        WebViewSettingUtil.setWebSetting(mWebView);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new webJSInterface(this), "hybrid");
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new webChromeClient(this));

        if (isLogin) {
            //로그인한 회원인 경우 (CI 값 보유, 신한통합인증 비로그인) 사용자 유효성 체크(가입여부 판단)
            mBaseRegistVerify(mModeType);
        } else {
            mWebView.loadUrl(loadUrl);
            showProgressDialog();
        }
    }

    //로그인한 회원 화면 처리
    public void loginWebViewCall(String requestType, String icId) {
        mRequestType = requestType;
        mIcId = icId;

        //등록/재등록 인 경우
        if (requestType.equals(SHFGICConfig.CodeRequestType.REGIST_ADD.getValue())
                || requestType.equals(SHFGICConfig.CodeRequestType.REGIST_REREGISTRATION.getValue())) {
            btnConfirm.setText(R.string.shfgic_regist);
            loadUrl = webUrl_use_regist;
        }

        mWebView.loadUrl(loadUrl);
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
                if (isLogin) {
                    intent = new Intent(this, AddAuthorizationActivity.class);
                    intent.putExtra(INTENT_KEY_REQUEST_TYPE, mRequestType);
                    intent.putExtra(INTENT_KEY_ICID, mIcId);
                } else {
                    intent = new Intent(this, AuthorizationActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
        }
    }
}
