package com.shinhan.shfgicdemo.view.intcertmanagement;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.common.BaseActivity;
import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;
import com.shinhan.shfgicdemo.util.PreferenceUtil;
import com.shinhan.shfgicdemo.view.certification.IntergratedCertificationActivity;

/**
 * 신한통합인증 로그인 설정 화면
 * - 비밀번호 / 지문 선택
 */
public class LoginOptionActivity extends BaseActivity {

    private final int SELECTED_TYPE_PASSWORD = 1;
    private final int SELECTED_TYPE_FINGER = 2;

    private ImageView btnTopBack, btnTopMenu;
    private TextView tvTopTitle;
    private TextView btnCancel, btnConfirm;

    private LinearLayout llPassword = null;
    private TextView tvPassword = null;
    private ImageView ivPassword = null;

    private LinearLayout llFinger = null;
    private TextView tvFinger = null;
    private ImageView ivFinger = null;

    private int initValue = -1;
    private int selectedValue = -1;
    private boolean isConfirmClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_option);

        btnTopBack = findViewById(R.id.btnTopBack);
        btnTopMenu = findViewById(R.id.btnTopMenu);
        tvTopTitle = findViewById(R.id.tvTopTitle);

        tvTopTitle.setText(R.string.title_login_option);
        btnTopBack.setOnClickListener(this);
        btnTopMenu.setOnClickListener(this);

        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

        llPassword = findViewById(R.id.option_password);
        tvPassword = findViewById(R.id.tvOptionPassword);
        ivPassword = findViewById(R.id.ivOptionPassword);

        llPassword.setOnClickListener(this);

        llFinger = findViewById(R.id.option_finger);
        tvFinger = findViewById(R.id.tvOptionFinger);
        ivFinger = findViewById(R.id.ivOptionFinger);

        llFinger.setOnClickListener(this);

        String pref_shfgic_verify_type = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_VERIFY_TYPE);
        if (null != pref_shfgic_verify_type) {
            if (!pref_shfgic_verify_type.equals(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue())) {
                getPreferenceUtil().put(PreferenceUtil.PREF_SHFGIC_LOGIN_TYPE, SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue());

                llFinger.setEnabled(false);
            }

            String pref_shfgic_login_type = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_LOGIN_TYPE);

            if (pref_shfgic_login_type.equals(SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue())) {
                setToggle(SELECTED_TYPE_PASSWORD);

                initValue = selectedValue = SELECTED_TYPE_PASSWORD;
            } else {
                setToggle(SELECTED_TYPE_FINGER);

                initValue = selectedValue = SELECTED_TYPE_FINGER;
            }
        }
    }

    public void onResume() {
        super.onResume();
    }

    public void onClick(View v) {
        super.onClick(v);

        switch (v.getId()) {
            case R.id.btnConfirm:
                isConfirmClick = true;
                if (initValue != selectedValue) {
                    if (SELECTED_TYPE_PASSWORD == selectedValue) {
                        showAlertDialog(null, getString(R.string.alert_changed_login_setting), null, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(LoginOptionActivity.this, IntergratedCertificationActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else if (SELECTED_TYPE_FINGER == selectedValue) {
                        String pref_shfgic_reg_type = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_REG_TYPE);

                        if (null != pref_shfgic_reg_type) {
                            if (pref_shfgic_reg_type.equals(SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue())) {  // 통합인증 지문등록 되어있지 않은 고객
                                showAlertDialog(null, getString(R.string.alert_changed_login_setting_fingerprint), null, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(LoginOptionActivity.this, PasswordConfirmActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        intent.putExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_CHANGE_LOGIN_SETTING_TO_FINGER);
                                        startActivityForResult(intent, INTENT_VALUE_MODE_CHANGE_LOGIN_SETTING_TO_FINGER);
                                    }
                                }, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        selectedValue = SELECTED_TYPE_PASSWORD;
                                        setToggle(SELECTED_TYPE_PASSWORD);
                                    }
                                });

                            } else {   // 통합인증 지문등록 되어있는 고객
                                showAlertDialog(null, getString(R.string.alert_changed_login_setting), null, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(LoginOptionActivity.this, IntergratedCertificationActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                        }
                    }
                } else
                    onBackPressed();
                break;

            case R.id.option_password:
                setToggle(SELECTED_TYPE_PASSWORD);
                selectedValue = SELECTED_TYPE_PASSWORD;
                break;

            case R.id.option_finger:
                setToggle(SELECTED_TYPE_FINGER);
                selectedValue = SELECTED_TYPE_FINGER;
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (isConfirmClick) {
            if (SELECTED_TYPE_PASSWORD == selectedValue)
                getPreferenceUtil().put(PreferenceUtil.PREF_SHFGIC_LOGIN_TYPE, SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue());
            else
                getPreferenceUtil().put(PreferenceUtil.PREF_SHFGIC_LOGIN_TYPE, SHFGICConfig.CodeFidoVerifyType.FINGER.getValue());
        }
    }

    @Override
    public void finish() {
        super.finish();

        if (isConfirmClick) {
            if (SELECTED_TYPE_PASSWORD == selectedValue)
                getPreferenceUtil().put(PreferenceUtil.PREF_SHFGIC_LOGIN_TYPE, SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue());
            else
                getPreferenceUtil().put(PreferenceUtil.PREF_SHFGIC_LOGIN_TYPE, SHFGICConfig.CodeFidoVerifyType.FINGER.getValue());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (INTENT_VALUE_MODE_CHANGE_LOGIN_SETTING_TO_FINGER == requestCode) {
            if (RESULT_OK == resultCode) {
                showAlertDialog(null, getString(R.string.alert_changed_login_setting_fingerprint_complete), null, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(LoginOptionActivity.this, IntergratedCertificationActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
            } else {
                selectedValue = SELECTED_TYPE_PASSWORD;
                setToggle(SELECTED_TYPE_PASSWORD);
            }
        }
    }

    private void setToggle(int selectedType) {
        if (SELECTED_TYPE_PASSWORD == selectedType) {
            tvPassword.setTextColor(Color.BLACK);
            ivPassword.setImageResource(R.drawable.radio_on);

            tvFinger.setTextColor(Color.GRAY);
            ivFinger.setImageResource(R.drawable.radio);
        } else {
            tvPassword.setTextColor(Color.GRAY);
            ivPassword.setImageResource(R.drawable.radio);

            tvFinger.setTextColor(Color.BLACK);
            ivFinger.setImageResource(R.drawable.radio_on);
        }
    }
}
