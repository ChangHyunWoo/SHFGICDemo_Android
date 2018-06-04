package com.shinhan.shfgicdemo.view.join;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Toast;

import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.common.PasswordBaseActivity;
import com.shinhan.shfgicdemo.shfgic.SHFGIC;
import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;
import com.shinhan.shfgicdemo.util.LogUtil;
import com.shinhan.shfgicdemo.util.PreferenceUtil;
import com.shinhan.shfgicdemo.util.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 신한통합인증 가입시 비밀번호 재입력 화면
 * - FIDO 비밀번호 silent 인증 및 비밀번호 등록
 * - 보안키패드 (라온제공) 적용
 */
public class JoinPasswordReActivity extends PasswordBaseActivity {
    private static final String TAG = JoinPasswordReActivity.class.getName();

    private String mPinPwd_encrypted = null;
    private String mPinPwd_cipherEx = null;

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

        tvTopTitle.setText(R.string.title_password_re);

        SpannableStringBuilder spanStr = new SpannableStringBuilder(getString(R.string.passwordre_info));
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#3a7bd3"));
        spanStr.setSpan(colorSpan, 5, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPasswordMessage.setText(spanStr);

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

        //FIDO 비밀번호 silent 인증 및 비밀번호 등록 요청
        showProgressDialog();
        String pref_ci = getPreferenceUtil().getString(PreferenceUtil.PREF_CI);
        String ciName = StringUtil.getStringToArr(pref_ci, 0);
        String ci = StringUtil.getStringToArr(pref_ci, 1);
        getShfgic().registRequest(mSHFGICCallBack, ci, mPinPwd_encrypted, mEncryptedData, mRequestType, ciName);
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

            if (requestKey == SHFGICConfig.REQUEST_SHFGIC_REGIST) { //FIDO 비밀번호 silent 인증 후 비밀번호 등록 결과

                try {
                    JSONObject result = new JSONObject(msg);
                    String resultCode = result.getString(SHFGICConfig.RESULT_CODE);
                    String resultMsg = result.getString(SHFGICConfig.RESULT_MSG);
                    if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue())) {
                        JSONObject resultData = result.getJSONObject(SHFGICConfig.RESULT_DATA);
                        String trStatus = resultData.getString(SHFGICConfig.TR_STATUS);

                        if (trStatus.equals(SHFGICConfig.CodeTrStatus.COMPLETE.getValue())) {
                            String icId = result.getString(SHFGICConfig.IC_ID);

                            getPreferenceUtil().put(PreferenceUtil.PREF_SHFGIC_ICID, icId);
                            getPreferenceUtil().put(PreferenceUtil.PREF_SHFGIC_REG_TYPE, SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue());
                            getPreferenceUtil().put(PreferenceUtil.PREF_SHFGIC_LOGIN_TYPE, SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue());

                            String pref_shfgic_verify_type = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_VERIFY_TYPE);
                            Intent intent;
                            if (pref_shfgic_verify_type.equals(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue())) {
                                intent = new Intent(JoinPasswordReActivity.this, JoinFingerPrintActivity.class);
                                intent.putExtra(INTENT_KEY_REQUEST_TYPE, mRequestType);
                                intent.putExtra(INTENT_KEY_ICID, mIcId);
                            } else {
                                intent = new Intent(JoinPasswordReActivity.this, JoinCompleteActivity.class);
                            }
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
