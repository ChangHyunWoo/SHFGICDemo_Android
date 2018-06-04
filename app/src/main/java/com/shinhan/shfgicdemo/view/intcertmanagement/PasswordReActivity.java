package com.shinhan.shfgicdemo.view.intcertmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.common.PasswordBaseActivity;
import com.shinhan.shfgicdemo.shfgic.SHFGIC;
import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;
import com.shinhan.shfgicdemo.util.LogUtil;
import com.shinhan.shfgicdemo.util.PreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 신한통합인증 비밀번호 재입력 화면
 * - 비밀번호 변경/분실 시
 * - FIDO 비밀번호 silent 인증 및 비밀번호 변경
 * - 보안키패드 (라온제공) 적용
 */
public class PasswordReActivity extends PasswordBaseActivity {
    private static final String TAG = PasswordReActivity.class.getName();

    private String mPinPwd_encrypted = null;
    private String mPinPwd_cipherEx = null;

    private int mModeType = INTENT_VALUE_MODE_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvTopTitle.setText(R.string.title_password_re);

        Intent intent = getIntent();

        if (intent.hasExtra(INTENT_KEY_MODE_TYPE))
            mModeType = intent.getIntExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_DEFAULT);

        Intent i = getIntent();
        mPinPwd_encrypted = i.getStringExtra(ENCRYPTED_PIN_DATA_KEY);
        mPinPwd_cipherEx = i.getStringExtra(CIPHER_PIN_DATA_EX_KEY);

        onClick(ivPassword[0]);
    }

    public void onResume() {
        super.onResume();
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

        if (null != mPinPwd_cipherEx && !mPinPwd_cipherEx.equals(mCipherDataEx)) {
            showToast(this, getString(R.string.password_not_identical), Toast.LENGTH_SHORT);

            onClick(ivPassword[0]);

            return;
        }

        switch (mModeType) {
            case INTENT_VALUE_MODE_PASSWORD_RESET:
            case INTENT_VALUE_MODE_PASSWORD_FIND:
                //FIDO 등록준비 요청(재등록)
                showProgressDialog();
                String icId = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_ICID);

                getShfgic().authRequest(mSHFGICCallBack, icId, mPinPwd_encrypted, mEncryptedData, SHFGICConfig.CodeRequestType.PASSWORD_RESET.getValue());
                break;
        }
    }

    /**
     * 신한통합인증 콜백 함수
     */
    private SHFGIC.SHFGICCallBack mSHFGICCallBack = new SHFGIC.SHFGICCallBack() {
        @Override
        public void onSHFGICCallBack(int requestKey, String msg) {
            LogUtil.d(TAG, "onSHICCallBack = " + requestKey + " : " + msg);

            releaseShfgic();
            dismissProgressDialog();

            if (requestKey == SHFGICConfig.REQUEST_SHFGIC_AUTH) { //FIDO 비밀번호 silent 인증 후 비밀번호 등록 결과

                try {
                    JSONObject result = new JSONObject(msg);
                    String resultCode = result.getString(SHFGICConfig.RESULT_CODE);
                    String resultMsg = result.getString(SHFGICConfig.RESULT_MSG);
                    if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue())) {
                        JSONObject resultData = result.getJSONObject(SHFGICConfig.RESULT_DATA);
                        String trStatus = resultData.getString(SHFGICConfig.TR_STATUS);

                        if (trStatus.equals(SHFGICConfig.CodeTrStatus.COMPLETE.getValue())) {

                            Intent intent = new Intent(PasswordReActivity.this, PWResetCompleteActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putExtra(INTENT_KEY_MODE_TYPE, mModeType);
                            startActivity(intent);
                            finish();

                            return;

                        } else {
                            resultMsg = resultData.getString(SHFGICConfig.TR_STATUS_MSG);
                        }
                    } else {
                        onClick(ivPassword[0]);
                    }

                    if (!resultCode.equals(SHFGICConfig.CodeResultCode.F9003.getValue())) //취소버튼 이벤트인 경우 알림 X
                        showAlertDialog(resultMsg);


                } catch (JSONException e) {
                    LogUtil.trace(e);
                }

            }
        }
    };
}
