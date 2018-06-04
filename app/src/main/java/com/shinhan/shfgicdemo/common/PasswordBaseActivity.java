package com.shinhan.shfgicdemo.common;

/**
 * Created by janghyeon-u on 2018. 3. 8..
 */

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.util.LogUtil;
import com.softsecurity.transkey.Global;
import com.softsecurity.transkey.ITransKeyActionListener;
import com.softsecurity.transkey.ITransKeyActionListenerEx;
import com.softsecurity.transkey.ITransKeyCallbackListener;
import com.softsecurity.transkey.TransKeyActivity;
import com.softsecurity.transkey.TransKeyWebCtrl;

import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * 비밀번호 공통 Activity
 * - 보안키패드 (라온제공) 적용
 */
public class PasswordBaseActivity extends BaseActivity implements ITransKeyActionListener, ITransKeyActionListenerEx, ITransKeyCallbackListener {
    private static final String TAG = PasswordBaseActivity.class.getName();

    private static final int PIN_NUMBER_LENGTH = 6;

    protected final String ENCRYPTED_PIN_DATA_KEY = "encryptedData";
    protected final String CIPHER_PIN_DATA_EX_KEY = "cipherDataEx";

    protected ImageView btnTopBack, btnTopMenu;
    protected TextView tvTopTitle;
    protected TextView btnCancel, btnConfirm;

    protected LinearLayout layoutExplanationMessage;
    protected TextView tvPasswordMessage, tvExplanationMessage_3;
    protected ImageView[] ivPassword = null;

    protected String mEncryptedData = null;
    protected String mCipherDataEx = null;
    protected int mBeforeLength = 0;

    protected boolean isViewCtrlKeypad = false;
    protected TransKeyWebCtrl m_tkMngr = null;
    protected String publicKey = null;        //파일에서 불러올 경우

    protected boolean mDigitError = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        btnTopBack = findViewById(R.id.btnTopBack);
        btnTopMenu = findViewById(R.id.btnTopMenu);
        tvTopTitle = findViewById(R.id.tvTopTitle);

