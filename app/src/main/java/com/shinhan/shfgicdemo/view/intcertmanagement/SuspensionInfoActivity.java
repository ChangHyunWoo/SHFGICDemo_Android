package com.shinhan.shfgicdemo.view.intcertmanagement;

import android.content.DialogInterface;
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
import com.shinhan.shfgicdemo.util.StringUtil;
import com.shinhan.shfgicdemo.util.WebViewSettingUtil;
import com.shinhan.shfgicdemo.view.certification.IntergratedCertificationActivity;
import com.shinhan.shfgicdemo.webview.webChromeClient;
import com.shinhan.shfgicdemo.webview.webJSInterface;

/**
 * 신한통합인증 정지 안내 화면
 * - 웹뷰
 */
public class SuspensionInfoActivity extends BaseActivity {

    private ImageView btnTopBack, btnTopMenu;
    private TextView tvTopTitle;
    private WebView mWebView;
    private TextView btnCancel, btnConfirm;

    private Boolean isShfgicLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        btnTopBack = findViewById(R.id.btnTopBack);
        btnTopMenu = findViewById(R.id.btnTopMenu);
        tvTopTitle = findViewById(R.id.tvTopTitle);

        tvTopTitle.setText(R.string.title_suspension_info);
        btnTopBack.setOnClickListener(this);
        btnTopMenu.setOnClickListener(this);

        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnCancel.setVisibility(View.GONE);
        btnConfirm.setText(R.string.title_suspension);
        btnConfirm.setOnClickListener(this);

        //신한통합인증 로그인여부
        isShfgicLogin = isLoginSHFGIC();

        mWebView = findViewById(R.id.webView);
        WebViewSettingUtil.setWebSetting(mWebView);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new webChromeClient(this));
        mWebView.loadUrl(webUrl_suspension_info);

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
                //앱 로그인여부 (각그룹사 로그인 수단 - 데모앱에서는 통합인증 만 적용)
                if (isShfgicLogin) {
                    //로그인한 수단으로 본인확인 절차 진행
                    String pref_login_verifyType = getPreferenceUtil().getString(PreferenceUtil.PREF_LOGIN_VERIFYTYPE);
                    if (StringUtil.notNullString(pref_login_verifyType).equals(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue())) {
                        intent = new Intent(this, FingerPrintActivity.class);
                    } else {
                        intent = new Intent(this, PasswordConfirmActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_SUSPENSION);
                    startActivity(intent);
                    finish();

                } else {
                    showAlertDialog(null, getString(R.string.alert_suspension_login), getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(SuspensionInfoActivity.this, IntergratedCertificationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_SUSPENSION);
                            startActivity(intent);
                            finish();
                        }
                    }, getString(R.string.cancel), null);
                }

                break;
        }
    }
}
