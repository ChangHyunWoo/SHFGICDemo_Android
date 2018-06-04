package com.shinhan.shfgicdemo.shfgic.action;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.raonsecure.touchen.onepass.sdk.OnePassManager;
import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;
import com.shinhan.shfgicdemo.shfgic.SHFGICProperty;
import com.shinhan.shfgicdemo.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * FIDO 연동
 */
public class SHFGICFido {
    private static final String TAG = SHFGICFido.class.getName();

    public interface SHFGICFidoCallBack {
        void onSHFGICFidoCallBack(int requstKey, String resultMsg);
    }

    private SHFGICFidoCallBack mFidoCallBack;
    private OnePassManager mOnePassManager;
    private Context mContext;
    private String mServerFido;
    private String mTrid;

    public SHFGICFido(Context context) {
        mContext = context;
        mServerFido = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.SERVER_FIDO);
        if (mOnePassManager == null) {
            mOnePassManager = new OnePassManager(mContext, mRespHandler);
            fidoInit();
        }
    }

    /**
     * 메모리 해제
     */
    public void release() {
        if (mOnePassManager != null) {
            mOnePassManager.release();
        }
    }

    /**
     * FIDO 초기셋팅
     */
    public void fidoInit() {
        String siteId = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.FIDO_SITE_ID);
        String svcId = SHFGICProperty.getInstance().getPropertyInfo(SHFGICConfig.FIDO_SVC_ID);

        mOnePassManager.setInitInfo(mServerFido, siteId, svcId);
        mOnePassManager.setTopsUrl(mServerFido);
    }

    /**
     * FIDO 지원가능 단말여부 확인
     *
     * @param callBack
     * @param arrAAID
     */
    public void isSupportedDevice(SHFGICFidoCallBack callBack, String[] arrAAID) {
        LogUtil.d(TAG, "isSupportedDevice : " + arrAAID);
        mFidoCallBack = callBack;
        mOnePassManager.isSupportedDevice(arrAAID, SHFGICConfig.REQUEST_FIDO_CHECK_DEVICE);
    }

    /**
     * FIDO 요청 (등록/인증/해지/정지)
     * @param callBack
     * @param trid
     * @param what
     */
    public void fidoRequest(SHFGICFidoCallBack callBack, String trid, int what) {
        LogUtil.d(TAG, "fidoRegist : " + trid);
        mFidoCallBack = callBack;
        mTrid = trid;
        mOnePassManager.request(trid, what);
    }


    public Handler mRespHandler = new Handler() {
        public void handleMessage(Message msg) {

            Bundle data = msg.getData();
            LogUtil.d(TAG, "mRespHandler : " + data);
            int resultCode;
            String resultMsg, returnCode, returnMsg;
            resultCode = data.getInt(OnePassManager.RESULT_CODE);
            resultMsg = data.getString(OnePassManager.RESULT_MSG);

            JSONObject result = new JSONObject();
            switch (msg.what) {
                case SHFGICConfig.REQUEST_FIDO_CHECK_DEVICE:  //지원가능 단말여부 확인

                    returnCode = SHFGICConfig.CodeResultCode.FAIL.getValue();
                    returnMsg = mContext.getString(R.string.resultmsg_device_not_allowed);
                    if (resultCode == OnePassManager.RESULT_OK) {
                        boolean isSupported = data.getBoolean(OnePassManager.RESULT_ISSUPPORTEDDEVICE);
                        if (isSupported) {
                            returnCode = SHFGICConfig.CodeResultCode.SUCCESS.getValue();
                            returnMsg = mContext.getString(R.string.resultmsg_device_allowed);
                        }
                    }

                    try {
                        result.put(SHFGICConfig.RESULT_CODE, returnCode);
                        result.put(SHFGICConfig.RESULT_MSG, returnMsg);
                    } catch (JSONException e) {
                        LogUtil.trace(e);
                    }

                    LogUtil.d(TAG, "mRespHandler Result : " + result.toString());
                    mFidoCallBack.onSHFGICFidoCallBack(msg.what, result.toString());
                    break;

                case SHFGICConfig.REQUEST_SHFGIC_REGIST_MAIN:  //Fido 인증 등록
                case SHFGICConfig.REQUEST_SHFGIC_AUTH_MAIN: //Fido 인증
                case SHFGICConfig.REQUEST_SHFGIC_UNREGIST_MAIN:  //Fido 인증 해지/정지
                case SHFGICConfig.REQUEST_SHFGIC_DIGITALSIGN_MAIN:  //전자서명
                case SHFGICConfig.REQUEST_SHFGIC_AUTH_PCTOAPP_MAIN:  //Pc to App 인증
                case SHFGICConfig.REQUEST_SHFGIC_VERIFY_SSO_MAIN:
                    try {
                        result.put(SHFGICConfig.RESULT_CODE, resultCode);
                        result.put(SHFGICConfig.RESULT_MSG, resultMsg);
                    } catch (JSONException e) {
                        LogUtil.trace(e);
                    }

                    LogUtil.d(TAG, "mRespHandler Result : " + result.toString());
                    mFidoCallBack.onSHFGICFidoCallBack(SHFGICConfig.getMainRequestKey(msg.what), result.toString());    // REQUEST_SHFGIC_REGIST_MAIN, REQUEST_SHFGIC_AUTH_MAIN
                    break;

                default:
                    break;
            }
        }
    };

}
