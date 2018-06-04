package com.shinhan.shfgicdemo.view.join;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
 * 신한통합인증 등록시 비밀번호 확인 화면
 * - 타사 가입된 비밀번호 확인
 * - FIDO 비밀번호 silent 인증 및 비밀번호 등록
 * - 보안키패드 (라온제공) 적용
 */
public class JoinPasswordConfirmActivity extends PasswordBaseActivity {
    private static final String TAG = JoinPasswordConfirmActivity.class.getName();

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

        tvTopTitle.setText(R.string.title_password_confirm);

        layoutExplanationMessage.setVisibility(View.VISIBLE);

        //통합인증서 연동
        mShfgic = new SHFGIC(this);

        goTransKeyPad();
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

    /**
     * 보안 키패드 호출
     */
    public void goTransKeyPad() {
        onClick(ivPassword[0]);
    }

    @Override
    public void done(Intent data) {
        super.done(data);

        if (mDigitError) {
            mDigitError = false;
            return;
        }

        //FIDO 비밀번호 silent 인증 및 비밀번호 등록
        showProgressDialog();
        String pref_ci = getPreferenceUtil().getString(PreferenceUtil.PREF_CI);
        String ciName = StringUtil.getStringToArr(pref_ci, 0);
        String ci = StringUtil.getStringToArr(pref_ci, 1);
        getShfgic().registRequest(mSHFGICCallBack, ci, mEncryptedData, mEncryptedData, mIcId, mRequestType, ciName);
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

            switch (requestKey) {
                case SHFGICConfig.REQUEST_SHFGIC_REGIST: //FIDO 비밀번호 silent 인증 및 비밀번호 등록 결과

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
                                    intent = new Intent(JoinPasswordConfirmActivity.this, JoinFingerPrintActivity.class);
                                    intent.putExtra(INTENT_KEY_REQUEST_TYPE, mRequestType);
                                    intent.putExtra(INTENT_KEY_ICID, mIcId);
                                } else {
                                    intent = new Intent(JoinPasswordConfirmActivity.this, JoinCompleteActivity.class);
                                }
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish();

                                return;

                            } else {
                                resultMsg = resultData.getString(SHFGICConfig.TR_STATUS_MSG);
                            }
                        }
                        else if (resultCode.equals(SHFGICConfig.CodeResultCode.RP002.getValue()))  // 비밀번호 불일치시 error response "비밀번호가 일치하지 않습니다."
                        {

                            if (result.has(SHFGICConfig.IC_DATA)) {
                                JSONObject icData = result.getJSONObject(SHFGICConfig.IC_DATA);
                                int cntAuthFail = icData.getInt(SHFGICConfig.CNT_AUTH_FAIL);  //인증실패횟수
                                boolean lock = icData.getBoolean(SHFGICConfig.LOCK);    //계정잠금여부

                                //오류 횟수 초과로 잠금 상태인 경우
                                if (lock) {
                                    showAlertDialog(getString(R.string.alert_regist_lock), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    });
                                } else {    //오류 횟수 알람
                                    showAlertDialog(String.format(getString(R.string.alert_login_cnt_auth_fail), cntAuthFail), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            goTransKeyPad();
                                        }
                                    });
                                }

                                return;
                            }
                        }
                        goTransKeyPad();
                        showAlertDialog(resultMsg);

                    } catch (JSONException e) {
                        LogUtil.trace(e);
                    }

                    break;

            }
        }
    };
}
