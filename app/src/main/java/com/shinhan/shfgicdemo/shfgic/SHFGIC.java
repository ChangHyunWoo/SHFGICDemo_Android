package com.shinhan.shfgicdemo.shfgic;

import android.content.Context;

import com.raonsecure.touchen.onepass.sdk.OnePassManager;
import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.shfgic.action.SHFGICAction;
import com.shinhan.shfgicdemo.shfgic.action.SHFGICFido;
import com.shinhan.shfgicdemo.util.LogUtil;
import com.shinhan.shfgicdemo.util.PreferenceUtil;
import com.shinhan.shfgicdemo.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_FIDO_ALLOWED_AUTHNR;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_FIDO_CHECK_DEVICE;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_FIDO_CHECK_PIN_VALIDATION;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_IS_SHFGIC;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_LIST_SHFGIC;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_AUTH_COMPLETE;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_AUTH_MAIN;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_AUTH_PCTOAPP_MAIN;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_AUTH_PCTOAPP_READY;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_AUTH_READY;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_AUTH_VERIFY_READY;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_DIGITALSIGN_COMPLETE;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_DIGITALSIGN_MAIN;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_DIGITALSIGN_READY;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_REGIST_COMPLETE;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_REGIST_MAIN;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_REGIST_READY;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_SSO;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_UNREGIST_COMPLETE;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_UNREGIST_MAIN;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_UNREGIST_READY;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_VERIFY_SSO_COMPLETE;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_VERIFY_SSO_MAIN;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_VERIFY_SHFGIC;
import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_SHFGIC_VERIFY_SSO_READY;

/**
 * 신한통합인증 그룹 앱과의 연동 설정
 * - 각사마다 Custom
 */
public class SHFGIC {
    private static final String TAG = SHFGIC.class.getName();

    public interface SHFGICCallBack {
        void onSHFGICCallBack(int requestKey, String resultMsg);
    }

    private SHFGICAction mShfgicAction;
    private SHFGICFido mShfgicFido;

    private Context mContext;
    private SHFGICCallBack mCallBack;
    private String mIcId = "";  //통합아이디

    private String mRequestType = "";
    private String mSrcDoc = "";

    public SHFGIC(Context context) {
        mContext = context;
    }

    private SHFGICAction getShfgicAction() {
        if (mShfgicAction == null) {
            WeakReference<SHFGICAction> wr = new WeakReference(new SHFGICAction(mContext));
            mShfgicAction = wr.get();
        }

        return mShfgicAction;
    }

    public SHFGICFido getShfgicFido() {
        if (mShfgicFido == null) {
            WeakReference<SHFGICFido> wr = new WeakReference(new SHFGICFido(mContext));
            mShfgicFido = wr.get();
        }

        return mShfgicFido;
    }


    /**
     * 서버 연동 메모리 해제
     */
    private void releaseAction() {
        if (mShfgicAction != null) {
            mShfgicAction = null;
        }
    }

    /**
     * FIDO 연동 메모리 해제
     */
    private void releaseFido() {
        if (mShfgicFido != null) {
            mShfgicFido.release();
            mShfgicFido = null;
        }
    }

    /**
     * 그룹사앱 정보 설정
     *
     * @param config - 그룹사앱 설정 정보
     */
    public void initProperty(HashMap<String, String> config) {
        LogUtil.e(TAG, "initProperty : " + config);

        config.put(SHFGICConfig.DEVICE_ID, OnePassManager.GetDeviceID(mContext));
        config.put(SHFGICConfig.APP_ID, OnePassManager.GetAppID(mContext));

        SHFGICProperty.getInstance().setProperty(config);
    }


    /**
     * 통합인증서 보유여부 확인 (통합ID 로컬저장 조회)
     *
     * @param callBack
     */
    public void isSHFGIC(SHFGICCallBack callBack, String requestType) {
        this.isSHFGIC(callBack, true, requestType);
    }

