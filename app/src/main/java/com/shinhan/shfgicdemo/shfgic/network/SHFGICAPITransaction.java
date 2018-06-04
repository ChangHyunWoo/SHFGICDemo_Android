package com.shinhan.shfgicdemo.shfgic.network;

import com.shinhan.shfgicdemo.shfgic.SHFGICProperty;
import com.shinhan.shfgicdemo.shfgic.network.cruxware.SHFGICTransaction;
import com.shinhan.shfgicdemo.shfgic.network.cruxware.SHFGICTransactionException;
import com.shinhan.shfgicdemo.shfgic.network.cruxware.SHFGICURLTransaction;
import com.shinhan.shfgicdemo.util.LogUtil;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by a60000732 on 2017. 6. 28..
 * 네트워크 통신
 */

public class SHFGICAPITransaction extends SHFGICURLTransaction {
    private static final String TAG = SHFGICAPITransaction.class.getName();

    private String     _code        = "";

    private JSONObject _request;
    private JSONObject _response;
    private JSONObject _option;

    private boolean	   _errorPopupEnabled	        = true;

    public SHFGICAPITransaction() {
        super();
    }

    public SHFGICAPITransaction(SHFGICTransaction.ResultListener listener) {
        super(listener);
    }

    public SHFGICAPITransaction(SHFGICTransaction.ResultOnUiThreadListener listener)  {
        super(listener);
    }

    public void setErrorPopupEnabled(boolean flags) {
        _errorPopupEnabled = flags;
    }
    public boolean errorPopupEnabled() {
        return _errorPopupEnabled;
    }

    @Override
    public void initialize() {
        super.initialize();

        _request = new JSONObject();
        _response = new JSONObject();
        _option = new JSONObject();

        try {
            _request.putOpt("dataHeader", new JSONObject());
            _request.putOpt("dataBody", new JSONObject());

            putForHTTPHeader("reqKey", identifier());
        } catch (Exception e) {
            LogUtil.trace(e);
        }
    }

    @Override
    public byte[] bytes() throws Exception {
        String string   = _request.toString();

        String log = _request.toString(4);
        String[] lines = log.split("\\n");
        LogUtil.d(TAG, "SEND : " + "-------------------------------------------------------------------------------------------------------------------------");
        LogUtil.d(TAG, "SEND : " + " * SHIC Open API request");
        LogUtil.d(TAG, "SEND : " + " * id   : " + identifier());
        LogUtil.d(TAG, "SEND : " + " * code : " + _code);
        LogUtil.d(TAG, "SEND : " + " * url  : " + target());
        LogUtil.d(TAG, "SEND : " + " * HTTP header" );

//        for( int i = 0; i < lines.length; i++ ) {
//            LogUtil.d("RECV", lines[i]);
//        }

        LogUtil.d(TAG, "SEND : " + " * HTTP body" );
        for( int i = 0; i < lines.length; i++ ) {
            LogUtil.d(TAG, "SEND : " + lines[i]);
        }
        LogUtil.d(TAG, "SEND : " + "-------------------------------------------------------------------------------------------------------------------------");

        return string.getBytes(targetCharset());
    }

    @Override
    public void setBytes(byte[] bytes) throws Exception {
        String string = new String(bytes, localCharset());
        try {
            _response = new JSONObject(string);
        } catch (Exception e) {
            LogUtil.d(TAG, "RECV : " + "-------------------------------------------------------------------------------------------------------------------------");
            LogUtil.d(TAG, "RECV : " + " * SHIC Open API Response");
            LogUtil.d(TAG, "RECV : " + " * id   : " + identifier());
            LogUtil.d(TAG, "RECV : " + " * code : " + _code);
            LogUtil.d(TAG, "RECV : " + " * url  : " + target());
            LogUtil.d(TAG, "RECV : " + "----Invalid response data!!!-----");
            LogUtil.d(TAG, "RECV : " + string );
            LogUtil.d(TAG, "RECV : " + "----Invalid response data!!!-----");
            LogUtil.d(TAG, "RECV : " + "-------------------------------------------------------------------------------------------------------------------------");
            throw new SHFGICTransactionException(SHFGICTransactionException.REQUEST_FAILED, "Invalid response data");
        }

        String log = _response.toString(4);
        String[] lines = log.split("\\n");
        LogUtil.d(TAG, "RECV : " + "-------------------------------------------------------------------------------------------------------------------------");
        LogUtil.d(TAG, "RECV : " + " * SHIC Open API Response");
        LogUtil.d(TAG, "RECV : " + " * id   : " + identifier());
        LogUtil.d(TAG, "RECV : " + " * code : " + _code);
        LogUtil.d(TAG, "RECV : " + " * url  : " + target());
        LogUtil.d(TAG, "RECV : " + " * HTTP header" );
//        for( int i = 0; i < lines.length; i++ ) {
//            LogUtil.d("RECV", lines[i]);
//        }
        LogUtil.d(TAG, "RECV : " + " * HTTP body" );
        for( int i = 0; i < lines.length; i++ ) {
            LogUtil.d(TAG, "RECV : " + lines[i]);
        }
        LogUtil.d(TAG, "RECV : " + "-------------------------------------------------------------------------------------------------------------------------");

    }

    public String code() {
        return _code;
    }
    public void setCode(String code) {
        _code = code;
    }

    public JSONObject option() {
        return _option;
    }
    public void setOption(JSONObject option) {
        Iterator<String> keys = option.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            try {
                Object value = option.opt(key);
                _option.putOpt(key, value);
            } catch (Exception e) {
                LogUtil.trace(e);
            }
        }

        setIndicatorEnabled(_option.optBoolean("indicator", indicatorEnabled()));
        setErrorPopupEnabled(_option.optBoolean("errorPopup", errorPopupEnabled()));
    }

    public void putForHeader(String key, Object value) {
        try {
            _request.optJSONObject("dataHeader").putOpt(key, value);
        } catch (Exception e) {
            LogUtil.trace(e);
        }
    }

    public void putForHeader(JSONObject object) {
        JSONObject head = _request.optJSONObject("dataHeader");

        Iterator<String> keys = object.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            try {
                Object value = object.opt(key);
                head.putOpt(key, value);
            } catch (Exception e) {
                LogUtil.trace(e);
            }
        }
    }

    public void put(String key, Object value) {
        try {
            _request.optJSONObject("dataBody").putOpt(key, value);
        } catch (Exception e) {
            LogUtil.trace(e);
        }
    }

    public void put(JSONObject object) {
        JSONObject body = _request.optJSONObject("dataBody");

        Iterator<String> keys = object.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            try {
                Object value = object.opt(key);
                body.putOpt(key, value);
            } catch (Exception e) {
                LogUtil.trace(e);
            }
        }
    }

    public JSONObject request() { return _request; }
    public JSONObject response() { return _response; }

    public void transmit() {
        try {
            SHFGICAPISession.getInstance().transmit(this);
        } catch (Exception e) {
            LogUtil.trace(e);
        }
    }

}
