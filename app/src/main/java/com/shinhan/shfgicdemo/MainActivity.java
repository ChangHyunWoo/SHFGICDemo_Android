package com.shinhan.shfgicdemo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.shinhan.shfgicdemo.common.BaseActivity;
import com.shinhan.shfgicdemo.shfgic.SHFGIC;
import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;
import com.shinhan.shfgicdemo.util.DateUtil;
import com.shinhan.shfgicdemo.util.LogUtil;
import com.shinhan.shfgicdemo.util.PreferenceUtil;
import com.shinhan.shfgicdemo.util.StringUtil;
import com.shinhan.shfgicdemo.view.authorization.AuthorizationActivity;
import com.shinhan.shfgicdemo.view.certification.IntergratedCertificationActivity;
import com.shinhan.shfgicdemo.view.eSignature.DigitalSignatureActivity;
import com.shinhan.shfgicdemo.view.intcertmanagement.IntCertManagementActivity;
import com.shinhan.shfgicdemo.view.intcertmanagement.TerminationOptionActivity;
import com.shinhan.shfgicdemo.view.join.InformationUseActivity;
import com.shinhan.shfgicdemo.view.sso.SSOMainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.shinhan.shfgicdemo.util.PreferenceUtil.PREF_LOGIN;
import static com.shinhan.shfgicdemo.util.PreferenceUtil.PREF_LOGIN_TYPE;
import static com.shinhan.shfgicdemo.util.PreferenceUtil.PREF_LOGIN_VERIFYTYPE;
import static com.shinhan.shfgicdemo.util.PreferenceUtil.PREF_SHFGIC_ICID;
import static com.shinhan.shfgicdemo.util.PreferenceUtil.PREF_SHFGIC_VERIFY_TYPE;