    /**
     * 통합인증서 보유여부 확인 (통합ID 로컬저장 조회)
     *
     * @param callBack
     * @param isVerify - 사용자 유효성을 체크할지 여부
     */
    public void isSHFGIC(SHFGICCallBack callBack, Boolean isVerify, String requestType) {
        mCallBack = callBack;

        String resultCode = SHFGICConfig.CodeResultCode.FAIL.getValue();
        String resultMsg = mContext.getString(R.string.resultmsg_icid_none);
        String pref_shfgic_icId = new PreferenceUtil(mContext).getString(PreferenceUtil.PREF_SHFGIC_ICID);
        if (!StringUtil.isEmpty(pref_shfgic_icId)) {
            resultCode = SHFGICConfig.CodeResultCode.SUCCESS.getValue();
            resultMsg = mContext.getString(R.string.resultmsg_icid);

            if (isVerify) {
                verifySHFGIC(callBack, "", pref_shfgic_icId, requestType);
                return;
            }
        }

        JSONObject result = new JSONObject();
        try {
            result.put(SHFGICConfig.RESULT_CODE, resultCode);
            result.put(SHFGICConfig.RESULT_MSG, resultMsg);
        } catch (JSONException e) {
            LogUtil.trace(e);
        }

        mCallBack.onSHFGICCallBack(REQUEST_IS_SHFGIC, result.toString());
    }


    /**
     * 통합인증 가입상태 조회(CI)
     *
     * @param callBack
     * @param ci
     * @param requestType
     */
    public void verifySHFGIC(SHFGICCallBack callBack, String ci, String requestType) {
        this.verifySHFGIC(callBack, ci, "", requestType);
    }

    /**
     * 통합인증 가입상태 조회(통합ID)
     *
     * @param callBack
     * @param ci
     * @param icId        - 통합아이디
     * @param requestType - 요청 업무구분 (SHFGICConfig.CodeRequestType 참조)
     */
    public void verifySHFGIC(SHFGICCallBack callBack, String ci, String icId, String requestType) {
        mCallBack = callBack;
        mRequestType = requestType;
        getShfgicAction().verifySHFGIC(mSHFGICActionCallBack, ci, icId, requestType);
    }

    /**
     * FIDO 지원가능 단말여부 확인
     * 1. Server - 허용단말 리스트 요청
     * 2. FIDO - 지원가능 단말여부 확인
     *
     * @param callBack
     */
    public void fidoAllowedAuthnr(SHFGICCallBack callBack) {
        mCallBack = callBack;
        getShfgicAction().fidoAllowedAuthnr(mSHFGICActionCallBack);
    }

    /**
     * 통합인증서 목록(통합ID)
     *
     * @param callBack
     * @param icId     - 통합아이디
     * @param isAll    - 인증서 해지 포함여부 (true, false)
     */
    public void listSHFGIC(SHFGICCallBack callBack, String icId, String isAll) {
        mCallBack = callBack;
        getShfgicAction().listSHFGIC(mSHFGICActionCallBack, icId, isAll);
    }


    /**
     * FIDO PIN번호 유효성 확인
     *
     * @param callBack
     * @param pinPwd
     */
    public void checkPinValidation(SHFGICCallBack callBack, String pinPwd) {
        mCallBack = callBack;
        getShfgicAction().fidoCheckPINValidation(mSHFGICActionCallBack, pinPwd);
    }

    /**
     * 비밀번호 등록요청 (신규등록)
     *
     * @param callBack
     * @param ci
     * @param pinPwd
     * @param pinPwdCheck
     * @param requestType
     * @param realNm
     */
    public void registRequest(SHFGICCallBack callBack, String ci, String pinPwd, String pinPwdCheck, String requestType, String realNm) {
        this.registRequest(callBack, ci, pinPwd, pinPwdCheck, mIcId, SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue(), requestType, realNm);
    }

    /**
     * 비밀번호 등록요청 (그룹사추가)
     *
     * @param callBack
     * @param ci
     * @param pinPwd
     * @param pinPwdCheck
     * @param icId
     * @param requestType
     * @param realNm
     */
    public void registRequest(SHFGICCallBack callBack, String ci, String pinPwd, String pinPwdCheck, String icId, String requestType, String realNm) {
        this.registRequest(callBack, ci, pinPwd, pinPwdCheck, icId, SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue(), requestType, realNm);
    }

    /**
     * 지문 등록요청
     *
     * @param callBack
     * @param ci
     * @param icId
     * @param requestType
     * @param realNm
     */
    public void registRequest(SHFGICCallBack callBack, String ci, String icId, String requestType, String realNm) {
        mIcId = icId;
        this.registRequest(callBack, ci, "", "", icId, SHFGICConfig.CodeFidoVerifyType.FINGER.getValue(), requestType, realNm);
    }

