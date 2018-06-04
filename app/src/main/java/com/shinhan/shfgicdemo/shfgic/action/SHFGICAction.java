package com.shinhan.shfgicdemo.shfgic.action;

import android.content.Context;

import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.shfgic.SHFGIC;
import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;
import com.shinhan.shfgicdemo.shfgic.SHFGICProperty;
import com.shinhan.shfgicdemo.shfgic.network.SHFGICAPITransaction;
import com.shinhan.shfgicdemo.shfgic.network.cruxware.SHFGICTransaction;
import com.shinhan.shfgicdemo.shfgic.network.cruxware.SHFGICTransactionException;
import com.shinhan.shfgicdemo.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 서버 연동
 * - 통신모듈 각 그룹사별로 개발
 */
public class SHFGICAction {
    private static final String TAG = SHFGICAction.class.getName();

    public interface SHFGICActionCallBack {
        void onSHFGICActionCallBack(int requstKey, String resultMsg);
    }

    private SHFGICActionCallBack mActionCallBack;
    private Context mContext;

    private String domain = "";
    private String siteId = "", svcId = "";
    private String deviceId = "", appId = "";

    public SHFGICAction(Context context) {
        mContext = context;

        domain = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.SERVER_DOMAIN);
        siteId = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.FIDO_SITE_ID);
        svcId = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.FIDO_SVC_ID);
        deviceId = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.DEVICE_ID);
        appId = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.APP_ID);
    }

    /**
     * 통합인증서 목록
     *
     * @param callBack
     * @param icId
     * @param isAll
     */
    public void listSHFGIC(SHFGICActionCallBack callBack, String icId, String isAll) {
        mActionCallBack = callBack;

        String url = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.API_LIST_SHFGIC);
        String target = domain + url;

        SHFGICAPITransaction tr = new SHFGICAPITransaction(mResultListener);

        tr.setTarget(target);
        tr.setCode(String.valueOf(SHFGICConfig.REQUEST_LIST_SHFGIC));

        tr.put(SHFGICConfig.IC_ID, icId); //통합ID
        tr.put(SHFGICConfig.IS_ALL, isAll);    //인증서 해지 포함여부 (true, false)
        tr.transmit();
    }

    /**
     * 통합인증 가입상태 조회(CI hash), 가입상태조회(통합ID) - 사용자 유효성 검증 (통합인증서 유효여부 확인)
     *
     * @param callBack
     * @param ci
     * @param icId
     * @param requestType
     */
    public void verifySHFGIC(SHFGICActionCallBack callBack, String ci, String icId, String requestType) {
        mActionCallBack = callBack;

        String url = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.API_VERIFY_SHFGIC);
        String target = domain + url;

        SHFGICAPITransaction tr = new SHFGICAPITransaction(mResultListener);

        tr.setTarget(target);
        tr.setCode(String.valueOf(SHFGICConfig.REQUEST_VERIFY_SHFGIC));

        tr.put(SHFGICConfig.COMMAND, SHFGICConfig.CodeServerCommand.VerifySHFGIC.getValue());
        tr.put(SHFGICConfig.SITE_ID, siteId);
        tr.put(SHFGICConfig.SVC_ID, svcId);
        tr.put(SHFGICConfig.DEVICE_ID, deviceId);
        tr.put(SHFGICConfig.APP_ID, appId);
        tr.put(SHFGICConfig.VERIFY_TYPE, SHFGICConfig.CodeFidoVerifyType.FINGER.getValue());
        tr.put(SHFGICConfig.CI_NO, ci); //고객번호(CI)
        tr.put(SHFGICConfig.IC_ID, icId); //통합ID
        tr.put(SHFGICConfig.REQUEST_TYPE, requestType); //업무구분
        tr.transmit();
    }

    public void requestSSO(SHFGICActionCallBack callBack, String icId) {
        mActionCallBack = callBack;

        String url = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.API_REQUEST_SSO);
        String target = domain + url;

        SHFGICAPITransaction tr = new SHFGICAPITransaction(mResultListener);

        tr.setTarget(target);
        tr.setCode(String.valueOf(SHFGICConfig.REQUEST_SHFGIC_SSO));

        tr.put(SHFGICConfig.IC_ID, icId); //통합아이디
        tr.put(SHFGICConfig.REQUEST_TYPE, SHFGICConfig.CodeRequestType.AUTH_SSO.getValue());

        tr.transmit();
    }

    public void verifySSO(SHFGICActionCallBack callBack, String icId, String affiliatesCode, String ssoData) {
        mActionCallBack = callBack;

        String url = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.API_REQUEST_FIDO);
        String target = domain + url;

        SHFGICAPITransaction tr = new SHFGICAPITransaction(mResultListener);

        tr.setTarget(target);
        tr.setCode(String.valueOf(SHFGICConfig.REQUEST_SHFGIC_VERIFY_SSO_READY));

        tr.put(SHFGICConfig.COMMAND, SHFGICConfig.CodeServerCommand.ServiceAuth.getValue());
        tr.put(SHFGICConfig.BIZ_REQ_TYPE, "app");
        tr.put(SHFGICConfig.SITE_ID, siteId);
        tr.put(SHFGICConfig.SVC_ID, svcId);
        tr.put(SHFGICConfig.DEVICE_ID, deviceId);
        tr.put(SHFGICConfig.APP_ID, appId);
        tr.put(SHFGICConfig.VERIFY_TYPE, SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue());
        tr.put(SHFGICConfig.IC_ID, icId);
        tr.put(SHFGICConfig.REQUEST_TYPE, SHFGICConfig.CodeRequestType.AUTH_SSO.getValue());
        tr.put(SHFGICConfig.AFFILIATES_CODE, affiliatesCode);
        tr.put(SHFGICConfig.SSO_DATA, ssoData);

        tr.transmit();
    }

    /**
     * FIDO 허용단말 리스트 요청
     *
     * @param callBack
     */
    public void fidoAllowedAuthnr(SHFGICActionCallBack callBack) {
        mActionCallBack = callBack;

        String url = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.API_REQUEST_FIDO);
        String target = domain + url;

        SHFGICAPITransaction tr = new SHFGICAPITransaction(mResultListener);

        tr.setTarget(target);
        tr.setCode(String.valueOf(SHFGICConfig.REQUEST_FIDO_ALLOWED_AUTHNR));

        tr.put(SHFGICConfig.COMMAND, SHFGICConfig.CodeServerCommand.AllowedAuthnr.getValue());
        tr.put(SHFGICConfig.SITE_ID, siteId);
        tr.put(SHFGICConfig.SVC_ID, svcId);
        tr.put(SHFGICConfig.VERIFY_TYPE, SHFGICConfig.CodeFidoVerifyType.FINGER.getValue());
        tr.transmit();
    }

    /**
     * FIDO PIN 번호 유효성 확인
     *
     * @param callBack
     * @param pinPwd
     */
    public void fidoCheckPINValidation(SHFGICActionCallBack callBack, String pinPwd) {
        mActionCallBack = callBack;

        String url = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.API_CHECK_PIN_RULE);
        String target = domain + url;

        SHFGICAPITransaction tr = new SHFGICAPITransaction(mResultListener);

        tr.setTarget(target);
        tr.setCode(String.valueOf(SHFGICConfig.REQUEST_FIDO_CHECK_PIN_VALIDATION));

        tr.put(SHFGICConfig.PIN_PWD, pinPwd);

        tr.transmit();
    }

    /**
     * FIDO 등록준비 요청
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
    public void fidoServiceRegist(SHFGICActionCallBack callBack, String ci, String pinPwd, String pinPwdCheck, String icId, String verifyType, String requestType, String realNm) {
        mActionCallBack = callBack;

        String url = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.API_REQUEST_FIDO);
        String target = domain + url;

        SHFGICAPITransaction tr = new SHFGICAPITransaction(mResultListener);

        tr.setTarget(target);
        tr.setCode(String.valueOf(SHFGICConfig.REQUEST_SHFGIC_REGIST_READY));

        tr.put(SHFGICConfig.COMMAND, SHFGICConfig.CodeServerCommand.ServiceRegist.getValue());
        tr.put(SHFGICConfig.BIZ_REQ_TYPE, "app");
        tr.put(SHFGICConfig.SITE_ID, siteId);
        tr.put(SHFGICConfig.SVC_ID, svcId);
        tr.put(SHFGICConfig.DEVICE_ID, deviceId);
        tr.put(SHFGICConfig.APP_ID, appId);
        tr.put(SHFGICConfig.VERIFY_TYPE, verifyType);
        tr.put(SHFGICConfig.CI_NO, ci); //고객번호(CI)
        tr.put(SHFGICConfig.OS_TYPE, "1");  //"1" : Android, "2": IOS
        tr.put(SHFGICConfig.PIN_PWD, pinPwd);
        tr.put(SHFGICConfig.PIN_PWD_CHECK, pinPwdCheck);
        tr.put(SHFGICConfig.IC_ID, icId); //통합아이디
        tr.put(SHFGICConfig.REQUEST_TYPE, requestType); //업무구분
        tr.put(SHFGICConfig.REAL_NM, realNm); //고객실명
        tr.transmit();
    }

    /**
     * FIDO 등록/인증/해지/정지 완료요청
     *
     * @param callBack
     * @param icId
     * @param requestKey
     * @param requestType
     */
    public void fidoResultConfirm(SHFGICActionCallBack callBack, String icId, int requestKey, String requestType) {
        fidoResultConfirm(callBack, icId, requestKey, requestType, null);
    }

    /**
     * FIDO 등록/인증/해지/정지 완료요청
     *
     * @param callBack
     * @param icId
     * @param requestKey
     * @param requestType
     * @param srcDoc
     */
    public void fidoResultConfirm(SHFGICActionCallBack callBack, String icId, int requestKey, String requestType, String srcDoc) {
        mActionCallBack = callBack;

        String url = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.API_REQUEST_FIDO);
        String target = domain + url;

        SHFGICAPITransaction tr = new SHFGICAPITransaction(mResultListener);

        tr.setTarget(target);
        tr.setCode(String.valueOf(requestKey));

        tr.put(SHFGICConfig.COMMAND, SHFGICConfig.CodeServerCommand.ResultConfirm.getValue());
        tr.put(SHFGICConfig.IC_ID, icId);
        tr.put(SHFGICConfig.REQUEST_TYPE, requestType); //업무구분

        if (null != srcDoc)     // 전자서명인 경우
            tr.put(SHFGICConfig.SRC_DOC, srcDoc);

        tr.transmit();
    }

    /**
     * FIDO 인증준비 요청 (+ 비밀번호 변경시에도 사용)
     *
     * @param callBack
     * @param icId
     * @param pinPwd
     * @param pinPwdCheck
     * @param verifyType
     * @param requestType
     * @param requestKeyCode
     */
    public void fidoServiceAuth(SHFGICActionCallBack callBack, String icId, String pinPwd, String pinPwdCheck, String verifyType, String requestType, int requestKeyCode) {
        mActionCallBack = callBack;

        String url = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.API_REQUEST_FIDO);
        String target = domain + url;

        SHFGICAPITransaction tr = new SHFGICAPITransaction(mResultListener);

        tr.setTarget(target);
        tr.setCode(String.valueOf(requestKeyCode));

        tr.put(SHFGICConfig.COMMAND, SHFGICConfig.CodeServerCommand.ServiceAuth.getValue());
        tr.put(SHFGICConfig.BIZ_REQ_TYPE, "app");
        tr.put(SHFGICConfig.SITE_ID, siteId);
        tr.put(SHFGICConfig.SVC_ID, svcId);
        tr.put(SHFGICConfig.DEVICE_ID, deviceId);
        tr.put(SHFGICConfig.APP_ID, appId);
        tr.put(SHFGICConfig.VERIFY_TYPE, verifyType);
        tr.put(SHFGICConfig.IC_ID, icId);
        tr.put(SHFGICConfig.PIN_PWD, pinPwd);
        tr.put(SHFGICConfig.PIN_PWD_CHECK, pinPwdCheck);
        tr.put(SHFGICConfig.REQUEST_TYPE, requestType); //업무구분
        tr.transmit();
    }


    /**
     * FIDO 전자서명 준비 요청
     *
     * @param callBack
     * @param icId
     * @param svcTrChallenge
     * @param transType
     * @param verifyType
     * @param requestType
     */
    public void fidoServiceAuth2(SHFGICActionCallBack callBack, String icId, String svcTrChallenge, String transType, String verifyType, String pinPwd, String requestType) {
        mActionCallBack = callBack;

        String url = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.API_REQUEST_FIDO);
        String target = domain + url;

        SHFGICAPITransaction tr = new SHFGICAPITransaction(mResultListener);

        tr.setTarget(target);
        tr.setCode(String.valueOf(SHFGICConfig.REQUEST_SHFGIC_DIGITALSIGN_READY));

        tr.put(SHFGICConfig.COMMAND, SHFGICConfig.CodeServerCommand.ServiceAuth2.getValue());
        tr.put(SHFGICConfig.BIZ_REQ_TYPE, "app");
        tr.put(SHFGICConfig.SITE_ID, siteId);
        tr.put(SHFGICConfig.SVC_ID, svcId);
        tr.put(SHFGICConfig.IC_ID, icId);
        tr.put(SHFGICConfig.DEVICE_ID, deviceId);
        tr.put(SHFGICConfig.APP_ID, appId);
        tr.put(SHFGICConfig.SVC_TR_CHALLENGE, svcTrChallenge);
        tr.put(SHFGICConfig.VERIFY_TYPE, verifyType);
        tr.put(SHFGICConfig.TRANS_TYPE, transType);
        tr.put(SHFGICConfig.PIN_PWD, pinPwd);
        tr.put(SHFGICConfig.REQUEST_TYPE, requestType); //업무구분
        tr.transmit();
    }


    /**
     * FIDO 해지/정지 준비요청
     *
     * @param callBack
     * @param icId
     * @param pinPwd
     * @param verifyType
     * @param requestType
     */
    public void fidoServiceUnRegist(SHFGICActionCallBack callBack, String icId, String pinPwd, String verifyType, String requestType) {
        mActionCallBack = callBack;

        String url = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.API_REQUEST_FIDO);
        String target = domain + url;

        SHFGICAPITransaction tr = new SHFGICAPITransaction(mResultListener);

        tr.setTarget(target);
        tr.setCode(String.valueOf(SHFGICConfig.REQUEST_SHFGIC_UNREGIST_READY));

        tr.put(SHFGICConfig.COMMAND, SHFGICConfig.CodeServerCommand.ServiceRelease.getValue());
        tr.put(SHFGICConfig.BIZ_REQ_TYPE, "app");
        tr.put(SHFGICConfig.SITE_ID, siteId);
        tr.put(SHFGICConfig.SVC_ID, svcId);
        tr.put(SHFGICConfig.DEVICE_ID, deviceId);
        tr.put(SHFGICConfig.APP_ID, appId);
        tr.put(SHFGICConfig.VERIFY_TYPE, verifyType);
        tr.put(SHFGICConfig.IC_ID, icId);
        tr.put(SHFGICConfig.PIN_PWD, pinPwd);
        tr.put(SHFGICConfig.REQUEST_TYPE, requestType); //업무구분
        tr.transmit();
    }

    /**
     * Pc To App 인증 준비 요청
     *
     * @param callBack
     * @param icId
     * @param pinPwd
     * @param verifyType
     * @param requestType
     * @param svcTrId
     */
    public void fidoServiceAuthPcToApp(SHFGICActionCallBack callBack, String icId, String pinPwd, String verifyType, String requestType, String svcTrId) {
        mActionCallBack = callBack;

        String url = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.API_REQUEST_FIDO);
        String target = domain + url;

        SHFGICAPITransaction tr = new SHFGICAPITransaction(mResultListener);

        tr.setTarget(target);
        tr.setCode(String.valueOf(SHFGICConfig.REQUEST_SHFGIC_AUTH_PCTOAPP_READY));

        tr.put(SHFGICConfig.COMMAND, SHFGICConfig.CodeServerCommand.ServiceAuth.getValue());
        tr.put(SHFGICConfig.BIZ_REQ_TYPE, "app");
        tr.put(SHFGICConfig.SITE_ID, siteId);
        tr.put(SHFGICConfig.SVC_ID, svcId);
        tr.put(SHFGICConfig.DEVICE_ID, deviceId);
        tr.put(SHFGICConfig.APP_ID, appId);
        tr.put(SHFGICConfig.VERIFY_TYPE, verifyType);
        tr.put(SHFGICConfig.IC_ID, icId);
        tr.put(SHFGICConfig.PIN_PWD, pinPwd);
        tr.put(SHFGICConfig.REQUEST_TYPE, requestType); //업무구분
        tr.put(SHFGICConfig.SVC_TRID, svcTrId);
        tr.transmit();
    }


    private SHFGICTransaction.ResultOnUiThreadListener mResultListener = new SHFGICTransaction.ResultOnUiThreadListener() {
        @Override
        public void onTransactionCanceledOnUiThread(SHFGICTransaction transaction) {
            SHFGICAPITransaction tr = (SHFGICAPITransaction) transaction;
            JSONObject response = tr.response();
            LogUtil.d(TAG, "_____onTransactionCanceled(" + response.toString() + ")");

            JSONObject result = new JSONObject();
            try {
                result.put(SHFGICConfig.RESULT_CODE, SHFGICConfig.CodeResultCode.FAIL.getValue());
                result.put(SHFGICConfig.RESULT_MSG, mContext.getString(R.string.error_server));
            } catch (JSONException e) {
                LogUtil.trace(e);
            }

            mActionCallBack.onSHFGICActionCallBack(Integer.parseInt(tr.code()), result.toString());
        }

        @Override
        public void onTransactionFailedOnUiThread(SHFGICTransaction transaction, SHFGICTransactionException exception) {
            SHFGICAPITransaction tr = (SHFGICAPITransaction) transaction;
            JSONObject response = tr.response();
            LogUtil.d(TAG, "_____onTransactionFailed(" + response.toString() + ", " + exception + ")");

            JSONObject result = new JSONObject();
            try {
                result.put(SHFGICConfig.RESULT_CODE, SHFGICConfig.CodeResultCode.FAIL.getValue());
                result.put(SHFGICConfig.RESULT_MSG, mContext.getString(R.string.error_server));
            } catch (JSONException e) {
                LogUtil.trace(e);
            }

            mActionCallBack.onSHFGICActionCallBack(Integer.parseInt(tr.code()), result.toString());
        }

        @Override
        public void onTransactionFinishedOnUiThread(SHFGICTransaction transaction) {
            LogUtil.d(TAG, "_____onTransactionFinished(" + transaction + ")");
            SHFGICAPITransaction tr = (SHFGICAPITransaction) transaction;
            JSONObject response = tr.response();

            //mActionCallBack.onSHFGICActionCallBack(Integer.parseInt(tr.code()), tr.toString());

            try {
                JSONObject result = response.getJSONObject("dataBody");
                LogUtil.d(TAG, "Result : " + result.toString());
                mActionCallBack.onSHFGICActionCallBack(Integer.parseInt(tr.code()), result.toString());
            } catch (JSONException e) {
                LogUtil.trace(e);
            }
        }
    };
}
