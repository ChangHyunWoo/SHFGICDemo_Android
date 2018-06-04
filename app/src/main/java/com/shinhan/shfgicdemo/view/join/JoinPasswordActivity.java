package com.shinhan.shfgicdemo.view.join;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Toast;

import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.common.PasswordBaseActivity;
import com.shinhan.shfgicdemo.shfgic.SHFGIC;
import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;
import com.shinhan.shfgicdemo.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 신한통합인증 가입시 비밀번호 입력 화면
 * - 비밀번호 유효성 체크(각사 정책에 따라 구현)
 * - 보안키패드 (라온제공) 적용
 */
public class JoinPasswordActivity extends PasswordBaseActivity {
    private static final String TAG = JoinPasswordActivity.class.getName();

    private String mRequestType = SHFGICConfig.CodeRequestType.REGIST_NEW.getValue();
    private String mIcId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_KEY_REQUEST_TYPE)) {
            mRequestType = intent.getStringExtra(INTENT_KEY_REQUEST_TYPE);
        }

        if (intent.hasExtra(INTENT_KEY_ICID)) {
            mIcId = intent.getStringExtra(INTENT_KEY_ICID);
        }

        tvTopTitle.setText(R.string.title_password);

        onClick(ivPassword[0]);
    }

    public void onClick(View v) {
        super.onClick(v);

        switch (v.getId()) {

            case R.id.btnConfirm_1:
                m_tkMngr.done();
                break;
        }
    }

    @Override
    public void done(Intent data) {
        super.done(data);

        if (mDigitError) {
            mDigitError = false;
            return;
        }

        showProgressDialog();

        //비밀번호 유효성 체크(각사 정책에 따라 구현)
        getShfgic().checkPinValidation(mSHFGICCallBack, mEncryptedData);
    }

    /**
     * 신한통합인증 콜백 함수
     */
    private SHFGIC.SHFGICCallBack mSHFGICCallBack = new SHFGIC.SHFGICCallBack() {
        @Override
        public void onSHFGICCallBack(int requestKey, String msg) {
            LogUtil.d(TAG, "onSHFGICCallBack = " + requestKey + " : " + msg);

            releaseShfgic();
            dismissProgressDialog();

            //비밀번호 유효성 체크(각사 정책에 따라 구현)
            if (requestKey == SHFGICConfig.REQUEST_FIDO_CHECK_PIN_VALIDATION) {
                try {
                    JSONObject result = new JSONObject(msg);

                    String resultCode = result.getString(SHFGICConfig.RESULT_CODE);
                    String resultMsg = result.getString(SHFGICConfig.RESULT_MSG);

                    if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue())) {
                        Intent intent;
                        intent = new Intent(JoinPasswordActivity.this, JoinPasswordReActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra(INTENT_KEY_REQUEST_TYPE, mRequestType);
                        intent.putExtra(INTENT_KEY_ICID, mIcId);
                        intent.putExtra(ENCRYPTED_PIN_DATA_KEY, mEncryptedData);
                        intent.putExtra(CIPHER_PIN_DATA_EX_KEY, mCipherDataEx);
                        startActivity(intent);
                        finish();
                    } else {
                        showToast(JoinPasswordActivity.this, resultMsg, Toast.LENGTH_SHORT);

                        onClick(ivPassword[0]);
                    }

                } catch (JSONException e) {
                    LogUtil.trace(e);
                }
            }

        }
    };
}