    /**
     * FIDO 등록요청
     * 1. Server - 등록준비요청
     * 2. FIDO - 등록요청
     * 3. Server - 등록완료요청
     *
     * @param callBack
     * @param ci
     * @param pinPwd
     * @param pinPwdCheck
     * @param icId
     * @param verifyType
     * @param requestType
     * @param realNm
     */
    public void registRequest(SHFGICCallBack callBack, String ci, String pinPwd, String pinPwdCheck, String icId, String verifyType, String requestType, String realNm) {
        mCallBack = callBack;
        mRequestType = requestType;
        getShfgicAction().fidoServiceRegist(mSHFGICActionCallBack, ci, pinPwd, pinPwdCheck, icId, verifyType, requestType, realNm);
    }


    /**
     * 지문 인증요청 - 예외사항 : 로그인시 verify 체크
     *
     * @param callBack
     * @param icId
     */
    public void authRequest(SHFGICCallBack callBack, String icId) {
        this.authRequest(callBack, icId, "", "", SHFGICConfig.CodeFidoVerifyType.FINGER.getValue(), SHFGICConfig.CodeRequestType.AUTH_LOGIN.getValue(), REQUEST_SHFGIC_AUTH_VERIFY_READY);
    }

    /**
     * 지문 인증요청 - 예외사항 : 로그인시 verify 체크 시 직접 FIDO 호출
     *
     * @param callBack
     * @param icId
     * @param trid
     */
    public void authRequestFido(SHFGICCallBack callBack, String icId, String trid) {
        mCallBack = callBack;
        mIcId = icId;
        mRequestType = SHFGICConfig.CodeRequestType.AUTH_LOGIN.getValue();
        getShfgicFido().fidoRequest(mSHFGICFidoCallBack, trid, SHFGICConfig.REQUEST_SHFGIC_AUTH_MAIN);
    }


    /**
     * 비밀번호 인증요청
     *
     * @param callBack
     * @param icId
     * @param pinPwd
     * @param requestType
     */
    public void authRequest(SHFGICCallBack callBack, String icId, String pinPwd, String requestType) {
        this.authRequest(callBack, icId, pinPwd, "", SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue(), requestType, SHFGICConfig.REQUEST_SHFGIC_AUTH_READY);
    }

    /**
     * 지문 인증요청
     *
     * @param callBack
     * @param icId
     * @param requestType
     */
    public void authRequest(SHFGICCallBack callBack, String icId, String requestType) {
        this.authRequest(callBack, icId, "", "", SHFGICConfig.CodeFidoVerifyType.FINGER.getValue(), requestType, SHFGICConfig.REQUEST_SHFGIC_AUTH_READY);
    }

    /**
     * 비밀번호 변경시
     *
     * @param callBack
     * @param icId
     * @param pinPwd
     * @param pinPwdCheck
     * @param requestType
     */
    public void authRequest(SHFGICCallBack callBack, String icId, String pinPwd, String pinPwdCheck, String requestType) {
        this.authRequest(callBack, icId, pinPwd, pinPwdCheck, SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue(), requestType, SHFGICConfig.REQUEST_SHFGIC_AUTH_READY);
    }

    /**
     * FIDO 인증요청
     * 1. Server - 인증준비요청
     * 2. FIDO - 인증요청
     * 3. Server - 인증완료요청
     *
     * @param callBack
     * @param icId
     * @param pinPwd
     * @param pinPwdCheck
     * @param verifyType
     * @param requestType
     * @param requestKeyCode
     */
    public void authRequest(SHFGICCallBack callBack, String icId, String pinPwd, String pinPwdCheck, String verifyType, String requestType, int requestKeyCode) {
        mCallBack = callBack;
        mIcId = icId;
        mRequestType = requestType;
        getShfgicAction().fidoServiceAuth(mSHFGICActionCallBack, icId, pinPwd, pinPwdCheck, verifyType, requestType, requestKeyCode);
    }

    /**
     * FIDO 전자서명 준비(지문)
     *
     * @param callBack
     * @param icId
     * @param srcDoc
     * @param svcTrChallenge
     * @param transType
     * @param verifyType
     * @param requestType
     */
    public void authRequest2(SHFGICCallBack callBack, String icId, String srcDoc, String svcTrChallenge, String transType, String verifyType, String requestType) {
        this.authRequest2(callBack, icId, srcDoc, svcTrChallenge, transType, verifyType, "", requestType);
    }

