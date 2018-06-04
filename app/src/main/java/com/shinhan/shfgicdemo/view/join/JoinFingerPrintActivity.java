package com.shinhan.shfgicdemo.view.join;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.raonsecure.oms.OMSFingerPrintManager;
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
 * 신한통합인증 가입/등록시 지문등록 화면
 * - FIDO 지문 인증 및 지문 등록
 */
public class JoinFingerPrintActivity extends BaseActivity {
    private static final String TAG = JoinFingerPrintActivity.class.getName();

    private ImageView btnTopBack, btnTopMenu;
    private TextView tvTopTitle;
    private TextView tvFinger;
    private ImageView ivFinger;
    private TextView btnCancel, btnConfirm;

    private String mRequestType = SHFGICConfig.CodeRequestType.REGIST_NEW.getValue();
    private String mIcId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);

        Intent intent = getIntent();
        if (intent.hasExtra(INTENT_KEY_REQUEST_TYPE)) {
            mRequestType = intent.getStringExtra(INTENT_KEY_REQUEST_TYPE);
        }

        if (intent.hasExtra(INTENT_KEY_ICID)) {
            mIcId = intent.getStringExtra(INTENT_KEY_ICID);
        }

        btnTopBack = findViewById(R.id.btnTopBack);
        btnTopMenu = findViewById(R.id.btnTopMenu);
        tvTopTitle = findViewById(R.id.tvTopTitle);

        tvTopTitle.setText(R.string.title_fingerprint);
        btnTopBack.setVisibility(View.INVISIBLE);
        btnTopMenu.setOnClickListener(this);

        tvFinger = findViewById(R.id.tvFinger);
        ivFinger = findViewById(R.id.ivFinger);
        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);

        tvFinger.setText(R.string.finger_print_guide_regist);
        btnCancel.setText(R.string.skip);
        btnConfirm.setText(R.string.regist_finger);

        ivFinger.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

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

        //OnePassManager.SetWriteableLog(true);
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        LogUtil.d(TAG, "onBackPressed ! ");
        //super.onBackPressed();
        return;
    }

    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btnCancel:    //건너뛰기 - 지문인증 안하고 등록완료처리
                intent = new Intent(this, JoinCompleteActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                break;

            case R.id.ivFinger:
            case R.id.btnConfirm:
                //FIDO 지문 인증 및 지문 등록
                showProgressDialog();
                String pref_ci = getPreferenceUtil().getString(PreferenceUtil.PREF_CI);
                String ciName = StringUtil.getStringToArr(pref_ci, 0);
                String ci = StringUtil.getStringToArr(pref_ci, 1);
                String icId = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_ICID);
                getShfgic().registRequest(mSHFGICCallBack, ci, icId, mRequestType, ciName);
                break;
        }
        super.onClick(v);
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

            if (requestKey == SHFGICConfig.REQUEST_SHFGIC_REGIST) { //FIDO 지문 인증 및 지문 등록 결과

                try {
                    JSONObject result = new JSONObject(msg);
                    String resultCode = result.getString(SHFGICConfig.RESULT_CODE);
                    String resultMsg = result.getString(SHFGICConfig.RESULT_MSG);
                    if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue())) {
                        JSONObject resultData = result.getJSONObject(SHFGICConfig.RESULT_DATA);
                        String trStatus = resultData.getString(SHFGICConfig.TR_STATUS);

                        if (trStatus.equals(SHFGICConfig.CodeTrStatus.COMPLETE.getValue())) { //등록 성공
                            getPreferenceUtil().put(PreferenceUtil.PREF_SHFGIC_REG_TYPE, SHFGICConfig.CodeFidoVerifyType.FINGER.getValue());
                            getPreferenceUtil().put(PreferenceUtil.PREF_SHFGIC_LOGIN_TYPE, SHFGICConfig.CodeFidoVerifyType.FINGER.getValue());

                            Intent intent = new Intent(JoinFingerPrintActivity.this, JoinCompleteActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();

                            return;

                        } else {
                            resultMsg = resultData.getString(SHFGICConfig.TR_STATUS_MSG);
                        }

                    } else if (resultCode.equals(SHFGICConfig.CodeResultCode.F239.getValue())) {  // 디바이스에 지문이 전혀 등록되지 않은 상태
                        resultMsg = getString(R.string.alert_fingerprint_unregist);
                    }

                    if (!resultCode.equals(SHFGICConfig.CodeResultCode.F9003.getValue())) //지문 취소버튼 이벤트인 경우 알림 X
                        showAlertDialog(resultMsg);


                } catch (JSONException e) {
                    LogUtil.trace(e);
                }

            }
        }
    };
}
