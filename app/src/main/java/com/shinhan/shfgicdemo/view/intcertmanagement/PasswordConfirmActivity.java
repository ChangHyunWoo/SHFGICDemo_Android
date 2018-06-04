package com.shinhan.shfgicdemo.view.intcertmanagement;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.raonsecure.touchen.onepass.sdk.OnePassManager;
import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.common.PasswordBaseActivity;
import com.shinhan.shfgicdemo.shfgic.SHFGIC;
import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;
import com.shinhan.shfgicdemo.util.LogUtil;
import com.shinhan.shfgicdemo.util.PreferenceUtil;
import com.shinhan.shfgicdemo.view.authorization.AuthorizationActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 신한통합인증 비밀번호 확인 화면
 * - 비밀번호 재설정 / 해지 / 정지 / 지문 변경/등록 시 사용
 * - 가입된 비밀번호 확인
 * - FIDO 비밀번호 silent 인증
 * - 보안키패드 (라온제공) 적용
 */
public class PasswordConfirmActivity extends PasswordBaseActivity {
    private static final String TAG = PasswordConfirmActivity.class.getName();

    private int mModeType = INTENT_VALUE_MODE_DEFAULT;
    private String mSvcTrId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvTopTitle.setText(R.string.title_password_confirm);

        Intent intent = getIntent();

        if (intent.hasExtra(INTENT_KEY_MODE_TYPE))
            mModeType = intent.getIntExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_DEFAULT);

        if (intent.hasExtra(INTENT_KEY_SVCTRID))
            mSvcTrId = intent.getStringExtra(INTENT_KEY_SVCTRID);

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (isViewCtrlKeypad != true)
            setResult(RESULT_CANCELED);
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

        //FIDO 인증 요청
        showProgressDialog();

        String icId = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_ICID);
        String requestType = "";

        switch (mModeType) {
            case INTENT_VALUE_MODE_PASSWORD_RESET:                  //비밀번호 재설정
            case INTENT_VALUE_MODE_REGIST_FINGERPRINT_CHANGED:      //지문 변경
            case INTENT_VALUE_MODE_CHANGE_LOGIN_SETTING_TO_FINGER:  //로그인 설정 지문
                if (mModeType == INTENT_VALUE_MODE_PASSWORD_RESET) {
                    requestType = SHFGICConfig.CodeRequestType.PASSWORD_AUTH.getValue();
                } else {
                    requestType = SHFGICConfig.CodeRequestType.AUTH_FINGER.getValue();
                }
                getShfgic().authRequest(mSHFGICCallBack, icId, mEncryptedData, requestType);
                break;

            case INTENT_VALUE_MODE_TERMINATION: //통합인증 해지
            case INTENT_VALUE_MODE_SUSPENSION:  //통합인증 정지
                if (mModeType == INTENT_VALUE_MODE_TERMINATION) {
                    requestType = SHFGICConfig.CodeRequestType.TERMINATION_I.getValue();
                } else {
                    requestType = SHFGICConfig.CodeRequestType.SUSPENSION_I.getValue();
                }
                getShfgic().unRegistRequest(mSHFGICCallBack, icId, mEncryptedData, requestType);
                break;

            case INTENT_VALUE_MODE_PC_TO_APP:   //PC to App 인증
                requestType = SHFGICConfig.CodeRequestType.AUTH_PCTOAPP.getValue();
                getShfgic().authRequestPcToApp(mSHFGICCallBack, icId, mEncryptedData, requestType, mSvcTrId);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            switch (requestCode) {
                case INTENT_VALUE_MODE_CHANGE_LOGIN_SETTING_TO_FINGER:
                case INTENT_VALUE_MODE_REGIST_FINGERPRINT_CHANGED:
                    setResult(RESULT_OK);
                    finish();
                    break;
            }
        }
    }

    private SHFGIC.SHFGICCallBack mSHFGICCallBack = new SHFGIC.SHFGICCallBack() {
        @Override
        public void onSHFGICCallBack(int requestKey, String msg) {
            LogUtil.d(TAG, "onSHICCallBack = " + requestKey + " : " + msg);

            releaseShfgic();
            dismissProgressDialog();

            try {
                JSONObject result = new JSONObject(msg);
                String resultCode = result.getString(SHFGICConfig.RESULT_CODE);
                String resultMsg = result.getString(SHFGICConfig.RESULT_MSG);

                if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue())) {
                    Intent intent = null;

                    switch (requestKey) {
                        case SHFGICConfig.REQUEST_SHFGIC_AUTH:  //인증 결과
                            switch (mModeType) {
                                case INTENT_VALUE_MODE_PASSWORD_RESET:
                                    intent = new Intent(PasswordConfirmActivity.this, PasswordActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    intent.putExtra(INTENT_KEY_MODE_TYPE, mModeType);
                                    startActivity(intent);
                                    break;
                                case INTENT_VALUE_MODE_CHANGE_LOGIN_SETTING_TO_FINGER:
                                case INTENT_VALUE_MODE_REGIST_FINGERPRINT_CHANGED:
                                    intent = new Intent(PasswordConfirmActivity.this, FingerPrintActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    intent.putExtra(INTENT_KEY_MODE_TYPE, mModeType);
                                    startActivityForResult(intent, mModeType);
                                    break;
                            }

                            return;

                        case SHFGICConfig.REQUEST_SHFGIC_UNREGIST:  //해지/정지 인 경우
                            checkTerminationSuspension(mModeType);
                            return;
                    }


                } else if (requestKey == SHFGICConfig.REQUEST_SHFGIC_AUTH_PCTOAPP
                        && resultCode.equals(OnePassManager.RESULT_OK + "")) {    //PC to App 인증

                    showAlertDialog(R.string.pctoapp_auth_success, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    return;


                } else if (resultCode.equals(SHFGICConfig.CodeResultCode.AP001.getValue())) {  // 비밀번호 불일치시 error response "비밀번호가 일치하지 않습니다."

                    if (result.has(SHFGICConfig.IC_DATA)) {
                        JSONObject icData = result.getJSONObject(SHFGICConfig.IC_DATA);
                        int cntAuthFail = icData.getInt(SHFGICConfig.CNT_AUTH_FAIL);  //인증실패횟수
                        boolean lock = icData.getBoolean(SHFGICConfig.LOCK);    //계정잠금여부

                        //오류 횟수 초과로 잠금 상태인 경우
                        if (lock) {
                            showAlertDialog(null, getString(R.string.alert_login_lock)
                                    , getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(PasswordConfirmActivity.this, AuthorizationActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                            intent.putExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_PASSWORD_FIND);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }, getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
                showToast(PasswordConfirmActivity.this, resultMsg, Toast.LENGTH_SHORT);

            } catch (JSONException e) {
                LogUtil.trace(e);
            }
        }
    };
}