        tvTopTitle.setText(R.string.title_password);
        btnTopBack.setOnClickListener(this);
        btnTopMenu.setOnClickListener(this);

        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm_1);

        btnCancel.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

        ivPassword = new ImageView[PIN_NUMBER_LENGTH];

        for (int i = 0; i < PIN_NUMBER_LENGTH; ++i) {
            ivPassword[i] = findViewById(R.id.password_1 + i);
            ivPassword[i].setOnClickListener(this);
        }

        tvPasswordMessage = findViewById(R.id.passwordMessage);
        SpannableStringBuilder spanStr = new SpannableStringBuilder(getString(R.string.password_info));
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#3a7bd3"));
        spanStr.setSpan(colorSpan, 5, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPasswordMessage.setText(spanStr);

        layoutExplanationMessage = findViewById(R.id.layoutExplanationMessage);
        tvExplanationMessage_3 = findViewById(R.id.explanationMessage_line3);
        spanStr = new SpannableStringBuilder(getString(R.string.password_explanation_line3));
        spanStr.setSpan(colorSpan, 0, 15, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvExplanationMessage_3.setText(spanStr);

        spanStr = null;
        colorSpan = null;

        //pem 파일로부터 PublicKey 생성
        try {
            InputStream fin = getResources().openRawResource(R.raw.server2048);
            CertificateFactory f = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) f.generateCertificate(fin);
            PublicKey pk = certificate.getPublicKey();
            publicKey = new String(Base64.encode(pk.getEncoded(), 0));
        } catch (Exception e) {
            LogUtil.d("error", "exception : " + e.getMessage());
        }

        initTransKeyPad(PIN_NUMBER_LENGTH, getString(R.string.password_max_message), true, (FrameLayout) findViewById(R.id.keypadContainer), (RelativeLayout) findViewById(R.id.keypadBallon));
    }

    public void initTransKeyPad(int maxLength, String maxLengthMessage, boolean bReArrange, FrameLayout keyPadView, RelativeLayout ballonView) {
        try {
            if (m_tkMngr == null)
                m_tkMngr = new TransKeyWebCtrl(this);
            //Activity 로 처리할 때 처럼 파라미터를 인텐트에 넣어서 처리.
            Intent newIntent = getIntentParam(maxLength, maxLengthMessage);
            m_tkMngr.init(newIntent, keyPadView, ballonView);
            m_tkMngr.setReArrangeKeapad(bReArrange);
            m_tkMngr.setTransKeyListener(this);
            m_tkMngr.setTransKeyListenerEx(this);
        } catch (Exception e) {
            LogUtil.d("STACKTRACE", e.getStackTrace().toString());
        }
    }

    public void showTransKeyPad(int keyPadType) {
        m_tkMngr.showKeypad(keyPadType);
    }

    public Intent getIntentParam(int maxLength, String maxLengthMessage) {
        Intent newIntent = new Intent(this.getApplicationContext(),
                TransKeyActivity.class);

        newIntent.putExtra(TransKeyActivity.mTK_PARAM_CRYPT_TYPE, TransKeyActivity.mTK_TYPE_CRYPT_SERVER);
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_DISABLE_BUTTON_EFFECT, false);

        //최대 입력값 설정 1 - 16
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_INPUT_MAXLENGTH, maxLength);

        //인터페이스 - maxLength시에 메시지 박스 보여주기. 기본은 메시지 안나옴.
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_MAX_LENGTH_MESSAGE, maxLengthMessage);

        byte[] secureKey = {'M', 'o', 'b', 'i', 'l', 'e', 'T', 'r', 'a', 'n', 's', 'K', 'e', 'y', '1', '0'};

        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SECURE_KEY, secureKey);

        newIntent.putExtra(TransKeyActivity.mTK_PARAM_SAMEKEY_ENCRYPT_ENABLE, false);

        //         비대칭키 사용시 공개키 설정하는 옵션
        newIntent.putExtra(TransKeyActivity.mTK_PARAM_RSA_PUBLICK_KEY, publicKey);

        return newIntent;
    }

    public void onClick(View v) {
        super.onClick(v);
        Intent intent;
        switch (v.getId()) {
            case R.id.password_1:
            case R.id.password_2:
            case R.id.password_3:
            case R.id.password_4:
            case R.id.password_5:
            case R.id.password_6:

                showTransKeyPad(TransKeyActivity.mTK_TYPE_KEYPAD_NUMBER);

                isViewCtrlKeypad = true;

                for (int i = 0; i < mBeforeLength; ++i)
                    ivPassword[i].setImageResource(R.drawable.img_pwdot);

                mBeforeLength = 0;
                break;

            case R.id.btnConfirm_1:

                if (mBeforeLength < PIN_NUMBER_LENGTH) {
                    Toast t = Toast.makeText(this, R.string.password_min_message, Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();

                    mDigitError = true;

                    onClick(ivPassword[0]);

                    return;
                }
                break;
        }
    }

    @Override
    public void cancel(Intent intent) {
        isViewCtrlKeypad = false;

        m_tkMngr.ClearAllData();
    }

    @Override
    public void done(Intent data) {

        if (mBeforeLength < PIN_NUMBER_LENGTH) {
            showToast(this, getString(R.string.password_min_message), Toast.LENGTH_SHORT);

            mDigitError = true;

            onClick(ivPassword[0]);

            return;
        }

        isViewCtrlKeypad = false;

        if (data == null)
            return;

        mCipherDataEx = data.getStringExtra(TransKeyActivity.mTK_PARAM_CIPHER_DATA_EX);

        // 비대칭키를 사용할 경우 데이터 포맷
        mEncryptedData = data.getStringExtra(TransKeyActivity.mTK_PARAM_RSA_DATA);
        LogUtil.d("encryptedDataFormat", "encryptedData : " + mEncryptedData);
    }

    @Override
    public void input(int i) {
        int currentLength = m_tkMngr.getInputLength();

        for (int j = 0; j < currentLength; ++j)
            ivPassword[j].setImageResource(R.drawable.img_pwdot_on);

        if (mBeforeLength > currentLength) {
            for (int j = currentLength; j < mBeforeLength; ++j)
                ivPassword[j].setImageResource(R.drawable.img_pwdot);
        }

        mBeforeLength = currentLength;

        if (Global.debug) LogUtil.d("TrnasKeyCtrlView", "currentLength :" + currentLength);
    }

    @Override
    public void minTextSizeCallback() {
        LogUtil.d(TAG, "minTextSizeCallback()");
    }

    @Override
    public void maxTextSizeCallback() {
        LogUtil.d(TAG, "maxTextSizeCallback()");
    }

    @Override
    public void onBackPressed() {
        if (isViewCtrlKeypad == true) {
//            m_tkMngr.finishTransKey(true);
//            m_tkMngr.finishTransKey(false);
            m_tkMngr.clearKeypad();
            isViewCtrlKeypad = false;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        m_tkMngr.showKeypad_changeOrientation();
    }
}