    /**
     * FIDO 전자서명 준비(PIN번호)
     *
     * @param callBack
     * @param icId
     * @param srcDoc
     * @param svcTrChallenge
     * @param transType
     * @param verifyType
     * @param pinPwd
     * @param requestType
     */
    public void authRequest2(SHFGICCallBack callBack, String icId, String srcDoc, String svcTrChallenge, String transType, String verifyType, String pinPwd, String requestType) {
        mCallBack = callBack;
        mIcId = icId;
        mSrcDoc = srcDoc;
        mRequestType = requestType;
        getShfgicAction().fidoServiceAuth2(mSHFGICActionCallBack, icId, svcTrChallenge, transType, verifyType, pinPwd, requestType);
    }

    /**
     * FIDO 해지/정지 요청 - 비밀번호
     *
     * @param callBack
     * @param icId
     * @param pinPwd
     * @param requestType
     */
    public void unRegistRequest(SHFGICCallBack callBack, String icId, String pinPwd, String requestType) {
        this.unRegistRequest(callBack, icId, pinPwd, SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue(), requestType);
    }

    /**
     * FIDO 해지/정지 요청 - 지문
     *
     * @param callBack
     * @param icId
     * @param requestType
     */
    public void unRegistRequest(SHFGICCallBack callBack, String icId, String requestType) {
        this.unRegistRequest(callBack, icId, "", SHFGICConfig.CodeFidoVerifyType.FINGER.getValue(), requestType);
    }

    /**
     * FIDO 해지/정지 요청
     * 1. Server - 해지/정지 준비요청
     * 2. FIDO - 해지/정지 인증요청
     * 3. Server - 해지/정지 완료요청
     *
     * @param callBack
     * @param icId
     * @param pinPwd
     * @param verifyType
     * @param requestType - 해지/정지여부
     */
    public void unRegistRequest(SHFGICCallBack callBack, String icId, String pinPwd, String verifyType, String requestType) {
        mCallBack = callBack;
        mIcId = icId;
        mRequestType = requestType;
        getShfgicAction().fidoServiceUnRegist(mSHFGICActionCallBack, icId, pinPwd, verifyType, requestType);
    }

    public void requestSSO(SHFGICCallBack callBack, String icId) {
        mCallBack = callBack;
        getShfgicAction().requestSSO(mSHFGICActionCallBack, icId);
    }

    public void verifySSO(SHFGICCallBack callBack, String icId, String affiliatesCode, String ssoData) {
        mCallBack = callBack;
        mIcId = icId;
        mRequestType = SHFGICConfig.CodeRequestType.AUTH_SSO.getValue();
        getShfgicAction().verifySSO(mSHFGICActionCallBack, icId, affiliatesCode, ssoData);
    }

    public void requestSsoFido(SHFGICCallBack callBack, String icId, String trid) {
        mCallBack = callBack;
        mIcId = icId;
        mRequestType = SHFGICConfig.CodeRequestType.AUTH_LOGIN.getValue();
        getShfgicFido().fidoRequest(mSHFGICFidoCallBack, trid, SHFGICConfig.REQUEST_SHFGIC_VERIFY_SSO_MAIN);
    }

    /**
     * FIDO Pc To App 인증 요청 - 비밀번호
     *
     * @param callBack
     * @param icId
     * @param pinPwd
     * @param requestType
     * @param svcTrId
     */
    public void authRequestPcToApp(SHFGICCallBack callBack, String icId, String pinPwd, String requestType, String svcTrId) {
        this.authRequestPcToApp(callBack, icId, pinPwd, SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue(), requestType, svcTrId);
    }

    /**
     * FIDO Pc To App 인증 요청 - 지문
     *
     * @param callBack
     * @param icId
     * @param requestType
     * @param svcTrId
     */
    public void authRequestPcToApp(SHFGICCallBack callBack, String icId, String requestType, String svcTrId) {
        this.authRequestPcToApp(callBack, icId, "", SHFGICConfig.CodeFidoVerifyType.FINGER.getValue(), requestType, svcTrId);
    }

