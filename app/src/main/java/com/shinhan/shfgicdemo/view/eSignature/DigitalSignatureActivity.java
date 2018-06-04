package com.shinhan.shfgicdemo.view.eSignature;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.raonsecure.oms.OMSFingerPrintManager;
import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.common.PasswordBaseActivity;
import com.shinhan.shfgicdemo.shfgic.SHFGIC;
import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;
import com.shinhan.shfgicdemo.util.LogUtil;
import com.shinhan.shfgicdemo.util.PreferenceUtil;
import com.shinhan.shfgicdemo.util.StringUtil;
import com.shinhan.shfgicdemo.view.authorization.AuthorizationActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigitalSignatureActivity extends PasswordBaseActivity {
    private static final String TAG = DigitalSignatureActivity.class.getName();

    private LinearLayout llESign = null;
    private LinearLayout llBtnFingerPrint = null;
    private TextView tvBtnFingerPrint_eSign = null;
    private TextView tvESign = null;

    private String mSvrTrChallenge = "타행이체\n(1)거래일자:2018.03.30\n(2)거래시간:15:45:14\n(3)출금계좌번호:123-45-678910" +
        "\n(4)입금은행:신한은행\n(5)입금계좌번호:11098765432101\n(6)받는분:홍길동\n(7)이체금액:200,000원\n(8)수수료:0원\n(9)받는통장 메모:3월점심값";
//    private String mSvrTrChallenge = "1234";
    private String mTransType = "3";

    private String mCertiMode = null;

    private String mIcId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvTopTitle.setText(R.string.title_esign);

        llESign = findViewById(R.id.layoutESign);
        llESign.setVisibility(View.VISIBLE);

        llBtnFingerPrint = findViewById(R.id.btnFingerPrint);
        llBtnFingerPrint.setOnClickListener(this);

        tvBtnFingerPrint_eSign = findViewById(R.id.tvBtnFingerPrint);
        tvBtnFingerPrint_eSign.setOnClickListener(this);
        tvBtnFingerPrint_eSign.setText(R.string.enter_fingerprint_esign);

        tvESign = findViewById(R.id.tvESign);
        tvESign.setText(mSvrTrChallenge);

        init();
    }

    private void init() {
        mIcId = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_ICID);
        String pref_shfgic_verify_type = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_VERIFY_TYPE);

        if (StringUtil.notNullString(pref_shfgic_verify_type).equals(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue()))
            llBtnFingerPrint.setVisibility(View.VISIBLE);
        else
            llBtnFingerPrint.setVisibility(View.GONE);
    }

    public void goFingerPrint() {
        //키패드 사라지게
        if (isViewCtrlKeypad == true) {
//            m_tkMngr.finishTransKey(false);
            m_tkMngr.clearKeypad();
            isViewCtrlKeypad = false;
        }

        OMSFingerPrintManager.SetHintColor(R.color.hint_color);
        OMSFingerPrintManager.SetFailColor(R.color.finger_fail_color);
        OMSFingerPrintManager.SetHintText(getString(R.string.fingerprint_hint));
        OMSFingerPrintManager.SetFailText(getString(R.string.fingerprint_not_recognized));

        showProgressDialog();
        getShfgic().authRequest2(mSHFGICCallBack, mIcId, mSvrTrChallenge, getSHA256Str(mSvrTrChallenge), mTransType, SHFGICConfig.CodeFidoVerifyType.FINGER.getValue(), SHFGICConfig.CodeRequestType.E_SIGN.getValue());

        mCertiMode = SHFGICConfig.CodeFidoVerifyType.FINGER.getValue();
    }

    /**
     * 보안 키패드 호출
     */
    public void goTransKeyPad() {
        onClick(ivPassword[0]);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        switch (v.getId()) {
            case R.id.btnConfirm_1:
                m_tkMngr.done();
                break;
            case R.id.btnFingerPrint:
            case R.id.tvBtnFingerPrint:
                goFingerPrint();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (isViewCtrlKeypad != true)
            setResult(RESULT_CANCELED);
    }

    @Override
    public void done(Intent data) {
        super.done(data);

        if (mDigitError) {
            mDigitError = false;
            return;
        }

        showProgressDialog();
        getShfgic().authRequest2(mSHFGICCallBack, mIcId, mSvrTrChallenge, getSHA256Str(mSvrTrChallenge), mTransType, SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue(), mEncryptedData, SHFGICConfig.CodeRequestType.E_SIGN.getValue());

        mCertiMode = SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue();
    }

    private String getSHA256Str(String plain) {

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(plain.getBytes());

            //return Base64.encodeToString(hash, Base64.DEFAULT);

            StringBuffer result = new StringBuffer();
            for (byte b : hash)
                result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            String strHash = result.toString();
            result = null;

            return strHash.substring(0, 64);

        } catch (NoSuchAlgorithmException e) {
            LogUtil.trace(e);
        }

        return "";
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
                        case SHFGICConfig.REQUEST_SHFGIC_DIGITALSIGN:  //인증 결과
                            showAlertDialog(resultMsg, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            });
                            break;
                    }

                    return;



                } else if (resultCode.equals(SHFGICConfig.CodeResultCode.AP001.getValue())) {  // 비밀번호 불일치시 error response "비밀번호가 일치하지 않습니다."

                    if (result.has(SHFGICConfig.IC_DATA)) {
                        JSONObject icData = result.getJSONObject(SHFGICConfig.IC_DATA);
                        int cntAuthFail = icData.getInt(SHFGICConfig.CNT_AUTH_FAIL);  //인증실패횟수
                        boolean lock = icData.getBoolean(SHFGICConfig.LOCK);    //계정잠금여부

                        //오류 횟수 초과로 잠금 상태인 경우
                        if (lock) {
                            showAlertDialog(null, getString(R.string.alert_login_lock), getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(DigitalSignatureActivity.this, AuthorizationActivity.class);
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

//                } else {
//                    if (null != mCertiMode) {
//                        if (mCertiMode.equals(SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue()))
//                            onClick(ivPassword[0]);
//                        else if (mCertiMode.equals(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue()))
//                            goFingerPrint();
//                    }
//                    else
//                        goFingerPrint();
                }

                if (!resultCode.equals(SHFGICConfig.CodeResultCode.F9003.getValue())) {   //지문 취소버튼 이벤트가 아닌 경우
                    showToast(DigitalSignatureActivity.this, resultMsg, Toast.LENGTH_SHORT);

                    if (null != mCertiMode) {
                        if (mCertiMode.equals(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue()))
                            goFingerPrint();
                        else
                            goTransKeyPad();
                    }
                }

            } catch (JSONException e) {
                LogUtil.trace(e);
            }
        }
    };
}
