package com.shinhan.shfgicdemo.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.shinhan.shfgicdemo.BuildConfig;
import com.shinhan.shfgicdemo.MainActivity;
import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.shfgic.SHFGIC;
import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;
import com.shinhan.shfgicdemo.util.LogUtil;
import com.shinhan.shfgicdemo.util.PreferenceUtil;
import com.shinhan.shfgicdemo.util.StringUtil;
import com.shinhan.shfgicdemo.view.authorization.AddAuthorizationActivity;
import com.shinhan.shfgicdemo.view.certification.IntergratedCertificationActivity;
import com.shinhan.shfgicdemo.view.intcertmanagement.SuspensionCompleteActivity;
import com.shinhan.shfgicdemo.view.intcertmanagement.TerminationCompleteActivity;
import com.shinhan.shfgicdemo.view.join.InformationUseActivity;
import com.shinhan.shfgicdemo.view.sso.SSOMainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_FIDO_ALLOWED_AUTHNR;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_VERIFY_SHFGIC;
import static com.shinhan.shfgicdemo.util.PreferenceUtil.PREF_LOGIN;
import static com.shinhan.shfgicdemo.util.PreferenceUtil.PREF_LOGIN_TYPE;
import static com.shinhan.shfgicdemo.util.PreferenceUtil.PREF_LOGIN_VERIFYTYPE;
import static com.shinhan.shfgicdemo.util.PreferenceUtil.PREF_SHFGIC_ICID;
import static com.shinhan.shfgicdemo.util.PreferenceUtil.PREF_SHFGIC_LOGIN_TYPE;
import static com.shinhan.shfgicdemo.util.PreferenceUtil.PREF_SHFGIC_REG_TYPE;
import static com.shinhan.shfgicdemo.util.PreferenceUtil.PREF_SHFGIC_VERIFY_TYPE;

/**
 * 공통 Activity
 */
