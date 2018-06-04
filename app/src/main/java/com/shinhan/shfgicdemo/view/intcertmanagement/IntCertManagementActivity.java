package com.shinhan.shfgicdemo.view.intcertmanagement;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.common.BaseActivity;
import com.shinhan.shfgicdemo.view.certification.IntergratedCertificationActivity;
import com.shinhan.shfgicdemo.view.join.InformationUseActivity;
import com.shinhan.shfgicdemo.view.pctoapp.PcToAppActivity;

/**
 *  신한통합인증센터 메뉴 화면
 */
public class IntCertManagementActivity extends BaseActivity {
    private static final String TAG = IntCertManagementActivity.class.getName();

    private ImageView btnTopBack, btnTopMenu;
    private TextView tvTopTitle;
    private LinearLayout btnIssued, btnTerminationOption, btnPwReset, btnLoginOption, btnPcToApp;

    private Boolean isShfgicLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intcert_management);

        btnTopBack = findViewById(R.id.btnTopBack);
        btnTopMenu = findViewById(R.id.btnTopMenu);
        tvTopTitle = findViewById(R.id.tvTopTitle);

        tvTopTitle.setText(R.string.title_management);
        btnTopBack.setOnClickListener(this);
        btnTopMenu.setOnClickListener(this);

        btnIssued = findViewById(R.id.btnIssued);
        btnTerminationOption = findViewById(R.id.btnTerminationOption);
        btnPwReset = findViewById(R.id.btnPwReset);
        btnLoginOption = findViewById(R.id.btnLoginOption);
        btnPcToApp = findViewById(R.id.btnPcToApp);

        btnIssued.setOnClickListener(this);
        btnTerminationOption.setOnClickListener(this);
        btnPwReset.setOnClickListener(this);
        btnLoginOption.setOnClickListener(this);
        btnPcToApp.setOnClickListener(this);
    }

    public void onResume() {
        super.onResume();

        //신한통합인증 로그인여부
        isShfgicLogin = isLoginSHFGIC();
    }

    public void onClick(View v) {
        super.onClick(v);

        Intent intent = null;

        switch (v.getId()) {
            case R.id.btnIssued:    //가입/등록
                if (isShfgicLogin) {
                    showAlertDialog(getString(R.string.alert_management_issue));
                } else {
                    intent = new Intent(this, InformationUseActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
                break;

            case R.id.btnTerminationOption: //해지/정지
                intent = new Intent(this, TerminationOptionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;

            case R.id.btnPwReset:   //비밀번호 재설정
                if (isShfgicLogin) {
                    intent = new Intent(this, PasswordConfirmActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_PASSWORD_RESET);
                    startActivity(intent);
                } else {
                    showAlertDialog(null, getString(R.string.alert_intergrated_certi_login), null, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Intent intent = new Intent(IntCertManagementActivity.this, IntergratedCertificationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_PASSWORD_RESET);
                            startActivity(intent);
                        }
                    }, getString(R.string.cancel), null);
                }

                break;

            case R.id.btnLoginOption:   //로그인 설정
                if (isShfgicLogin) {
                    intent = new Intent(this, LoginOptionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                } else {
                    showAlertDialog(null, getString(R.string.alert_changed_login_setting_login), null, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Intent intent = new Intent(IntCertManagementActivity.this, IntergratedCertificationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_CHANGE_LOGIN_SETTING);
                            startActivity(intent);
                        }
                    }, getString(R.string.cancel), null);
                }
                break;

            case R.id.btnPcToApp:   //Pc to App 인증
                intent = new Intent(this, PcToAppActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
        }
    }
}
