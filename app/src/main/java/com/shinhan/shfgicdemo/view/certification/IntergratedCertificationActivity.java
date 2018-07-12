package com.shinhan.shfgicdemo.view.certification;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.ScaleXSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.raonsecure.oms.OMSFingerPrintManager;
import com.shinhan.shfgicdemo.MainActivity;
import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.shfgic.SHFGIC;
import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;
import com.shinhan.shfgicdemo.util.DateUtil;
import com.shinhan.shfgicdemo.util.LogUtil;
import com.shinhan.shfgicdemo.util.PreferenceUtil;
import com.shinhan.shfgicdemo.util.StringUtil;
import com.shinhan.shfgicdemo.view.authorization.AuthorizationActivity;
import com.shinhan.shfgicdemo.common.PasswordBaseActivity;
import com.shinhan.shfgicdemo.view.intcertmanagement.LoginOptionActivity;
import com.shinhan.shfgicdemo.view.intcertmanagement.PasswordConfirmActivity;
import com.shinhan.shfgicdemo.view.intcertmanagement.SuspensionInfoActivity;
import com.shinhan.shfgicdemo.view.intcertmanagement.TerminationInfoActivity;
import com.shinhan.shfgicdemo.view.join.InformationUseActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 로그인 화면
 * - 인증서 정보 상태 체크
 * - FIDO 비밀번호 silent 인증
 * - 보안키패드 (라온제공) 적용
 * - FIDO 지문 인증
 */
public class IntergratedCertificationActivity extends PasswordBaseActivity {
    private static final String TAG = IntergratedCertificationActivity.class.getName();

    private LinearLayout llCertCard = null;
    private LinearLayout btnFingerPrint = null;
    private LinearLayout btnFindPassword = null;
    private LinearLayout llBottom = null;
    private TextView btnConfirm = null;
    private TextView tvName, tvExpireDate;

    private int mModeType = INTENT_VALUE_MODE_DEFAULT;
    private String mLoginVerifyType = SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue();

