package com.shinhan.shfgicdemo.shfgic.network.cruxware;

import java.util.HashMap;

public abstract class SHFGICURLTransaction extends SHFGICTransaction {
    protected HashMap<String, String> _httpHeader;
    private String _method = "POST";
    private String _target;
    private String _targetCharset  = null;
    private String _localCharset   = null;

    public SHFGICURLTransaction() {
        super();
    }

    public SHFGICURLTransaction(ResultListener listener) {
        super(listener);
    }
    public SHFGICURLTransaction(ResultOnUiThreadListener listener)  {
        super(listener);
    }

    @Override
    public void initialize() {
        super.initialize();;

        _httpHeader = new HashMap<String, String>();
    }

    public void setTarget(String target) { _target = target; }
    public String target() { return _target; }

    public void setMethod(String method) { _method = method; }
    public String method() { return _method; }

    public void setCharset(String charSet) { _targetCharset = charSet; _localCharset = charSet; }

    public void setTargetCharset(String charSet) { _targetCharset = charSet; }
    public String targetCharset() { return _targetCharset; }

    public void setLocalCharset(String charSet) { _localCharset = charSet; }
    public String localCharset() { return _localCharset; }

    public HashMap<String, String> httpHeader() { return _httpHeader; }

    public void putForHTTPHeader(String key, String value) {
        _httpHeader.put(key, value);
    }
}
