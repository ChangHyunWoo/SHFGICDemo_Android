package com.shinhan.shfgicdemo.view.pctoapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.common.BaseActivity;
import com.shinhan.shfgicdemo.shfgic.SHFGIC;
import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;
import com.shinhan.shfgicdemo.util.DateUtil;
import com.shinhan.shfgicdemo.util.LogUtil;
import com.shinhan.shfgicdemo.util.PreferenceUtil;
import com.shinhan.shfgicdemo.util.StringUtil;
import com.shinhan.shfgicdemo.view.authorization.AuthorizationActivity;
import com.shinhan.shfgicdemo.view.intcertmanagement.FingerPrintActivity;
import com.shinhan.shfgicdemo.view.intcertmanagement.PasswordConfirmActivity;
import com.shinhan.shfgicdemo.view.join.InformationUseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by sangheun on 2018. 4. 13..
 */

public class PcToAppActivity extends BaseActivity implements ZXingScannerView.ResultHandler {
    private static final String TAG = PcToAppActivity.class.getName();

    private ImageView btnTopBack, btnTopMenu;
    private TextView tvTopTitle;

    private ViewGroup frameScannerView;
    private ZXingScannerView mScannerView = null;

    private String pref_shfgic_login_type = "";
    private Boolean isShfgicLogin = false;
    private String mSvcTrId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pctoapp);

        btnTopBack = findViewById(R.id.btnTopBack);
        btnTopMenu = findViewById(R.id.btnTopMenu);
        tvTopTitle = findViewById(R.id.tvTopTitle);

        tvTopTitle.setText(R.string.title_pctoapp);
        btnTopBack.setOnClickListener(this);
        btnTopMenu.setOnClickListener(this);

        frameScannerView = findViewById(R.id.frameScannerView);

        pref_shfgic_login_type = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_LOGIN_TYPE);   //통합인증 로그인 설정 장치타입

        //신한통합인증 로그인여부
        isShfgicLogin = isLoginSHFGIC();

        //통합인증서 보유여부 및 인증서 상태 체크
        if (isShfgicLogin)
            initQrScanner();
        else
            initVerifyCheck();
    }

    /**
     * 통합인증서 보유여부 및 인증서 상태 체크
     */
    public void initVerifyCheck() {
        showProgressDialog();
        getShfgic().isSHFGIC(mSHFGICCallBack);
    }

    /**
     * QR scanner
     */
    public void initQrScanner() {
        List<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);

        mScannerView = new ZXingScannerView(this);
        mScannerView.setFormats(formats);
        frameScannerView.addView(mScannerView);

        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    public void goAuthCheck() {
        showAlertDialog(getString(R.string.alert_pctoapp_qrcode_scan_password), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (StringUtil.notNullString(pref_shfgic_login_type).equals(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue())) {
                    goAuthView(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue());
                } else {
                    goAuthView(SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue());
                }
            }
        });


//        if (StringUtil.notNullString(pref_shfgic_login_type).equals(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue())) {
//            showAlertDialog(null, getString(R.string.alert_pctoapp_qrcode_scan)
//                    , getString(R.string.fingerprint_auth), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            goAuthView(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue());
//                        }
//                    }, getString(R.string.password_confirm), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            goAuthView(SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue());
//                        }
//                    });
//        } else {
//            showAlertDialog(getString(R.string.alert_pctoapp_qrcode_scan_password), new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    goAuthView(SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue());
//                }
//            });
//        }
    }

    public void goAuthView(String verifyType) {
        Intent intent;
        if (StringUtil.notNullString(verifyType).equals(SHFGICConfig.CodeFidoVerifyType.FINGER.getValue())) {
            intent = new Intent(this, FingerPrintActivity.class);
        } else {
            intent = new Intent(this, PasswordConfirmActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_PC_TO_APP);
        intent.putExtra(INTENT_KEY_SVCTRID, mSvcTrId);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        LogUtil.d(TAG, "onBackPressed ! ");
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        LogUtil.d(TAG, "onResume ! ");
        super.onResume();
        if (mScannerView != null)
            mScannerView.resumeCameraPreview(this);
    }

    @Override
    public void onPause() {
        LogUtil.d(TAG, "onPause ! ");
        super.onPause();
        if (mScannerView != null)
            mScannerView.stopCameraPreview();
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "onDestroy ! ");
        super.onDestroy();
        if (mScannerView != null)
            mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        LogUtil.d(TAG, "result = " + rawResult.toString() + ", Format = " + rawResult.getBarcodeFormat().toString());
        mSvcTrId = rawResult.toString();

        if (!StringUtil.isEmptyString(mSvcTrId)) {
            goAuthCheck();
        } else {
            mScannerView.resumeCameraPreview(this);
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

                switch (requestKey) {

                    case SHFGICConfig.REQUEST_IS_SHFGIC:    //통합인증서 유무
                        if (!resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue())) {

                            showAlertDialog(getString(R.string.alert_login_unregist), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                        }
                        break;

                    case SHFGICConfig.REQUEST_VERIFY_SHFGIC:        //인증서 정보 상태
                        checkVerifySHFGIC(msg);
                        break;
                }
            } catch (JSONException e) {
                LogUtil.trace(e);
            }
        }
    };


    /**
     * 인증서 정보 상태 체크
     *
     * @param msg
     */
    private void checkVerifySHFGIC(String msg) {

        String resultMsg = "", resourceAlert = "";
        boolean isUnRegist = false, isLock = false;

        try {
            JSONObject result = new JSONObject(msg);
            String resultCode = result.getString(SHFGICConfig.RESULT_CODE);
            resultMsg = result.getString(SHFGICConfig.RESULT_MSG);
            if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue()) || resultCode.equals(SHFGICConfig.CodeResultCode.AS001.getValue())) {   //AS001 - 통합인증상태가 인증할 수 없는 상태, 인증서 유효여부 및 계정 Lock 여부
                String groupCode = getGroupCode();

                String stateCode = "";
                if (result.has(SHFGICConfig.IC_DATA)) {
                    JSONObject icData = result.getJSONObject(SHFGICConfig.IC_DATA);
                    stateCode = icData.getString(SHFGICConfig.STATE_CODE);  //인증서 상태코드
                    String expiryDate = icData.getString(SHFGICConfig.EXPIRY_DATE); //만료일
                    isLock = icData.getBoolean(SHFGICConfig.LOCK);    //계정잠금여부

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
                                        //만료일 체크
                                        String nowDate = DateUtil.getNowDate();
                                        if (Integer.parseInt(nowDate) > Integer.parseInt(expiryDate)) { //만료일 종료 (재가입)
                                            showAlertDialog(R.string.alert_login_expire_after, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent intent = new Intent(PcToAppActivity.this, InformationUseActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                    intent.putExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_EXPIRY_DATE_AFTER);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });

                                        } else {
                                            //QR scanner
                                            initQrScanner();
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
                        Intent intent = new Intent(PcToAppActivity.this, AuthorizationActivity.class);
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

                    Intent intent = new Intent(PcToAppActivity.this, InformationUseActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
            }, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
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