public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = MainActivity.class.getName();

    private ImageView btnTopBack, btnTopMenu;
    private TextView tvTopTitle;

    private TextView btnRegist, btnTermination, btnLogin, btnLogout, btnESign, btnIC;
    private Spinner spinnerLoginYn;
    private EditText etServer, etCiName, etCi;

    private boolean isShfgicLogin = false;

    //설정
    private HashMap<String, String> mShfgicConfig = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTopBack = findViewById(R.id.btnTopBack);
        btnTopMenu = findViewById(R.id.btnTopMenu);
        tvTopTitle = findViewById(R.id.tvTopTitle);

        tvTopTitle.setText(R.string.title_main);
        btnTopBack.setVisibility(View.INVISIBLE);
        btnTopMenu.setOnClickListener(this);

        spinnerLoginYn = findViewById(R.id.spinnerLoginYn);
        spinnerLoginYn.setOnItemSelectedListener(this);

        etServer = findViewById(R.id.etServer);
        etCiName = findViewById(R.id.etCiName);
        etCi = findViewById(R.id.etCi);

        btnRegist = findViewById(R.id.btnRegist);
        btnTermination = findViewById(R.id.btnTermination);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogout = findViewById(R.id.btnLogout);

        btnRegist.setOnClickListener(this);
        btnTermination.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        btnLogout.setOnClickListener(this);

        btnESign = findViewById(R.id.btnESign);
        btnESign.setOnClickListener(this);

        btnIC = findViewById(R.id.btnIC);
        btnIC.setOnClickListener(this);

        initPropertyDemo();

        doSSOSetting(getIntent());
    }

    public void onResume() {
        LogUtil.d(TAG, "onResume");
        super.onResume();

        checkPermission();
        isLoginCheck();
    }

    @Override
    protected void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        getPreferenceUtil().put(PREF_LOGIN_TYPE, "");
        getPreferenceUtil().put(PREF_LOGIN_VERIFYTYPE, "");
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.d(TAG, "onNewIntent");
        super.onNewIntent(intent);

        doSSOSetting(intent);
    }

    //통합인증 설정 정보
    public void initSHFGIC() {
        //서버 정보
        mShfgicConfig.put(SHFGICConfig.SERVER_DOMAIN, "http://13.124.170.128:8080");

        //api url
        mShfgicConfig.put(SHFGICConfig.API_LIST_SHFGIC, "/shfgic/v1.0/listShic");           //통합인증서 목록
        mShfgicConfig.put(SHFGICConfig.API_VERIFY_SHFGIC, "/shfgic/v1.0/verifyShic");       //사용자 유효성 검증
        mShfgicConfig.put(SHFGICConfig.API_REQUEST_FIDO, "/shfgic/v1.0/requestFido");       //FIDO 요청
        mShfgicConfig.put(SHFGICConfig.API_CHECK_PIN_RULE, "/shfgic/v1.0/checkPinRule");    //PIN번호 유효성 확인
        mShfgicConfig.put(SHFGICConfig.API_REQUEST_SSO, "/shfgic/v1.0/getSsoData");

        //fido 접속 정보
        mShfgicConfig.put(SHFGICConfig.SERVER_FIDO, "http://13.124.161.80:8080");
        mShfgicConfig.put(SHFGICConfig.FIDO_SITE_ID, "SHG00000000000");
        mShfgicConfig.put(SHFGICConfig.FIDO_SVC_ID, "SHG11111111111");

        getShfgic().initProperty(mShfgicConfig);
    }

    public void isLoginCheck() {

        int selLogin = getPreferenceUtil().getBoolean(PREF_LOGIN) ? 1 : 0;
        spinnerLoginYn.setSelection(selLogin);

        String pref_ci = getPreferenceUtil().getString(PreferenceUtil.PREF_CI);
        if (!StringUtil.isEmptyString(pref_ci) && (pref_ci.trim().equals(":") || !pref_ci.contains(":"))) {
            getPreferenceUtil().put(PreferenceUtil.PREF_CI, "");
        }

        String ci = etCi.getText().toString();
        pref_ci = getPreferenceUtil().getString(PreferenceUtil.PREF_CI);

        if (!StringUtil.isEmptyString(ci)) {
            ci = etCiName.getText().toString() + " : " + etCi.getText().toString();
            if (!StringUtil.isEmptyString(pref_ci) && !pref_ci.equals(ci)) {
                getPreferenceUtil().put(PREF_LOGIN_TYPE, "");
                getPreferenceUtil().put(PREF_LOGIN_VERIFYTYPE, "");
                getPreferenceUtil().put(PREF_SHFGIC_ICID, "");
            }
        } else {
            if (!StringUtil.isEmptyString(pref_ci)) {
                etCiName.setText(StringUtil.getStringToArr(pref_ci, 0).trim());
                etCi.setText(StringUtil.getStringToArr(pref_ci, 1).trim());

            } else {
                getPreferenceUtil().put(PREF_LOGIN_TYPE, "");
                getPreferenceUtil().put(PREF_LOGIN_VERIFYTYPE, "");
                getPreferenceUtil().put(PREF_SHFGIC_ICID, "");
                etCiName.setText("김신한");
                etCi.setText("V7w141R/lhyzkmxq4+8XpAWPbF41pBFyN+c/k8qJBGXcaBT6RGrXWvNKi0KLrHmv5UxH2XTAG2LjRkHuz7fe5w==");
            }
        }

        //신한통합인증 로그인여부
        isShfgicLogin = isLoginSHFGIC();

        //button 처리
        if (isShfgicLogin) {
            btnRegist.setEnabled(false);
            btnTermination.setEnabled(true);
            btnLogin.setEnabled(false);
            btnLogout.setEnabled(true);
            btnESign.setEnabled(true);
        } else {
            btnRegist.setEnabled(true);
            btnTermination.setEnabled(false);
            btnLogin.setEnabled(true);
            btnLogout.setEnabled(false);
            btnESign.setEnabled(false);
        }
    }

    public void onClick(View v) {
        super.onClick(v);

        sendPropertyDemo();

        Intent intent;
        switch (v.getId()) {
            case R.id.btnLogin:
                intent = new Intent(this, IntergratedCertificationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;

            case R.id.btnLogout:
                logout();
                isLoginCheck();
                break;

            case R.id.btnRegist:
                intent = new Intent(this, InformationUseActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;

            case R.id.btnTermination:
                intent = new Intent(this, TerminationOptionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;

            case R.id.btnESign:
                intent = new Intent(this, DigitalSignatureActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;

            case R.id.btnIC:
                intent = new Intent(this, IntCertManagementActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spinnerLoginYn:
                getPreferenceUtil().put(PreferenceUtil.PREF_LOGIN, (position == 1 ? true : false));
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    //Demo용 설정
    public void initPropertyDemo() {
        //api url
        String apiListSHFGIC = "/shfgic/v1.0/listShic";         //통합인증서 목록
        String apiVerifySHFGIC = "/shfgic/v1.0/verifyShic";     //사용자 유효성 검증
        String apiRequestFido = "/shfgic/v1.0/requestFido";     //FIDO 요청
        String apiCheckPinRule = "/shfgic/v1.0/checkPinRule";   //PIN번호 유효성 확인
        String apiRequestSso = "/shfgic/v1.0/getSsoData";

        //그룹사 코드 (BANK:은행:001, CARD:카드:002, INVESTMENT:금투:003, INSURANCE:생명:004) - 데모앱에서는 001/002 만 사용
        //default : Group1 (001)
        String serverDomain = "http://13.124.170.128:8080";

        //fido 접속 정보
        String serverFido = "http://13.124.161.80:8080";
        String fidoSiteId = "SHG00000000000";
        String fidoSvcId = "SHG11111111111";

        //Group2 (002)
        if (getGroupCode().equals(SHFGICConfig.CodeGroupCode.CARD.getValue())) {
//            serverDomain = "http://13.125.27.166:8080";
            serverDomain = "http://13.124.170.128:9080";
            serverFido = "http://13.125.21.27:8080";
            fidoSiteId = "SHG00000000001";
            fidoSvcId = "SHG11111111112";
        }

        etServer.setText(serverDomain);

        //api url
        mShfgicConfig.put(SHFGICConfig.API_LIST_SHFGIC, apiListSHFGIC);         //통합인증서 목록
        mShfgicConfig.put(SHFGICConfig.API_VERIFY_SHFGIC, apiVerifySHFGIC);     //사용자 유효성 검증
        mShfgicConfig.put(SHFGICConfig.API_REQUEST_FIDO, apiRequestFido);       //FIDO 요청
        mShfgicConfig.put(SHFGICConfig.API_CHECK_PIN_RULE, apiCheckPinRule);    //PIN번호 유효성 확인
        mShfgicConfig.put(SHFGICConfig.API_REQUEST_SSO, apiRequestSso);

        //fido 접속 정보
        mShfgicConfig.put(SHFGICConfig.SERVER_FIDO, serverFido);
        mShfgicConfig.put(SHFGICConfig.FIDO_SITE_ID, fidoSiteId);
        mShfgicConfig.put(SHFGICConfig.FIDO_SVC_ID, fidoSvcId);
    }

    //Demo용 설정
    public void sendPropertyDemo() {
        isLoginCheck();

        String serverDomain = etServer.getText().toString();
        String ci = etCiName.getText().toString() + " : " + etCi.getText().toString();

        mShfgicConfig.put(SHFGICConfig.SERVER_DOMAIN, serverDomain);
        getPreferenceUtil().put(PreferenceUtil.PREF_CI, ci);

        getShfgic().initProperty(mShfgicConfig);
    }


    /**
     * Permission check
     *
     * @return
     */
    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA
                    , Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

            List<String> denied_permissions = new ArrayList<String>();
            for (String perm : permissions) {
                if (ActivityCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
                    denied_permissions.add(perm);
            }

            if (denied_permissions.size() > 0) {
                String[] deniedPerms = denied_permissions.toArray(new String[denied_permissions.size()]);
                ActivityCompat.requestPermissions(this, deniedPerms, 10001);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10001) {
            boolean isGranted = true;
            for (int granted : grantResults) {
                if (granted != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false;
                }
            }
            if (!isGranted) {
                //showAlertDialog("Permission error");
            } else {
                //permission granted

            }

        }
    }

    private void doSSOSetting(Intent intent) {
        if (null != intent) {
            if (intent.hasExtra(SSOMainActivity.SSO_INTENT_KEY_ACTION)) {
                sendPropertyDemo();

//                showToast(this, "Call " + intent.getStringExtra("sso_action"), Toast.LENGTH_SHORT);

                String affiliatesCode = null;
                String ssoData = null;

                if (intent.hasExtra(SSOMainActivity.SSO_INTENT_KEY_AFFILIATES_CODE))
                    affiliatesCode = intent.getStringExtra(SSOMainActivity.SSO_INTENT_KEY_AFFILIATES_CODE);

                if (intent.hasExtra(SSOMainActivity.SSO_INTENT_KEY_SSO_DATA))
                    ssoData = intent.getStringExtra(SSOMainActivity.SSO_INTENT_KEY_SSO_DATA);

                String icId = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_ICID);
                if (!StringUtil.isEmptyString(icId)) {
                    showProgressDialog();
                    getShfgic().verifySSO(mSHFGICCallBack, icId, affiliatesCode, ssoData);
                } else {

                    showAlertDialog(null, getString(R.string.alert_login_unregist)
                            , getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent intent = new Intent(MainActivity.this, InformationUseActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                }
                            }, getString(R.string.cancel), null);
                }

                //1. 인증처리
                //2. 로그인처리
                //3. goPage 값에 따라 - page이동 (홈 ..)
            }
        }
    }
// test2
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

                    //가입 여부 판단 (미가입 상태 - 가입)
                    if (StringUtil.isEmptyString(stateCode)) {
                        isUnRegist = true;
                        resourceAlert = getString(R.string.alert_login_unregist);

                    } else if (stateCode.equals(SHFGICConfig.CodeSHFGICState.NORMAL.getValue())) { //통합인증서 상태가 정상일 경우

                        //그룹사 가입여부 항목이 null 인경우 (예:블록체인 정보 못가져온 경우 .. )
                        String strAffiliatesCodes = icData.getString(SHFGICConfig.AFFILIATES_CODES);
                        if (StringUtil.isEmptyString(strAffiliatesCodes)) {
                            showAlertDialog(R.string.error_server);
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
                                                    Intent intent = new Intent(MainActivity.this, InformationUseActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                    intent.putExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_EXPIRY_DATE_AFTER);
                                                    startActivity(intent);
                                                }
                                            });

                                        } else {
                                            String icId = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_ICID);
                                            getShfgic().requestSsoFido(mSHFGICCallBack, icId, trId);
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
                showAlertDialog(null, getString(R.string.alert_login_lock)
                        , getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MainActivity.this, AuthorizationActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                intent.putExtra(INTENT_KEY_MODE_TYPE, INTENT_VALUE_MODE_PASSWORD_FIND);
                                startActivity(intent);
                            }
                        }, getString(R.string.cancel), null);
                return;
            }

            showAlertDialog(null, resourceAlert
                    , getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(MainActivity.this, InformationUseActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }
                    }, getString(R.string.cancel), null);

        } else {    //기타 오류 메세지
            showAlertDialog(resultMsg);
        }

    }

    private SHFGIC.SHFGICCallBack mSHFGICCallBack = new SHFGIC.SHFGICCallBack() {
        @Override
        public void onSHFGICCallBack(int requestKey, String msg) {
            LogUtil.d(TAG, "onSHFGICCallBack = " + requestKey + " : " + msg);

            releaseShfgic();
            dismissProgressDialog();

            try {
                JSONObject result = new JSONObject(msg);
                String resultCode = result.getString(SHFGICConfig.RESULT_CODE);
                String resultMsg = result.getString(SHFGICConfig.RESULT_MSG);

                //test
                switch (requestKey) {
                    case SHFGICConfig.REQUEST_IS_SHFGIC:
                        showAlertDialog(resultMsg);
                        break;
                    case SHFGICConfig.REQUEST_FIDO_ALLOWED_AUTHNR:
                        String shfgicVerifyType = SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue();

                        if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue()))
                            shfgicVerifyType = SHFGICConfig.CodeFidoVerifyType.FINGER.getValue();

                        getPreferenceUtil().put(PREF_SHFGIC_VERIFY_TYPE, shfgicVerifyType);
                        break;
                    case SHFGICConfig.REQUEST_SHFGIC_VERIFY_SSO:
                        checkVerifySHFGIC(msg);
                        break;
                    case SHFGICConfig.REQUEST_SHFGIC_VERIFY_SSO_COMPLETE:
                        if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue())) {
                            JSONObject resultData = result.getJSONObject(SHFGICConfig.RESULT_DATA);
                            String trStatus = resultData.getString(SHFGICConfig.TR_STATUS);

                            if (trStatus.equals(SHFGICConfig.CodeTrStatus.COMPLETE.getValue())) {
                                getPreferenceUtil().put(PreferenceUtil.PREF_LOGIN, true);
                                getPreferenceUtil().put(PreferenceUtil.PREF_LOGIN_TYPE, PreferenceUtil.LOGIN_SHFGIC);
                                getPreferenceUtil().put(PreferenceUtil.PREF_LOGIN_VERIFYTYPE, SHFGICConfig.CodeFidoVerifyType.PASSWORD.getValue());

                                showAlertDialog(R.string.shfgic_login_success);

                                isLoginCheck();

                                return;

                            } else {
                                resultMsg = resultData.getString(SHFGICConfig.TR_STATUS_MSG);
                            }
                        }

                        if (!resultCode.equals(SHFGICConfig.CodeResultCode.F9003.getValue())) { //지문 취소버튼 이벤트가 아닌 경우
                            showToast(MainActivity.this, resultMsg, Toast.LENGTH_SHORT);
                        }
                        break;
                }
            } catch (JSONException e) {
                LogUtil.trace(e);
            }
        }
    };
}