public class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = BaseActivity.class.getName();

    //그룹사 코드 (BANK:은행:001, CARD:카드:002, INVESTMENT:금투:003, INSURANCE:생명:004) - 데모앱에서는 001/002 만 사용
    public String getGroupCode() {
        String groupCode = BuildConfig.GROUP_CODE;
        return groupCode;
    }

    //서버 연동 Url 설정
    private String getWebUrl() {
        //default : Group1
        String serverDomain = "http://1.237.181.61:8080";

        //Group2
        if (getGroupCode().equals(SHFGICConfig.CodeGroupCode.CARD.getValue())) {
            serverDomain = "http://1.237.181.65:8080";
        }
        return serverDomain;
    }

    /**
     * WebView Url - 각사에서 설정
     */
    private final String webUrl = getWebUrl() + "/shic/res/html";
    public final String webUrl_use_finger = webUrl + "/joi/joip0010.html";            //지문이용안내
    public final String webUrl_use_pw = webUrl + "/joi/joip0020.html";                //비밀번호이용안내
    public final String webUrl_use = webUrl + "/joi/joim0010.html";                   //신한통합인증 가입 이용안내
    public final String webUrl_use_regist = webUrl + "/joi/joim0011.html";            //신한통합인증 등록 이용안내
    public final String webUrl_terms = webUrl + "/joi/joim0020.html";                 //신한통합인증 약관 및 이용동의
    public final String webUrl_terms_detail = webUrl + "/joi/joim0030.html";          //신한통합인증 약관 및 이용동의
    public final String webUrl_join_complete = webUrl + "/joi/joim0040.html";         //신한통합인증 가입 완료
    public final String webUrl_suspension_info = webUrl + "/sto/stom0010.html";       //신한통합인증 정지 안내
    public final String webUrl_suspension_complete = webUrl + "/sto/stom0020.html";   //신한통합인증 정지 완료
    public final String webUrl_termination_info = webUrl + "/can/canm0010.html";      //신한통합인증 해지 안내
    public final String webUrl_termination_complete = webUrl + "/can/canm0020.html";  //신한통합인증 해지 완료

    public final String INTENT_KEY_MODE_TYPE = "modeType";
    public final int INTENT_VALUE_MODE_DEFAULT = 0;                         // default
    public final int INTENT_VALUE_MODE_TERMINATION = 1;                     // 해지
    public final int INTENT_VALUE_MODE_SUSPENSION = 2;                      // 정지
    public final int INTENT_VALUE_MODE_REGIST_FINGERPRINT_CHANGED = 3;      // 등록지문정보 변경으로 인한 재등록
    public final int INTENT_VALUE_MODE_PASSWORD_RESET = 4;                  // 통합인증서 비밀번호 재설정(통합인증으로 로그인하지 않은 고객)
    public final int INTENT_VALUE_MODE_PASSWORD_FIND = 5;                   // 통합인증 비밀번호 찾기
    public final int INTENT_VALUE_MODE_CHANGE_LOGIN_SETTING = 6;            // 로그인 설정
    public final int INTENT_VALUE_MODE_CHANGE_LOGIN_SETTING_TO_FINGER = 7;  // 로그인 설정을 지문으로 변경
    public final int INTENT_VALUE_MODE_EXPIRY_DATE_AFTER = 8;               // 만료일 종료로 인한 등록(재가입)
    public final int INTENT_VALUE_MODE_EXPIRY_DATE_BEFORE = 9;              // 만료일 이전 30일이내 등록 (재가입)
    public final int INTENT_VALUE_MODE_PC_TO_APP = 10;                      // PC to App 인증

    public final String INTENT_KEY_REQUEST_TYPE = "requestType";
    public final String INTENT_KEY_ICID = "icId";
    public final String INTENT_KEY_SVCTRID = "svcTrId";


    /**
     * 신한통합인증 로그인 여부
     **/
    public boolean isLoginSHFGIC() {
        boolean isLogin = getPreferenceUtil().getBoolean(PREF_LOGIN);
        String loginType = getPreferenceUtil().getString(PREF_LOGIN_TYPE);

        boolean isLoginShfgic = false;
        if (isLogin && StringUtil.notNullString(loginType).equals(PreferenceUtil.LOGIN_SHFGIC)) {
            isLoginShfgic = true;
        }

        return isLoginShfgic;
    }

    /**
     * 신한통합인증 로그아웃
     */
    public void logout() {
        getPreferenceUtil().put(PREF_LOGIN, false);
        getPreferenceUtil().put(PREF_LOGIN_TYPE, "");
        getPreferenceUtil().put(PREF_LOGIN_VERIFYTYPE, "");
    }


    public SHFGIC mShfgic;

    public SHFGIC getShfgic() {
        if (mShfgic == null) {
            WeakReference<SHFGIC> wr = new WeakReference(new SHFGIC(this));
            mShfgic = wr.get();
        }

        return mShfgic;
    }

    public void releaseShfgic() {
        if (mShfgic != null) {
            mShfgic = null;
        }
    }

    /**
     * 가입/등록시 사용자 유효성 체크(가입여부 판단)
     */
    private int mBaseModeType = INTENT_VALUE_MODE_DEFAULT;

    public void mBaseRegistVerify(int baseModeType) {
        mBaseModeType = baseModeType;
        mBaseFidoAllowedAuthnr();
    }

    /**
     * FIDO 지원가능 단말여부 확인 서버 호출
     */
    public void mBaseFidoAllowedAuthnr() {
        showProgressDialog();
        getShfgic().fidoAllowedAuthnr(mBaseSHFGICCallBack);
    }

    /**
     * 사용자 유효성 체크 서버 호출
     */
    public void mBaseRegistVerifyShfgic() {
        showProgressDialog();
        String ci = StringUtil.getStringToArr(getPreferenceUtil().getString(PreferenceUtil.PREF_CI), 1);
        getShfgic().verifySHFGIC(mBaseSHFGICCallBack, ci, SHFGICConfig.CodeRequestType.INQUIRE_CERTI.getValue());
    }

    /**
     * 신한통합인증 콜백 함수
     */
    private SHFGIC.SHFGICCallBack mBaseSHFGICCallBack = new SHFGIC.SHFGICCallBack() {
        @Override
        public void onSHFGICCallBack(int requestKey, String msg) {
            LogUtil.d(TAG, "mBaseSHFGICCallBack = " + requestKey + " : " + msg);

            releaseShfgic();
            dismissProgressDialog();

            switch (requestKey) {
                case REQUEST_FIDO_ALLOWED_AUTHNR:   //FIDO 지원가능 단말여부 확인

                    String shfgicVerifyType = SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue();
                    try {
                        JSONObject result = new JSONObject(msg);
                        String resultCode = result.getString(SHFGICConfig.RESULT_CODE);

                        if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue())) {
                            shfgicVerifyType = SHFGICConfig.CodeFidoVerifyType.FINGER.getValue();
                        }

                    } catch (JSONException e) {
                    }
                    getPreferenceUtil().put(PREF_SHFGIC_VERIFY_TYPE, shfgicVerifyType);
                    mBaseRegistVerifyShfgic();
                    break;

                case REQUEST_VERIFY_SHFGIC: //가입/등록시 사용자 유효성 체크(가입여부 판단)
                    checkRegistVerify(msg);
                    break;
            }

        }
    };


    /**
     * 가입/등록시 사용자 유효성 체크(가입여부 판단)
     *
     * @param msg
     */
    private void checkRegistVerify(String msg) {
        String resultMsg = "";
        String requestType = "", icId = "";
        boolean isRegist = false, isAlreadyRegist = false, isLock = false;
        try {
            JSONObject result = new JSONObject(msg);
            String resultCode = result.getString(SHFGICConfig.RESULT_CODE);
            resultMsg = result.getString(SHFGICConfig.RESULT_MSG);
            if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue())) {
                String groupCode = getGroupCode();

                String stateCode = "";
                if (result.has(SHFGICConfig.IC_DATA)) {
                    JSONObject icData = result.getJSONObject(SHFGICConfig.IC_DATA);
                    stateCode = icData.getString(SHFGICConfig.STATE_CODE);
                    icId = icData.getString(SHFGICConfig.IC_ID);
                    isLock = icData.getBoolean(SHFGICConfig.LOCK);    //계정잠금여부

                    //가입 여부 판단 (미가입 상태)
                    if (StringUtil.isEmptyString(stateCode)) {
                        isRegist = true;
                        requestType = SHFGICConfig.CodeRequestType.REGIST_NEW.getValue();

                    } else if (stateCode.equals(SHFGICConfig.CodeSHFGICState.NORMAL.getValue())) { //통합인증서 상태가 정상일 경우
                        resultMsg = getString(R.string.alert_join_regist);

                        //그룹사 가입 여부 판단 (그룹사 미가입 상태)
                        JSONObject affiliatesCodes = icData.getJSONObject(SHFGICConfig.AFFILIATES_CODES);
                        if (!affiliatesCodes.has(groupCode)) {
                            isRegist = true;
                            requestType = SHFGICConfig.CodeRequestType.REGIST_ADD.getValue();
                        } else {
                            //그룹사 가입 상태
                            String affiliatesStateCode = affiliatesCodes.getString(groupCode);

                            //그룹사 가입 상태가 정상일 경우
                            if (affiliatesStateCode.equals(SHFGICConfig.CodeSHFGICState.NORMAL.getValue())) {

                                //가입된 상태인데 통합ID 없는 경우 (재등록)
                                String pref_shfgic_icId = new PreferenceUtil(this).getString(PreferenceUtil.PREF_SHFGIC_ICID);
                                if (StringUtil.isEmpty(pref_shfgic_icId)) {
                                    isRegist = true;
                                    requestType = SHFGICConfig.CodeRequestType.REGIST_REREGISTRATION.getValue();

                                } else {
                                    switch (mBaseModeType) {
                                        case INTENT_VALUE_MODE_EXPIRY_DATE_AFTER:   //만료일 종료 (재가입)
                                        case INTENT_VALUE_MODE_EXPIRY_DATE_BEFORE:  //만료일 30일 이전 (재가입)
                                            isRegist = true;
                                            requestType = SHFGICConfig.CodeRequestType.REGIST_REENTRANCE.getValue();
                                            break;

                                        default: //이미 가입한 경우
                                            isAlreadyRegist = true;
                                            break;

                                    }
                                }

                            } else {    //그룹사 가입 상태가 정지 일 경우 (재등록)
                                isRegist = true;
                                requestType = SHFGICConfig.CodeRequestType.REGIST_REREGISTRATION.getValue();
                            }
                        }

                    } else {    //통합인증서 상태가 해지 일 경우 (재가입)
                        isRegist = true;
                        requestType = SHFGICConfig.CodeRequestType.REGIST_REENTRANCE.getValue();
                    }
                }
            }

        } catch (JSONException e) {
            LogUtil.trace(e);
        }

        //통합인증 가입/등록 처리
        if (isRegist) {
            //등록/재등록 인 경우 - 비밀번호 오류 횟수 초과로 잠금 상태인 경우 체크
            if (requestType.equals(SHFGICConfig.CodeRequestType.REGIST_ADD.getValue())
                    || requestType.equals(SHFGICConfig.CodeRequestType.REGIST_REREGISTRATION.getValue())) {
                if (isLock) {
                    showAlertDialog(getString(R.string.alert_regist_lock), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    return;
                }
            }


            if (this instanceof InformationUseActivity) {
                ((InformationUseActivity) this).loginWebViewCall(requestType, icId);
            } else {
                Intent intent = new Intent(this, AddAuthorizationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(INTENT_KEY_REQUEST_TYPE, requestType);
                intent.putExtra(INTENT_KEY_ICID, icId);
                startActivity(intent);
                finish();
            }

        } else {
            //이미 가입된 경우 처리
            if (isAlreadyRegist) {

                showAlertDialog(null, getString(R.string.alert_join_regist), getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(BaseActivity.this, IntergratedCertificationActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    }
                }, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(BaseActivity.this, MainActivity.class);
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


    /**
     * 신한통합인증 해지/정지 처리
     **/
    public void checkTerminationSuspension(int mModeType) {

        checkTerminationSuspension();

        Intent intent;
        if (mModeType == INTENT_VALUE_MODE_TERMINATION) { //해지처리
            intent = new Intent(this, TerminationCompleteActivity.class);
        } else { //정지처리
            intent = new Intent(this, SuspensionCompleteActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * 신한통합인증 해지/정지 처리
     **/
    public void checkTerminationSuspension() {
        getPreferenceUtil().put(PREF_LOGIN, false);
        getPreferenceUtil().put(PREF_LOGIN_TYPE, "");
        getPreferenceUtil().put(PREF_LOGIN_VERIFYTYPE, "");
        getPreferenceUtil().put(PREF_SHFGIC_ICID, "");
        getPreferenceUtil().put(PREF_SHFGIC_REG_TYPE, "");
        getPreferenceUtil().put(PREF_SHFGIC_LOGIN_TYPE, "");
    }


    private AlertDialog mAlertDialog;
    private AlertDialog.Builder mAlertDialogBuilder;
    private ProgressDialog mProgressDialog;
    private PreferenceUtil preferenceUtil;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Dialog Init
     */
    private void initializeAlertDialog() {
        if (mAlertDialogBuilder == null)
            mAlertDialogBuilder = new AlertDialog.Builder(this);
        if (mAlertDialog == null)
            mAlertDialog = mAlertDialogBuilder.create();
        if (mAlertDialog.isShowing())
            mAlertDialog.dismiss();
    }

    private void initializeProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this, R.style.AppProgressDialog);
        }
    }

    @Override
    protected void onDestroy() {
        LogUtil.d(TAG, "onDestroy ! ");

        if (mShfgic != null) {
            mShfgic = null;
        }

        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }

        if (mAlertDialogBuilder != null)
            mAlertDialogBuilder = null;

        super.onDestroy();
    }


    @Override
    protected void onResume() {
        LogUtil.d(TAG, "onResume ! ");
        super.onResume();
    }

    @Override
    protected void onPause() {
        LogUtil.d(TAG, "onPause ! ");
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        LogUtil.d(TAG, "onBackPressed ! ");
        super.onBackPressed();
    }

    public void onClick(View v) {
        Intent tIntent;
        switch (v.getId()) {
            case R.id.btnTopBack:   //상단 Back 버튼
            case R.id.btnCancel:    //취소 버튼
                onBackPressed();
                break;

            case R.id.btnTopMenu:   //상단 메뉴 버튼
                tIntent = new Intent(this, SSOMainActivity.class);
                tIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(tIntent);
                overridePendingTransition(R.anim.activity_slide_in, R.anim.activity_slide_out);
//                overridePendingTransition(R.anim.activity_slide_enter, R.anim.activity_slide_exit);
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }


    public PreferenceUtil getPreferenceUtil() {
        if (preferenceUtil == null) {
            preferenceUtil = new PreferenceUtil(this);
        }
        return preferenceUtil;
    }

    public void showAlertDialog(int resource) {
        if (resource > 0)
            this.showAlertDialog(getString(resource));
    }

    protected void showAlertDialog(int resource, DialogInterface.OnClickListener positiveListener) {
        if (resource > 0)
            this.showAlertDialog(getString(resource), positiveListener);

    }


    public void showAlertDialog(String message) {
        this.showAlertDialog("", message);
    }

    protected void showAlertDialog(String message, DialogInterface.OnClickListener positiveListener) {
        this.showAlertDialog("", message, positiveListener);
    }

    protected void showAlertDialog(String title, String message) {
        this.showAlertDialog(title, message, "");
    }

    protected void showAlertDialog(String title, String message, String positiveName) {
        this.showAlertDialog(title, message, positiveName, null);
    }

    protected void showAlertDialog(String title, String message, DialogInterface.OnClickListener positiveListener) {
        this.showAlertDialog(title, message, "", positiveListener);
    }

    public void showAlertDialog(String title, String message, String positiveName, DialogInterface.OnClickListener positiveListener) {
        this.showAlertDialog(title, message, positiveName, positiveListener, "", null);
    }


    protected void showAlertDialog(String title, String message, String positiveName, DialogInterface.OnClickListener positiveListener, String negativeName, DialogInterface.OnClickListener negativeListener) {
        mAlertDialogBuilder = null;
        mAlertDialog = null;

        if (!isFinishing()) {
            initializeAlertDialog();
            mAlertDialog.setTitle(title);
            mAlertDialog.setMessage(message);
            if (StringUtil.isEmptyString(positiveName))
                positiveName = getString(android.R.string.yes);
            mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveName, positiveListener);
            mAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, negativeName, negativeListener);

            mAlertDialog.setCanceledOnTouchOutside(false);
            mAlertDialog.setCancelable(false);

            mAlertDialog.show();
        }
    }

    public void dismissAlertDialog() {
        if (mAlertDialog.isShowing())
            mAlertDialog.dismiss();
    }

    public void showProgressDialog() {
        this.showProgressDialog(null);
    }

    protected void showProgressDialog(String message) {
        if (!isFinishing()) {
            initializeProgressDialog();
            if (mProgressDialog.isShowing())
                return;

            mProgressDialog.setCancelable(false);
//            if (!StringUtil.isEmptyString(message))
//                mProgressDialog.setMessage(message);
            mProgressDialog.show();
        }
    }

    protected ProgressDialog getProgressDialog() {
        if (!isFinishing()) {
            initializeProgressDialog();
            return mProgressDialog;
        }
        return null;
    }

    public void dismissProgressDialog() {
        if (!isFinishing() && mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void showToast(Context context, String message, int duration) {
        Toast t = Toast.makeText(context, message, duration);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }
}
