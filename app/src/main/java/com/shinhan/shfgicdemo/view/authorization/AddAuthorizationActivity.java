package com.shinhan.shfgicdemo.view.authorization;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.common.BaseActivity;
import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;
import com.shinhan.shfgicdemo.view.intcertmanagement.PasswordActivity;
import com.shinhan.shfgicdemo.view.join.TermsActivity;


/**
 * 추가 본인인증 화면
 * - 각그룹사 공인인증서에 준하는 인증 방식 구현
 */
public class AddAuthorizationActivity extends BaseActivity {
    private static final String TAG = AddAuthorizationActivity.class.getName();

    private ImageView btnTopBack, btnTopMenu;
    private TextView tvTopTitle;
    private TextView btnCancel, btnConfirm;

    private String mRequestType = SHFGICConfig.CodeRequestType.REGIST_NEW.getValue();
    private String mIcId = "";
    private int mModeType = INTENT_VALUE_MODE_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_KEY_REQUEST_TYPE)) {
            mRequestType = intent.getStringExtra(INTENT_KEY_REQUEST_TYPE);
        }

        if (intent.hasExtra(INTENT_KEY_ICID)) {
            mIcId = intent.getStringExtra(INTENT_KEY_ICID);
        }

        if (intent.hasExtra(INTENT_KEY_MODE_TYPE))
            mModeType = intent.getIntExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_DEFAULT);

        btnTopBack = findViewById(R.id.btnTopBack);
        btnTopMenu = findViewById(R.id.btnTopMenu);
        tvTopTitle = findViewById(R.id.tvTopTitle);

        tvTopTitle.setText(R.string.title_auth_add);
        btnTopBack.setOnClickListener(this);
        btnTopMenu.setOnClickListener(this);

        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
    }

    public void onResume() {
        super.onResume();
    }

    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btnConfirm:
                Intent intent = null;

                switch (mModeType) {
                    case INTENT_VALUE_MODE_PASSWORD_FIND:
                        intent = new Intent(this, PasswordActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra(INTENT_KEY_MODE_TYPE, mModeType);
                        startActivity(intent);
                        finish();
                        break;

                    default:
                        intent = new Intent(this, TermsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra(INTENT_KEY_REQUEST_TYPE, mRequestType);
                        intent.putExtra(INTENT_KEY_ICID, mIcId);
                        startActivity(intent);
                        finish();
                        break;

                }
                break;
        }
    }
}