    private boolean isVerifyCheck = false;
    private String mIcId = "";
    private String pref_shfgic_verify_type = "";
    private String pref_shfgic_login_type = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //logout();

        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_KEY_MODE_TYPE))
            mModeType = intent.getIntExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_DEFAULT);

        tvTopTitle.setText(R.string.title_intergrated_certification);

        btnFingerPrint = findViewById(R.id.btnFingerPrint);
        btnFingerPrint.setOnClickListener(this);

        btnConfirm = findViewById(R.id.btnConfirm_2);
        btnConfirm.setOnClickListener(this);

        llCertCard = findViewById(R.id.layoutCertcard);
        llCertCard.setVisibility(View.VISIBLE);
        llBottom = findViewById(R.id.layoutBottom_1);
        llBottom.setVisibility(View.GONE);
        llBottom = findViewById(R.id.layoutBottom_2);
        llBottom.setVisibility(View.VISIBLE);


        tvPasswordMessage = findViewById(R.id.passwordMessage);
        SpannableStringBuilder spanStr = new SpannableStringBuilder(getString(R.string.login_password_info));
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#3a7bd3"));
        spanStr.setSpan(colorSpan, 12, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanStr.setSpan(new ScaleXSpan(0.88f), 0, spanStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPasswordMessage.setText(spanStr);


        tvName = findViewById(R.id.tvName);
        tvExpireDate = findViewById(R.id.tvExpireDate);

        btnFindPassword = findViewById(R.id.btnFindPassoword);
        btnFindPassword.setOnClickListener(this);
        btnFindPassword.setVisibility(View.VISIBLE);

        pref_shfgic_verify_type = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_VERIFY_TYPE); //등록 가능 장치타입
        pref_shfgic_login_type = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_LOGIN_TYPE);   //통합인증 로그인 설정 장치타입
        mIcId = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_ICID);


        if (StringUtil.notNullString(pref_shfgic_verify_type).equals(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue())) {
            btnFingerPrint.setVisibility(View.VISIBLE);
        } else {
            btnFingerPrint.setVisibility(View.GONE);
        }

        //통합인증서 보유여부 및 인증서 상태 체크
        initVerifyCheck();
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.d(TAG, "onNewIntent");
        super.onNewIntent(intent);

        if (intent.hasExtra(INTENT_KEY_MODE_TYPE))
            mModeType = intent.getIntExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_DEFAULT);

        //통합인증서 보유여부 및 인증서 상태 체크
        initVerifyCheck();
    }

    /**
     * 통합인증서 보유여부 및 인증서 상태 체크
     */
    public void initVerifyCheck() {
        showProgressDialog();
        if (StringUtil.notNullString(pref_shfgic_login_type).equals(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue())) {
            if (INTENT_VALUE_MODE_AFFILIATED_CONCERN_JOIN == mModeType || INTENT_VALUE_MODE_AFFILIATED_CONCERN_LOGIN == mModeType)
                getShfgic().isSHFGIC(mSHFGICCallBack, false, SHFGICConfig.CodeRequestType.INQUIRE_CERTI.getValue());
            else
                getShfgic().isSHFGIC(mSHFGICCallBack, false, SHFGICConfig.CodeRequestType.AUTH_LOGIN.getValue());
        } else {
            if (INTENT_VALUE_MODE_AFFILIATED_CONCERN_JOIN == mModeType || INTENT_VALUE_MODE_AFFILIATED_CONCERN_LOGIN == mModeType)
                getShfgic().isSHFGIC(mSHFGICCallBack, SHFGICConfig.CodeRequestType.INQUIRE_CERTI.getValue());
            else
                getShfgic().isSHFGIC(mSHFGICCallBack, SHFGICConfig.CodeRequestType.AUTH_LOGIN.getValue());
        }
    }

    /**
     * 로그인 설정된 장치 체크
     */
    public void initVerifyType(boolean isLock, String trId) {
        isVerifyCheck = true;

        //비밀번호 오류 횟수 초과로 잠금 상태인 경우 체크
        if (isLock) {
            showAlertDialog(null, getString(R.string.alert_login_lock), getString(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(IntergratedCertificationActivity.this, AuthorizationActivity.class);
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
            return;
        }

        //통합인증 로그인 설정 장치타입에 따라 화면 설정
        if (StringUtil.notNullString(pref_shfgic_login_type).equals(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue())) {
            getShfgic().authRequestFido(mSHFGICCallBack, mIcId, trId);
        } else {
            goTransKeyPad();
        }
    }

    @Override
    public void onBackPressed() {
        LogUtil.d(TAG, "onBackPressed ! ");
        if (isViewCtrlKeypad == true) {
//            m_tkMngr.finishTransKey(false);
            m_tkMngr.clearKeypad();
            isViewCtrlKeypad = false;
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }

    public void onClick(View v) {
        super.onClick(v);
        Intent intent;
        switch (v.getId()) {
            case R.id.btnFingerPrint:   //지문인증
                goFingerPrint();
                break;

            case R.id.btnConfirm_2: //다른 로그인 수단
                intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                break;

            case R.id.btnFindPassoword: //비밀번호 찾기
                intent = new Intent(this, AuthorizationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_PASSWORD_FIND);
                startActivity(intent);
                finish();
                break;
        }
    }

    /**
     * 지문 인증 화면 호출
     **/
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

        //FIDO 인증 요청
        mLoginVerifyType = SHFGICConfig.CodeFidoVerifyType.FINGER.getValue();
        showProgressDialog();
        if (isVerifyCheck)
            getShfgic().authRequest(mSHFGICCallBack, mIcId, SHFGICConfig.CodeRequestType.AUTH_LOGIN.getValue());
        else
            getShfgic().authRequest(mSHFGICCallBack, mIcId);
    }

    /**
     * 보안 키패드 호출
     */
    public void goTransKeyPad() {
        mLoginVerifyType = SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue();
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
        getShfgic().authRequest(mSHFGICCallBack, mIcId, mEncryptedData, SHFGICConfig.CodeRequestType.AUTH_LOGIN.getValue());
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

                switch (requestKey) {

                    case SHFGICConfig.REQUEST_IS_SHFGIC:    //통합인증서 유무
                        if (!resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue())) {

                            showAlertDialog(null, getString(R.string.alert_login_unregist), getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent intent = new Intent(IntergratedCertificationActivity.this, InformationUseActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                            }, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent intent = new Intent(IntergratedCertificationActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        } else {
                            if (StringUtil.notNullString(pref_shfgic_login_type).equals(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue())) {
                                goFingerPrint();
                            }
                        }
                        break;

                    case SHFGICConfig.REQUEST_VERIFY_SHFGIC:        //인증서 정보 상태
                    case SHFGICConfig.REQUEST_SHFGIC_AUTH_VERIFY:   //예외사항 : 로그인시 verify 체크 (지문인증)
                        checkVerifySHFGIC(msg);
                        break;

                    case SHFGICConfig.REQUEST_SHFGIC_AUTH:  //인증 결과

                        if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue())) {
                            JSONObject resultData = result.getJSONObject(SHFGICConfig.RESULT_DATA);
                            String trStatus = resultData.getString(SHFGICConfig.TR_STATUS);

                            if (trStatus.equals(SHFGICConfig.CodeTrStatus.COMPLETE.getValue())) {

                                // 제휴사 연동
                                if (INTENT_VALUE_MODE_AFFILIATED_CONCERN_JOIN == mModeType || INTENT_VALUE_MODE_AFFILIATED_CONCERN_LOGIN == mModeType) {

                                    setResult(RESULT_OK);

                                    finish();

                                    return;
                                }

                                getPreferenceUtil().put(PreferenceUtil.PREF_LOGIN, true);
                                getPreferenceUtil().put(PreferenceUtil.PREF_LOGIN_TYPE, PreferenceUtil.LOGIN_SHFGIC);
                                getPreferenceUtil().put(PreferenceUtil.PREF_LOGIN_VERIFYTYPE, mLoginVerifyType);
                                showAlertDialog(R.string.shfgic_login_success, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent;
                                        switch (mModeType) {
                                            case INTENT_VALUE_MODE_SUSPENSION:  //서비스 정지
                                                intent = new Intent(IntergratedCertificationActivity.this, SuspensionInfoActivity.class);
                                                break;
                                            case INTENT_VALUE_MODE_TERMINATION:  //서비스 해지
                                                intent = new Intent(IntergratedCertificationActivity.this, TerminationInfoActivity.class);
                                                break;

                                            case INTENT_VALUE_MODE_CHANGE_LOGIN_SETTING:  //로그인 설정
                                                intent = new Intent(IntergratedCertificationActivity.this, LoginOptionActivity.class);
                                                break;

                                            case INTENT_VALUE_MODE_PASSWORD_RESET:  //비밀번호 재설정
                                                intent = new Intent(IntergratedCertificationActivity.this, PasswordConfirmActivity.class);
                                                intent.putExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_PASSWORD_RESET);
                                                break;

                                            default:
                                                intent = new Intent(IntergratedCertificationActivity.this, MainActivity.class);
                                                break;
                                        }
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                                return;

                            } else {
                                resultMsg = resultData.getString(SHFGICConfig.TR_STATUS_MSG);
                            }

                        } else {

                            if (INTENT_VALUE_MODE_AFFILIATED_CONCERN_JOIN == mModeType || INTENT_VALUE_MODE_AFFILIATED_CONCERN_LOGIN == mModeType) {
                                if (!resultCode.equals(SHFGICConfig.CodeResultCode.F9003.getValue())) {
                                    showToast(IntergratedCertificationActivity.this, resultMsg, Toast.LENGTH_SHORT);

                                    setResult(RESULT_CANCELED);

                                    finish();

                                    return;
                                }
                            }

                            if (resultCode.equals(SHFGICConfig.CodeResultCode.F240.getValue())) {   // 단말의 등록지문정보 변경 후 로그인 시 발생하는 error response
                                if (null != pref_shfgic_verify_type && pref_shfgic_verify_type.equals(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue())) {
                                    showAlertDialog(null, getString(R.string.alert_fingerprint_changed), null, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            Intent intent = new Intent(IntergratedCertificationActivity.this, PasswordConfirmActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                            intent.putExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_REGIST_FINGERPRINT_CHANGED);
                                            startActivityForResult(intent, INTENT_VALUE_MODE_REGIST_FINGERPRINT_CHANGED);
                                        }
                                    }, getString(R.string.cancel), null);

                                    return;
                                }
                            } else if (resultCode.equals(SHFGICConfig.CodeResultCode.F101.getValue()))  // 통합인증에 지문을 등록하지 않은 경우 발생하는 error response "등록된 인증장치가 없습니다."
                            {

                                if (null != pref_shfgic_verify_type && pref_shfgic_verify_type.equals(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue())) {
                                    showAlertDialog(null, getString(R.string.alert_changed_login_setting_fingerprint)
                                            , null, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent intent = new Intent(IntergratedCertificationActivity.this, PasswordConfirmActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                            intent.putExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_CHANGE_LOGIN_SETTING_TO_FINGER);
                                            startActivityForResult(intent, INTENT_VALUE_MODE_CHANGE_LOGIN_SETTING_TO_FINGER);
                                        }
                                    }, getString(R.string.cancel), null);

                                    return;
                                }
                            } else if (resultCode.equals(SHFGICConfig.CodeResultCode.F239.getValue()))  // 디바이스에 지문이 전혀 등록되지 않은 상태
                            {

                                if (null != pref_shfgic_verify_type && pref_shfgic_verify_type.equals(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue())) {
                                    showAlertDialog(null, getString(R.string.alert_fingerprint_unregist)
                                            , null, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    goTransKeyPad();
                                                }
                                            }, getString(R.string.cancel), null);
                                    return;
                                }
                            } else if (resultCode.equals(SHFGICConfig.CodeResultCode.AP001.getValue()))  // 비밀번호 불일치시 error response "비밀번호가 일치하지 않습니다."
                            {

                                if (result.has(SHFGICConfig.IC_DATA)) {
                                    JSONObject icData = result.getJSONObject(SHFGICConfig.IC_DATA);
                                    int cntAuthFail = icData.getInt(SHFGICConfig.CNT_AUTH_FAIL);  //인증실패횟수
                                    boolean lock = icData.getBoolean(SHFGICConfig.LOCK);    //계정잠금여부

                                    //오류 횟수 초과로 잠금 상태인 경우
                                    if (lock) {
                                        showAlertDialog(null, getString(R.string.alert_login_lock), getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent(IntergratedCertificationActivity.this, AuthorizationActivity.class);
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
                            } else {
                                goTransKeyPad();
                            }
                        }

                        if (!resultCode.equals(SHFGICConfig.CodeResultCode.F9003.getValue())) { //지문 취소버튼 이벤트가 아닌 경우
                            showToast(IntergratedCertificationActivity.this, resultMsg, Toast.LENGTH_SHORT);
                        }

                        break;
                }
            } catch (JSONException e) {
                LogUtil.trace(e);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            switch (requestCode) {
                case INTENT_VALUE_MODE_CHANGE_LOGIN_SETTING_TO_FINGER:
                    showAlertDialog(null, getString(R.string.alert_changed_login_setting_fingerprint_complete), null, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            goFingerPrint();
                        }
                    });
                    break;
                case INTENT_VALUE_MODE_REGIST_FINGERPRINT_CHANGED:
                    goFingerPrint();
                    break;
            }
        }
    }

    /**
     * 인증서 정보 상태 체크
     * - 미가입/해지/정지/만료일 체크
     *
     * @param msg
     */
    private void checkVerifySHFGIC(String msg) {

        String resultMsg = "", resourceAlert = "", trId = "";
        boolean isUnRegist = false, isLock = false;

        try {
            JSONObject result = new JSONObject(msg);
            String resultCode = result.getString(SHFGICConfig.RESULT_CODE);
            resultMsg = result.getString(SHFGICConfig.RESULT_MSG);
            if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue()) || resultCode.equals(SHFGICConfig.CodeResultCode.AS001.getValue())) {   //AS001 - 통합인증상태가 인증할 수 없는 상태, 인증서 유효여부 및 계정 Lock 여부
                String groupCode = getGroupCode();

                if (result.has(SHFGICConfig.RESULT_DATA)) {
                    JSONObject resultData = result.getJSONObject(SHFGICConfig.RESULT_DATA);
                    if (resultData.has(SHFGICConfig.TR_ID)) {
                        trId = resultData.getString(SHFGICConfig.TR_ID);
                    }
                }

                String stateCode = "";
                if (result.has(SHFGICConfig.IC_DATA)) {
                    JSONObject icData = result.getJSONObject(SHFGICConfig.IC_DATA);
                    stateCode = icData.getString(SHFGICConfig.STATE_CODE);  //인증서 상태코드
                    String expiryDate = icData.getString(SHFGICConfig.EXPIRY_DATE); //만료일
                    isLock = icData.getBoolean(SHFGICConfig.LOCK);    //계정잠금여부
                    String realNm = icData.getString(SHFGICConfig.REAL_NM);

                    //가입 여부 판단 (미가입 상태 - 가입)
                    if (StringUtil.isEmptyString(stateCode)) {
                        isUnRegist = true;
                        resourceAlert = getString(R.string.alert_login_unregist);

                    } else if (stateCode.equals(SHFGICConfig.CodeSHFGICState.NORMAL.getValue())) { //통합인증서 상태가 정상일 경우

                        //그룹사 가입여부 항목이 null 인경우 (예:블록체인 정보 못가져온 경우 .. )
                        String strAffiliatesCodes = icData.getString(SHFGICConfig.AFFILIATES_CODES);
                        if (StringUtil.isEmptyString(strAffiliatesCodes)) {
                            showAlertDialog(R.string.error_server, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(IntergratedCertificationActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                            return;

                        } else {

                            //그룹사 가입 여부 판단 (그룹사 미가입 상태 - 등록)
                            JSONObject affiliatesCodes = icData.getJSONObject(SHFGICConfig.AFFILIATES_CODES);
                            if (!affiliatesCodes.has(groupCode)) {
                                isUnRegist = true;
                                resourceAlert = getString(R.string.alert_login_reregist);

                            } else {
                                //그룹사 가입 상태
                                String affiliatesStateCode = affiliatesCodes.getString(groupCode);

                                //그룹사 가입 상태가 정상일 경우
                                if (affiliatesStateCode.equals(SHFGICConfig.CodeSHFGICState.NORMAL.getValue())) {

                                    if (expiryDate.length() == 8) {
                                        String strExpiryDate = DateUtil.getExpiryDate(expiryDate);

                                        tvName.setText(realNm);
                                        tvExpireDate.setText(String.format(getString(R.string.shfgic_expireDate), strExpiryDate));


                                        //만료일 체크
                                        String nowDate = DateUtil.getNowDate();
                                        if (Integer.parseInt(nowDate) > Integer.parseInt(expiryDate)) { //만료일 종료 (재가입)
                                            showAlertDialog(R.string.alert_login_expire_after, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent intent = new Intent(IntergratedCertificationActivity.this, InformationUseActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                    intent.putExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_EXPIRY_DATE_AFTER);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });

                                        } else {
                                            //만료일 1개월전 체크 (재가입)
                                            String expiryDateAdd = DateUtil.getDateMonthAdd(nowDate, 1);
                                            if (!StringUtil.isEmptyString(expiryDateAdd) && Integer.parseInt(expiryDateAdd) > Integer.parseInt(expiryDate)) {
                                                long expiryDays = DateUtil.getDiffDays(expiryDate);
                                                final boolean fIsLock = isLock;
                                                final String fTrId = trId;

                                                showAlertDialog(null, String.format(getString(R.string.alert_login_expire_before), expiryDays), getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Intent intent = new Intent(IntergratedCertificationActivity.this, InformationUseActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                        intent.putExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_EXPIRY_DATE_BEFORE);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        initVerifyType(fIsLock, fTrId);
                                                    }
                                                });

                                            } else {
                                                initVerifyType(isLock, trId);
                                            }

                                        }
                                        return;
                                    }

                                } else {    //그룹사 가입 상태가 정지 일 경우 (재등록)
                                    isUnRegist = true;
                                    resourceAlert = getString(R.string.alert_login_reregist);
                                }
                            }
                        }

                    } else {    //통합인증서 상태가 해지 일 경우 (재가입)
                        isUnRegist = true;
                        resourceAlert = getString(R.string.alert_login_termination);
                        checkTerminationSuspension();

                    }
                }
            }

        } catch (JSONException e) {
            LogUtil.trace(e);
        }

        //통합인증 미가입/해지/정지 상태
        if (isUnRegist) {

            //비밀번호 오류 횟수 초과로 잠금 상태인 경우 체크
            if (isLock) {
                showAlertDialog(null, getString(R.string.alert_login_lock), getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(IntergratedCertificationActivity.this, AuthorizationActivity.class);
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
                return;
            }

            showAlertDialog(null, resourceAlert, getString(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Intent intent = new Intent(IntergratedCertificationActivity.this, InformationUseActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
            }, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Intent intent = new Intent(IntergratedCertificationActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
            });

        } else {    //기타 오류 메세지
            showAlertDialog(resultMsg, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }

    }
}
