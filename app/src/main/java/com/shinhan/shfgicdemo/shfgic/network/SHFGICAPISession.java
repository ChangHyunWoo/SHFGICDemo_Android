package com.shinhan.shfgicdemo.shfgic.network;

import com.shinhan.shfgicdemo.shfgic.network.cruxware.SHFGICTransaction;
import com.shinhan.shfgicdemo.shfgic.network.cruxware.SHFGICURLSession;
import com.shinhan.shfgicdemo.util.LogUtil;

/**
 * Created by a60000732 on 2017. 6. 28..
 * 네트워크 통신
 */

public class SHFGICAPISession extends SHFGICURLSession {
    private static final String TAG = SHFGICAPITransaction.class.getName();

    private static SHFGICAPISession mInstance = null;

    public static SHFGICAPISession getInstance() {
        if(mInstance == null) {
            mInstance = new SHFGICAPISession();
        }

        return mInstance;
    }

    @Override
    protected boolean onTransmit(SHFGICTransaction transaction) throws Exception {
        if(!(transaction instanceof SHFGICAPITransaction)) {
            return true;
        }

        SHFGICAPITransaction tr = (SHFGICAPITransaction) transaction;
        String code = tr.code();

        tr.setCharset(targetCharset());
        tr.setTarget(tr.target());
        tr.putForHTTPHeader("Content-Type", "application/json;charset=" + targetCharset());

        return true;
    }

    @Override
    protected boolean onResponse(SHFGICTransaction transaction) throws Exception {
        if(!(transaction instanceof SHFGICAPITransaction)) {
            return true;
        }

        SHFGICAPITransaction tr = (SHFGICAPITransaction) transaction;
        LogUtil.d(TAG,"onResponse : " + tr.response().toString());

        return true;
    }

}
