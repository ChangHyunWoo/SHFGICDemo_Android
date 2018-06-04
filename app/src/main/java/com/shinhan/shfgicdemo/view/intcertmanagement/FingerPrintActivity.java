package com.shinhan.shfgicdemo.view.intcertmanagement;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.raonsecure.oms.OMSFingerPrintManager;
import com.raonsecure.touchen.onepass.sdk.OnePassManager;
import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.common.BaseActivity;
import com.shinhan.shfgicdemo.shfgic.SHFGIC;
import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;
import com.shinhan.shfgicdemo.util.LogUtil;
import com.shinhan.shfgicdemo.util.PreferenceUtil;
import com.shinhan.shfgicdemo.util.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 신한통합인증 지문 인증 화면
 * - 해지 / 정지 / 지문 변경/등록 시 사용
 * - PC to App 인증
 */
public class FingerPrintActivity extends BaseActivity {
    private static final String TAG = FingerPrintActivity.class.getName();

    private ImageView btnTopBack, btnTopMenu;
    private TextView tvTopTitle;
    private ImageView ivFinger;
    private TextView tvFingerDesc, btnCancel, btnConfirm;

    private int mModeType = INTENT_VALUE_MODE_DEFAULT;
    private String mSvcTrId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);

        btnTopBack = findViewById(R.id.btnTopBack);
        btnTopMenu = findViewById(R.id.btnTopMenu);
        tvTopTitle = findViewById(R.id.tvTopTitle);

        tvTopTitle.setText(R.string.title_fingerprint);
        btnTopBack.setOnClickListener(this);
        btnTopMenu.setOnClickListener(this);

        ivFinger = findViewById(R.id.ivFinger);
        tvFingerDesc = findViewById(R.id.tvFingerDesc);
        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);

        tvFingerDesc.setVisibility(View.GONE);

        ivFinger.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

        Intent intent = getIntent();

        if (intent.hasExtra(INTENT_KEY_MODE_TYPE))
            mModeType = intent.getIntExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_DEFAULT);

        if (intent.hasExtra(INTENT_KEY_SVCTRID))
            mSvcTrId = intent.getStringExtra(INTENT_KEY_SVCTRID);

        omsFingerPrintSet();
    }

    /**
     * 지문 layout 변경
     **/
    public void omsFingerPrintSet() {
        OMSFingerPrintManager.SetHintColor(R.color.hint_color);
        OMSFingerPrintManager.SetFailColor(R.color.finger_fail_color);
        OMSFingerPrintManager.SetHintText(getString(R.string.fingerprint_hint));
        OMSFingerPrintManager.SetFailText(getString(R.string.fingerprint_not_recognized));

        if (INTENT_VALUE_MODE_CHANGE_LOGIN_SETTING_TO_FINGER != mModeType && INTENT_VALUE_MODE_REGIST_FINGERPRINT_CHANGED != mModeType)
            btnConfirm.performClick();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        String icId = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_ICID);

        switch (v.getId()) {
            case R.id.ivFinger:
            case R.id.btnConfirm:

                showProgressDialog();
                switch (mModeType) {
                    case INTENT_VALUE_MODE_CHANGE_LOGIN_SETTING_TO_FINGER:
                    case INTENT_VALUE_MODE_REGIST_FINGERPRINT_CHANGED:
                        String pref_ci = getPreferenceUtil().getString(PreferenceUtil.PREF_CI);
                        String ciName = StringUtil.getStringToArr(pref_ci, 0);

                        //FIDO 지문 인증 및 지문 등록
                        getShfgic().registRequest(mSHFGICCallBack, "", icId, SHFGICConfig.CodeRequestType.REGIST_FINGER.getValue(), ciName);
                        break;
                    case INTENT_VALUE_MODE_TERMINATION:
                    case INTENT_VALUE_MODE_SUSPENSION:
                        String requestType = "";
                        if (mModeType == INTENT_VALUE_MODE_TERMINATION) { //통합인증 해지
                            requestType = SHFGICConfig.CodeRequestType.TERMINATION_I.getValue();
                        } else {    //통합인증 정지
                            requestType = SHFGICConfig.CodeRequestType.SUSPENSION_I.getValue();
                        }

                        //FIDO 인증 요청
                        getShfgic().unRegistRequest(mSHFGICCallBack, icId, requestType);
                        break;

                    case INTENT_VALUE_MODE_PC_TO_APP:   //PC to App 인증
                        //FIDO 인증 요청
                        getShfgic().authRequestPcToApp(mSHFGICCallBack, icId, SHFGICConfig.CodeRequestType.AUTH_PCTOAPP.getValue(), mSvcTrId);
                        break;
                }

                break;
            case R.id.btnCancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        setResult(RESULT_CANCELED);
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

                    case SHFGICConfig.REQUEST_SHFGIC_UNREGIST:  //해지/정지 인 경우

                        if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue())) {
                            JSONObject resultData = result.getJSONObject(SHFGICConfig.RESULT_DATA);
                            String trStatus = resultData.getString(SHFGICConfig.TR_STATUS);

                            if (trStatus.equals(SHFGICConfig.CodeTrStatus.COMPLETE.getValue())) { //인증 성공
                                checkTerminationSuspension(mModeType);
                                return;

                            } else {
                                resultMsg = resultData.getString(SHFGICConfig.TR_STATUS_MSG);
                            }

                        }

                        if (resultCode.equals(SHFGICConfig.CodeResultCode.F9003.getValue())) { //취소버튼 이벤트인 경우
                            finish();
                        } else {
                            showAlertDialog(resultMsg);
                        }
                        break;

                    case SHFGICConfig.REQUEST_SHFGIC_REGIST:

                        if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue())) {
                            JSONObject resultData = result.getJSONObject(SHFGICConfig.RESULT_DATA);
                            String trStatus = resultData.getString(SHFGICConfig.TR_STATUS);

                            if (trStatus.equals(SHFGICConfig.CodeTrStatus.COMPLETE.getValue())) { //등록 성공
                                switch (mModeType) {
                                    case INTENT_VALUE_MODE_CHANGE_LOGIN_SETTING_TO_FINGER:
                                        getPreferenceUtil().put(PreferenceUtil.PREF_SHFGIC_REG_TYPE, SHFGICConfig.CodeFidoVerifyType.FINGER.getValue());
                                        getPreferenceUtil().put(PreferenceUtil.PREF_SHFGIC_LOGIN_TYPE, SHFGICConfig.CodeFidoVerifyType.FINGER.getValue());
                                        setResult(RESULT_OK);
                                        finish();
                                        break;
                                    case INTENT_VALUE_MODE_REGIST_FINGERPRINT_CHANGED:
                                        showAlertDialog(getString(R.string.finger_reregi_complete), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                setResult(RESULT_OK);
                                                finish();
                                            }
                                        });
                                        break;
                                }

                                return;

                            } else {
                                resultMsg = resultData.getString(SHFGICConfig.TR_STATUS_MSG);
                            }

                        } else if (resultCode.equals(SHFGICConfig.CodeResultCode.F239.getValue())) {  // 디바이스에 지문이 전혀 등록되지 않은 상태
                            resultMsg = getString(R.string.alert_fingerprint_unregist);
                        }

                        if (!resultCode.equals(SHFGICConfig.CodeResultCode.F9003.getValue())) //지문 취소버튼 이벤트인 경우 알림 X
                            showAlertDialog(resultMsg);

                        break;

                    case SHFGICConfig.REQUEST_SHFGIC_AUTH_PCTOAPP:  //PC to App 인증 결과

                        //FIDO 인증 성공 처리
                        if (resultCode.equals(OnePassManager.RESULT_OK + "")) {
                            showAlertDialog(R.string.pctoapp_auth_success, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                            return;
                        }

                        showAlertDialog(resultMsg, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });

                        break;

                }


            } catch (JSONException e) {
                LogUtil.trace(e);
            }
        }
    };
}