    /**
     * FIDO Pc To App 인증 요청
     * 1. Server - 인증준비요청
     * 2. FIDO - 인증요청
     *
     * @param callBack
     * @param icId
     * @param pinPwd
     * @param verifyType
     * @param requestType
     * @param svcTrId
     */
    public void authRequestPcToApp(SHFGICCallBack callBack, String icId, String pinPwd, String verifyType, String requestType, String svcTrId) {
        mCallBack = callBack;
        mIcId = icId;
        mRequestType = requestType;
        getShfgicAction().fidoServiceAuthPcToApp(mSHFGICActionCallBack, icId, pinPwd, verifyType, requestType, svcTrId);
    }

    /**
     * 서버 콜백 함수
     */
    private SHFGICAction.SHFGICActionCallBack mSHFGICActionCallBack = new SHFGICAction.SHFGICActionCallBack() {
        @Override
        public void onSHFGICActionCallBack(int requestKey, String msg) {
            LogUtil.d(TAG, "SHFGICActionCallBack = " + requestKey + " : " + msg);
            String resultCode;

            switch (requestKey) {
                case REQUEST_VERIFY_SHFGIC:                    //사용자 유효성
                case REQUEST_LIST_SHFGIC:                      //인증서 목록
                case REQUEST_FIDO_CHECK_PIN_VALIDATION:        //PIN번호 유효성 확인
                case REQUEST_SHFGIC_SSO:
                    mCallBack.onSHFGICCallBack(requestKey, msg);
                    break;

                case REQUEST_FIDO_ALLOWED_AUTHNR:    //허용 단말 리스트 요청
                    String[] arrAAID = null;
                    try {
                        JSONObject result = new JSONObject(msg);
                        resultCode = result.getString(SHFGICConfig.RESULT_CODE);
                        if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue())) {
                            JSONObject resultData = result.getJSONObject(SHFGICConfig.RESULT_DATA);
                            JSONArray aaidAllowList = resultData.getJSONArray(SHFGICConfig.AAID_ALLOW_LIST);

                            //지문 인식 가능 단말 여부 체크 (silent 인증은 4.1이상 지원)
                            arrAAID = new String[aaidAllowList.length()];
                            int k = 0;
                            for (int i = 0; i < aaidAllowList.length(); i++) {
                                String aaid = aaidAllowList.getJSONObject(i).getString(SHFGICConfig.AAID);
                                LogUtil.d(TAG, "SHFGICActionCallBack aaid : " + aaid);
                                arrAAID[i] = aaid;
//                                if (aaid.equals("0012#0021")) {
//                                    arrAAID = new String[k+1];
//                                    arrAAID[k] = aaid;
//                                    k++;
//                                }
                            }

                        }

                    } catch (JSONException e) {
                        LogUtil.trace(e);
                    }

                    LogUtil.d(TAG, "SHFGICActionCallBack arrAAID : " + arrAAID);
                    if (arrAAID != null && arrAAID.length > 0) {
                        getShfgicFido().isSupportedDevice(mSHFGICFidoCallBack, arrAAID);
                    } else {    //오류
                        mCallBack.onSHFGICCallBack(REQUEST_FIDO_ALLOWED_AUTHNR, msg);
                    }
                    break;

                case REQUEST_SHFGIC_REGIST_READY:       //FIDO 등록준비 요청
                case REQUEST_SHFGIC_AUTH_READY:         //FIDO 인증준비 요청
                case REQUEST_SHFGIC_AUTH_VERIFY_READY:  //FIDO 인증준비 요청 - 예외사항 : 로그인시 verify 체크 (지문인증)
                case REQUEST_SHFGIC_UNREGIST_READY:     //FIDO 해지/정지 준비 요청
                case REQUEST_SHFGIC_DIGITALSIGN_READY:  //FIDO 전자서명 준비 요청
                case REQUEST_SHFGIC_AUTH_PCTOAPP_READY: //FIDO PCtoApp 인증 준비 요청
                case REQUEST_SHFGIC_VERIFY_SSO_READY:
                    String trid = "";
                    try {
                        JSONObject result = new JSONObject(msg);
                        resultCode = result.getString(SHFGICConfig.RESULT_CODE);
                        if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue())) {
                            JSONObject resultData = result.getJSONObject(SHFGICConfig.RESULT_DATA);
                            trid = resultData.getString(SHFGICConfig.TR_ID);

                            if (requestKey == REQUEST_SHFGIC_REGIST_READY)
                                mIcId = resultData.getString(SHFGICConfig.IC_ID);

                        }
                    } catch (JSONException e) {
                        LogUtil.trace(e);
                    }

                    LogUtil.d(TAG, "SHFGICActionCallBack trid : " + trid);
                    if (!StringUtil.isEmptyString(trid) && requestKey != REQUEST_SHFGIC_AUTH_VERIFY_READY) {
                        getShfgicFido().fidoRequest(mSHFGICFidoCallBack, trid, SHFGICConfig.getMainRequestKey(requestKey));    //인증수단 등록
                    } else {    //오류
                        mCallBack.onSHFGICCallBack(SHFGICConfig.getStartRequestKey(requestKey), msg);  // REQUEST_SHFGIC_REGIST, REQUEST_SHFGIC_AUTH
                    }
                    break;

                case REQUEST_SHFGIC_REGIST_COMPLETE:   //FIDO 인증등록 확인 결과
                    try {
                        JSONObject result = new JSONObject(msg);
                        result.put(SHFGICConfig.IC_ID, mIcId);
                        msg = result.toString();
                    } catch (JSONException e) {
                        LogUtil.trace(e);
                    }
                case REQUEST_SHFGIC_AUTH_COMPLETE:          //FIDO 인증결과
                case REQUEST_SHFGIC_UNREGIST_COMPLETE:      //FIDO 해지/정지 결과
                case REQUEST_SHFGIC_DIGITALSIGN_COMPLETE:   //FIDO 전자서명
                    mCallBack.onSHFGICCallBack(SHFGICConfig.getStartRequestKey(requestKey), msg);  // REQUEST_SHFGIC_REGIST, REQUEST_SHFGIC_AUTH
                    break;

                case REQUEST_SHFGIC_VERIFY_SSO_COMPLETE:
                    mCallBack.onSHFGICCallBack(requestKey, msg);
                    break;
            }

            releaseAction();

        }
    };

    /**
     * FIDO 콜백 함수
     */
    private SHFGICFido.SHFGICFidoCallBack mSHFGICFidoCallBack = new SHFGICFido.SHFGICFidoCallBack() {
        @Override
        public void onSHFGICFidoCallBack(int requestKey, String msg) {
            LogUtil.d(TAG, "onSHFGICFidoCallBack = " + requestKey + " : " + msg);

            String resultCode = "";
            try {
                JSONObject result = new JSONObject(msg);
                resultCode = result.getString(SHFGICConfig.RESULT_CODE);
            } catch (JSONException e) {
                LogUtil.trace(e);
            }

            switch (requestKey) {
                case REQUEST_FIDO_CHECK_DEVICE:  //지원가능 단말여부 확인
                    mCallBack.onSHFGICCallBack(REQUEST_FIDO_ALLOWED_AUTHNR, msg);
                    break;

                case REQUEST_SHFGIC_REGIST_MAIN:        //FIDO 등록
                case REQUEST_SHFGIC_AUTH_MAIN:          //FIDO 인증
                case REQUEST_SHFGIC_UNREGIST_MAIN:      //FIDO 해지/정지
                case REQUEST_SHFGIC_DIGITALSIGN_MAIN:   //FIDO 전자서명
                case REQUEST_SHFGIC_AUTH_PCTOAPP_MAIN:  //FIDO PCtoApp 인증
                case REQUEST_SHFGIC_VERIFY_SSO_MAIN:
                    if (resultCode.equals(OnePassManager.RESULT_OK + "") && requestKey != REQUEST_SHFGIC_AUTH_PCTOAPP_MAIN) {
                        if (REQUEST_SHFGIC_DIGITALSIGN_MAIN == requestKey)
                            getShfgicAction().fidoResultConfirm(mSHFGICActionCallBack, mIcId, SHFGICConfig.getCompleteRequestKey(requestKey), mRequestType, mSrcDoc);
                        else
                            getShfgicAction().fidoResultConfirm(mSHFGICActionCallBack, mIcId, SHFGICConfig.getCompleteRequestKey(requestKey), mRequestType); //인증등록 확인
                    } else {    //오류
                        mCallBack.onSHFGICCallBack(SHFGICConfig.getStartRequestKey(requestKey), msg);  // REQUEST_SHFGIC_REGIST, REQUEST_SHFGIC_AUTH
                    }
                    break;
            }

            releaseFido();

        }
    };
}
