package com.shinhan.shfgicdemo.view.authorization;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.common.BaseActivity;
import com.shinhan.shfgicdemo.shfgic.SHFGIC;
import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;
import com.shinhan.shfgicdemo.util.LogUtil;
import com.shinhan.shfgicdemo.util.PreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import static com.shinhan.shfgicdemo.shfgic.SHFGICConfig.REQUEST_VERIFY_SHFGIC;

/**
 * 본인인증 화면 (CI값 추출)
 * - 각사 본인인증 방식 구현 (휴대폰 본인인증 or 카드본인확인)
 */
public class AuthorizationActivity extends BaseActivity {
    private static final String TAG = AuthorizationActivity.class.getName();

    private ImageView btnTopBack, btnTopMenu;
    private TextView tvTopTitle;
    private TextView btnCancel, btnConfirm;

    private int mModeType = INTENT_VALUE_MODE_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        btnTopBack = findViewById(R.id.btnTopBack);
        btnTopMenu = findViewById(R.id.btnTopMenu);
        tvTopTitle = findViewById(R.id.tvTopTitle);

        tvTopTitle.setText(R.string.title_auth);
        btnTopBack.setOnClickListener(this);
        btnTopMenu.setOnClickListener(this);

        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

        Intent intent = getIntent();

        if (intent.hasExtra(INTENT_KEY_MODE_TYPE))
            mModeType = intent.getIntExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_DEFAULT);
    }

    public void onResume() {
        super.onResume();
    }

    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btnConfirm:

                switch (mModeType) {
                    case INTENT_VALUE_MODE_PASSWORD_FIND:   //비밀번호분실시 사용자 유효성 체크
                        showProgressDialog();
                        String icId = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_ICID);
                        getShfgic().verifySHFGIC(mSHFGICCallBack, "", icId, SHFGICConfig.CodeRequestType.PASSWORD_AUTH.getValue());
                        break;

                    default:
                        //본인 인증 후 (CI값 추출) 사용자 유효성 체크(가입여부 판단)
                        mBaseRegistVerify(mModeType);
                        break;
                }
                break;
        }
    }

    /**
     * 신한통합인증 콜백 함수
     */
    private SHFGIC.SHFGICCallBack mSHFGICCallBack = new SHFGIC.SHFGICCallBack() {
        @Override
        public void onSHFGICCallBack(int requestKey, String msg) {
            LogUtil.d(TAG, "mBaseSHFGICCallBack = " + requestKey + " : " + msg);

            releaseShfgic();
            dismissProgressDialog();

            switch (requestKey) {
                case REQUEST_VERIFY_SHFGIC: //비밀번호분실시 사용자 유효성 체크
                    String resultMsg = "";
                    try {
                        JSONObject result = new JSONObject(msg);
                        String resultCode = result.getString(SHFGICConfig.RESULT_CODE);
                        resultMsg = result.getString(SHFGICConfig.RESULT_MSG);
                        if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue())) {
                            Intent intent = new Intent(AuthorizationActivity.this, AddAuthorizationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putExtra(INTENT_KEY_MODE_TYPE, mModeType);
                            startActivity(intent);
                            finish();

                            return;
                        }

                    } catch (JSONException e) {
                        LogUtil.trace(e);
                    }

                    //오류 메세지
                    showAlertDialog(resultMsg);
                    break;
            }

        }
    };
}
