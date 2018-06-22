package com.shinhan.shfgicdemo.view.sso;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.common.BaseActivity;
import com.shinhan.shfgicdemo.shfgic.SHFGIC;
import com.shinhan.shfgicdemo.shfgic.SHFGICConfig;
import com.shinhan.shfgicdemo.util.LogUtil;
import com.shinhan.shfgicdemo.util.PreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class SSOMainActivity extends BaseActivity {
    private static final String TAG = SSOMainActivity.class.getName();

    private static final int INDEX_BANK = 0;
    private static final int INDEX_CARD = 1;
    private static final int INDEX_INVESTMENT = 2;
    private static final int INDEX_INSURANCE = 3;
    private static final int SHINHAN_GROUP_AFFILIATED_COMPANY_NUM = 4;

//    public static final String SSO_INTENT_KEY_GROUP_TYPE = "groupType";
////    public static final String SSO_INTENT_KEY_AFFILIATES_CODE = "affiliates_code";
////    public static final String SSO_INTENT_KEY_SSO_DATA = "sso_data";
////    public static final String SSO_INTENT_KEY_ACTION = "sso_action";

    //SSO Scheme
    public static final String SSO_PARAM_AFFILIATESCODE = "affiliatesCode";
    public static final String SSO_PARAM_SSODATA = "ssoData";
    public static final String SSO_PARAM_SSOTIME = "ssoTime";

    public static final int SSO_INTENT_VALUE_NONE = 1000;
    public static final int SSO_INTENT_VALUE_BANK = 1001;
    public static final int SSO_INTENT_VALUE_CARD = 1002;
    public static final int SSO_INTENT_VALUE_INVESTMENT = 1003;
    public static final int SSO_INTENT_VALUE_INSURANCE = 1004;

    public static final String SSO_PACKAGE_BANK = "com.shinhan.shfgicdemo.group1";
    public static final String SSO_PACKAGE_CARD = "com.shinhan.shfgicdemo.group2";
//    public static final String SSO_PACKAGE_BANK = "com.shinhan.sbanking";
//    public static final String SSO_PACKAGE_CARD = "com.shcard.smartpay";

//    public static final String SSO_PACKAGE_INVESTMENT = "com.shinhan.shfgicdemo.group3";
//    public static final String SSO_PACKAGE_INSURANCE = "com.shinhan.shfgicdemo.group4";
    public static final String SSO_PACKAGE_INVESTMENT = "com.shinhaninvest.nsmts";
    public static final String SSO_PACKAGE_INSURANCE = "com.AFSSHLife";

    private ImageView btnTopBack, btnTopMenu = null;
    private TextView tvTopTitle = null;

    private LinearLayout llGroup = null;
    private LinearLayout[] llSectionGroup = null;
    private RelativeLayout[] rlBtnGroup = null;
    private ImageView[] ivStatusGroup = null;

    private int mMyGroupCheckIndex = -1;
    private String[] mCurrentStatus = null;     // 인증서 상태 코드 값과 동일하게 "1"(정상), "2"(해지), "3"(정지)를 가지나 여기에 "0"(affiliatesCodes에 값이 아예 내려오지 않는 경우) 추가함

    private int mClickId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sso_main);

        btnTopBack = findViewById(R.id.btnTopBack);
        btnTopMenu = findViewById(R.id.btnTopMenu);
        tvTopTitle = findViewById(R.id.tvTopTitle);

        btnTopBack.setOnClickListener(this);
        btnTopMenu.setVisibility(View.GONE);

        if (isLoginSHFGIC()) {
            llGroup = findViewById(R.id.llGroup);
            llGroup.setVisibility(View.VISIBLE);

            mMyGroupCheckIndex = checkMyGroup();

            isShfgic();
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        switch (v.getId()) {
            case R.id.rlBtnGroup1:
            case R.id.rlBtnGroup2:
            case R.id.rlBtnGroup3:
            case R.id.rlBtnGroup4:
                mClickId = v.getId();

                String groupCurrentStatus = getGroupCurrentStatus(v.getId());

                if (groupCurrentStatus.equals(SHFGICConfig.CodeSHFGICState.SUSPENSION.getValue())) {
                    showAlertDialog(getString(R.string.sso_ic_status_suspension));
                } else {

                    showProgressDialog();

                    String icId = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_ICID);

                    getShfgic().requestSSO(mSHFGICCallBack, icId);
                }
                break;
        }
    }

    //통합인증서 보유여부
    private void isShfgic() {
        showProgressDialog();

        String icId = getPreferenceUtil().getString(PreferenceUtil.PREF_SHFGIC_ICID);

        getShfgic().listSHFGIC(mSHFGICCallBack, icId, "true");
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

                if (resultCode.equals(SHFGICConfig.CodeResultCode.SUCCESS.getValue())) {

                    switch (requestKey) {
                        case SHFGICConfig.REQUEST_LIST_SHFGIC:
                            Map<String, String> affiliatesCodesMap = new HashMap<String, String>();
                            String stateCode = null;

                            if (result.has(SHFGICConfig.IC_DATA)) {
                                JSONObject icData = result.getJSONObject(SHFGICConfig.IC_DATA);

                                if (icData.has(SHFGICConfig.AFFILIATES_CODES)) {
                                    JSONObject affiliatesCodes = icData.getJSONObject(SHFGICConfig.AFFILIATES_CODES);

                                    if (affiliatesCodes.has(SHFGICConfig.CodeGroupCode.BANK.getValue()))
                                        affiliatesCodesMap.put(SHFGICConfig.CodeGroupCode.BANK.getValue(), affiliatesCodes.getString(SHFGICConfig.CodeGroupCode.BANK.getValue()));

                                    if (affiliatesCodes.has(SHFGICConfig.CodeGroupCode.CARD.getValue()))
                                        affiliatesCodesMap.put(SHFGICConfig.CodeGroupCode.CARD.getValue(), affiliatesCodes.getString(SHFGICConfig.CodeGroupCode.CARD.getValue()));

                                    if (affiliatesCodes.has(SHFGICConfig.CodeGroupCode.INVESTMENT.getValue()))
                                        affiliatesCodesMap.put(SHFGICConfig.CodeGroupCode.INVESTMENT.getValue(), affiliatesCodes.getString(SHFGICConfig.CodeGroupCode.INVESTMENT.getValue()));

                                    if (affiliatesCodes.has(SHFGICConfig.CodeGroupCode.INSURANCE.getValue()))
                                        affiliatesCodesMap.put(SHFGICConfig.CodeGroupCode.INSURANCE.getValue(), affiliatesCodes.getString(SHFGICConfig.CodeGroupCode.INSURANCE.getValue()));
                                }

                                if (icData.has(SHFGICConfig.STATE_CODE))
                                    stateCode = icData.getString(SHFGICConfig.STATE_CODE);
                            }

                            if (null != stateCode)
                                doSSOSetting(stateCode, affiliatesCodesMap);

                            affiliatesCodesMap = null;
                            break;
                        case SHFGICConfig.REQUEST_SHFGIC_SSO:
                            String affiliatesCode = null;
                            String ssoData = null;

                            if (result.has(SHFGICConfig.AFFILIATES_CODE))
                                affiliatesCode = result.getString(SHFGICConfig.AFFILIATES_CODE);

                            if (result.has(SHFGICConfig.SSO_DATA))
                                ssoData = result.getString(SHFGICConfig.SSO_DATA);

                            runSingleSignOnApp(getSSOActionGroupType(mClickId), affiliatesCode, ssoData);
                            break;
                    }
                } else {
                    showAlertDialog(resultMsg);

                    return;
                }

            } catch (JSONException e) {
                showToast(SSOMainActivity.this, e.toString(), Toast.LENGTH_SHORT);
            }
        }
    };

    private int checkMyGroup() {
        llSectionGroup = new LinearLayout[SHINHAN_GROUP_AFFILIATED_COMPANY_NUM];
        llSectionGroup[INDEX_BANK] = findViewById(R.id.llSectionGroup1);
        llSectionGroup[INDEX_CARD] = findViewById(R.id.llSectionGroup2);
        llSectionGroup[INDEX_INVESTMENT] = findViewById(R.id.llSectionGroup3);
        llSectionGroup[INDEX_INSURANCE] = findViewById(R.id.llSectionGroup4);

        String packageName = getPackageName();
        int myGroupCheckIndex = -1;

        if (null != packageName) {
            if (packageName.equals(SSO_PACKAGE_BANK))
                myGroupCheckIndex = INDEX_BANK;
            else if (packageName.equals(SSO_PACKAGE_CARD))
                myGroupCheckIndex = INDEX_CARD;
            else if (packageName.equals(SSO_PACKAGE_INVESTMENT))
                myGroupCheckIndex = INDEX_INVESTMENT;
            else if (packageName.equals(SSO_PACKAGE_INSURANCE))
                myGroupCheckIndex = INDEX_INSURANCE;

            llSectionGroup[myGroupCheckIndex].setVisibility(View.GONE);
        }

        return myGroupCheckIndex;
    }

    private void doSSOSetting(String stateCode, Map affiliatesCodes) {
        rlBtnGroup = new RelativeLayout[SHINHAN_GROUP_AFFILIATED_COMPANY_NUM];
        rlBtnGroup[INDEX_BANK] = findViewById(R.id.rlBtnGroup1);
        rlBtnGroup[INDEX_CARD] = findViewById(R.id.rlBtnGroup2);
        rlBtnGroup[INDEX_INVESTMENT] = findViewById(R.id.rlBtnGroup3);
        rlBtnGroup[INDEX_INSURANCE] = findViewById(R.id.rlBtnGroup4);

        ivStatusGroup = new ImageView[SHINHAN_GROUP_AFFILIATED_COMPANY_NUM];
        ivStatusGroup[INDEX_BANK] = findViewById(R.id.ivStatusGroup1);
        ivStatusGroup[INDEX_CARD] = findViewById(R.id.ivStatusGroup2);
        ivStatusGroup[INDEX_INVESTMENT] = findViewById(R.id.ivStatusGroup3);
        ivStatusGroup[INDEX_INSURANCE] = findViewById(R.id.ivStatusGroup4);

        mCurrentStatus = new String[SHINHAN_GROUP_AFFILIATED_COMPANY_NUM];

        for (int i = INDEX_BANK; i < SHINHAN_GROUP_AFFILIATED_COMPANY_NUM; ++i) {
            String key = null;

            switch (i) {
                case INDEX_BANK:
                    key = SHFGICConfig.CodeGroupCode.BANK.getValue();
                    break;
                case INDEX_CARD:
                    key = SHFGICConfig.CodeGroupCode.CARD.getValue();
                    break;
                case INDEX_INVESTMENT:
                    key = SHFGICConfig.CodeGroupCode.INVESTMENT.getValue();
                    break;
                case INDEX_INSURANCE:
                    key = SHFGICConfig.CodeGroupCode.INSURANCE.getValue();
                    break;
            }

            if (mMyGroupCheckIndex == i)
                continue;

            rlBtnGroup[i].setOnClickListener(this);

            // test code
////            stateCode = "2";

//            affiliatesCodes.put("001", "1");
//            affiliatesCodes.put("002", "1");
//            affiliatesCodes.put("003", "2");
//            affiliatesCodes.put("004", "3");

            if (stateCode.equals(SHFGICConfig.CodeSHFGICState.TERMINATION.getValue())) {
                ivStatusGroup[i].setVisibility(View.GONE);
                mCurrentStatus[i] = stateCode;
            } else {
                if (null != affiliatesCodes.get(key)) {
                    if (affiliatesCodes.get(key).equals(SHFGICConfig.CodeSHFGICState.NORMAL.getValue())) {
                        ivStatusGroup[i].setBackground(getResources().getDrawable(R.drawable.ico_sso_login));
                        ivStatusGroup[i].setVisibility(View.VISIBLE);
                    } else if (affiliatesCodes.get(key).equals(SHFGICConfig.CodeSHFGICState.SUSPENSION.getValue())) {
                        ivStatusGroup[i].setBackground(getResources().getDrawable(R.drawable.ico_sso_login_dis));
                        ivStatusGroup[i].setVisibility(View.VISIBLE);
                    } else
                        ivStatusGroup[i].setVisibility(View.GONE);

                    mCurrentStatus[i] = String.valueOf(affiliatesCodes.get(key));
                } else {
                    ivStatusGroup[i].setVisibility(View.GONE);
                    mCurrentStatus[i] = "0";
                }
            }
        }
    }

    private int getSSOActionGroupType(int id) {
        int rtnValue = SSO_INTENT_VALUE_NONE;

        if (R.id.rlBtnGroup1 == id)
            rtnValue = SSO_INTENT_VALUE_BANK;
        else if (R.id.rlBtnGroup2 == id)
            rtnValue = SSO_INTENT_VALUE_CARD;
        else if (R.id.rlBtnGroup3 == id)
            rtnValue = SSO_INTENT_VALUE_INVESTMENT;
        else if (R.id.rlBtnGroup4 == id)
            rtnValue = SSO_INTENT_VALUE_INSURANCE;

        return rtnValue;
    }

    private String getGroupCurrentStatus(int id) {
        String rtnValue = null;

        if (R.id.rlBtnGroup1 == id)
            rtnValue = mCurrentStatus[INDEX_BANK];
        else if (R.id.rlBtnGroup2 == id)
            rtnValue = mCurrentStatus[INDEX_CARD];
        else if (R.id.rlBtnGroup3 == id)
            rtnValue = mCurrentStatus[INDEX_INVESTMENT];
        else if (R.id.rlBtnGroup4 == id)
            rtnValue = mCurrentStatus[INDEX_INSURANCE];

        return rtnValue;
    }

    private void runSingleSignOnApp(int groupType, String affiliatesCode, String ssoData) {

        if (SSO_INTENT_VALUE_NONE != groupType) {

            if (existPackage(groupType, true)) {
//                Intent ssoIntent = getPackageManager().getLaunchIntentForPackage(getPackageName(groupType));
//
//                switch (groupType) {
//                    case SSO_INTENT_VALUE_BANK:
//                        ssoIntent.putExtra(SSO_INTENT_KEY_ACTION, "bank");
//                        break;
//                    case SSO_INTENT_VALUE_CARD:
//                        ssoIntent.putExtra(SSO_INTENT_KEY_ACTION, "card");
//                        break;
//                    case SSO_INTENT_VALUE_INVESTMENT:
//                        ssoIntent.putExtra(SSO_INTENT_KEY_ACTION, "investment");
//                        break;
//                    case SSO_INTENT_VALUE_INSURANCE:
//                        ssoIntent.putExtra(SSO_INTENT_KEY_ACTION, "insurance");
//                        break;
//                }
//
//                ssoIntent.putExtra(SSO_INTENT_KEY_AFFILIATES_CODE, affiliatesCode);
//                ssoIntent.putExtra(SSO_INTENT_KEY_SSO_DATA, ssoData);

                Intent ssoIntent = new Intent(Intent.ACTION_VIEW);

                String host = null;
                String scheme = null;

                switch (groupType) {
                    case SSO_INTENT_VALUE_BANK:
                        host = "";
                        scheme = "sbankandnor";
                        break;
                    case SSO_INTENT_VALUE_CARD:
                        host = "blcsso";
                        scheme = "shinhan-appcard";
                        break;
                    case SSO_INTENT_VALUE_INVESTMENT:
                        host = "shasso";
                        scheme = "newshinhanialpha";
                        break;
                    case SSO_INTENT_VALUE_INSURANCE:
                        host = "smtsso";
                        scheme = "com.AFSSHLife";
                        break;
                }

                ssoIntent.setData(Uri.parse(scheme + "://" + host + "?" + SSO_PARAM_AFFILIATESCODE + "=" + affiliatesCode + "&"
                        + SSO_PARAM_SSODATA + "=" + ssoData + "&" + SSO_PARAM_SSOTIME + "=" + System.currentTimeMillis()));

                try {
                    startActivity(ssoIntent);
                }
                catch (ActivityNotFoundException e) {
                    showToast(this, "ActivityNotFoundException~!!!", Toast.LENGTH_SHORT);
                }
                catch (Exception e) {
                    showToast(this, e.toString(), Toast.LENGTH_SHORT);
                }
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + getPackageName(groupType)));
                startActivity(intent);

                intent = null;
            }
        }
    }

    private boolean existPackage(int groupType, boolean useExceptionLog) {
        boolean rtnValue = false;

        PackageManager pm = getPackageManager();
        String packageName = getPackageName(groupType);

        try {
            pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);

            rtnValue = true;
        } catch (PackageManager.NameNotFoundException e) {
            if (useExceptionLog)
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }

        return rtnValue;
    }

    public String getPackageName(int groupType) {
        String rtnValue = null;

        switch (groupType) {
            case SSO_INTENT_VALUE_BANK:
                rtnValue = SSO_PACKAGE_BANK;
                break;
            case SSO_INTENT_VALUE_CARD:
                rtnValue = SSO_PACKAGE_CARD;
                break;
            case SSO_INTENT_VALUE_INVESTMENT:
                rtnValue = SSO_PACKAGE_INVESTMENT;
                break;
            case SSO_INTENT_VALUE_INSURANCE:
                rtnValue = SSO_PACKAGE_INSURANCE;
                break;
        }

        return rtnValue;
    }
}
