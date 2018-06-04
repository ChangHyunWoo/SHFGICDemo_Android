package com.shinhan.shfgicdemo.view.intcertmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shinhan.shfgicdemo.R;
import com.shinhan.shfgicdemo.common.BaseActivity;

/**
 * 신한통합인증 해지/정지 선택 화면
 */
public class TerminationOptionActivity extends BaseActivity {

    private ImageView btnTopBack, btnTopMenu;
    private TextView tvTopTitle;
    private LinearLayout btnTermination, btnSuspension;
    private TextView tvTerminationOption, tvSuspensionOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_termination_option);

        btnTopBack = findViewById(R.id.btnTopBack);
        btnTopMenu = findViewById(R.id.btnTopMenu);
        tvTopTitle = findViewById(R.id.tvTopTitle);

        tvTopTitle.setText(R.string.title_termination_option);
        btnTopBack.setOnClickListener(this);
        btnTopMenu.setOnClickListener(this);

        btnTermination = findViewById(R.id.btnTermination);
        btnSuspension = findViewById(R.id.btnSuspension);

        btnTermination.setOnClickListener(this);
        btnSuspension.setOnClickListener(this);

        tvTerminationOption = findViewById(R.id.tvTerminationOption);
        tvSuspensionOption = findViewById(R.id.tvSuspensionOption);

        tvTerminationOption.setText(Html.fromHtml(getString(R.string.termination_option_desc)));
        tvSuspensionOption.setText(Html.fromHtml(getString(R.string.suspension_option_desc)));
    }

    public void onResume() {
        super.onResume();
    }

    public void onClick(View v) {
        super.onClick(v);
        Intent intent;
        switch (v.getId()) {
            case R.id.btnTermination:   //해지
                intent = new Intent(this, TerminationInfoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;

            case R.id.btnSuspension:    //정지
                intent = new Intent(this, SuspensionInfoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
        }
    }
}
