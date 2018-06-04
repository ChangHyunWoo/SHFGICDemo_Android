package com.shinhan.shfgicdemo.shfgic.network.cruxware;

import java.util.HashMap;

public abstract class SHFGICTransaction {
	static private String __synchronizer = "__synchronizer";
	static private int __identifierHandler = 0;
	
	private ResultListener  			_resultListener;
	private ResultOnUiThreadListener	_resultOnUiThreadListener;
	private String _identifier;
	private Exception _error;
	private boolean 					_synchronous		= false;
	private boolean						_indicatorEnabled	= true;
	private int							_timeout			= 60000;
	private HashMap<String, Object> _userInfo			= new HashMap<String, Object>();

	public SHFGICTransaction() {
		super();

		initialize();
	}
	
	public SHFGICTransaction(ResultListener listener) {
		super();

		_resultListener = listener;
		initialize();
	}

	public SHFGICTransaction(ResultOnUiThreadListener listener) {
		super();

		_resultOnUiThreadListener = listener;
		initialize();
	}

	public void initialize() {
		synchronized (__synchronizer) {
			_identifier = String.format("%08x", __identifierHandler);

			__identifierHandler++;
			if (__identifierHandler == 0xffffffff) {
				__identifierHandler = 0;
			}
		}
	}

	public void setTimeout(int timeout) { _timeout = timeout; }
	public int timeout() { return  _timeout; }

	public String identifier() {
		return _identifier;
	}

	public void setSynchronous(boolean flags) {
		_synchronous = flags;
	}
	public boolean isSynchronous() {
		return _synchronous;
	}

	public void setError(Exception exception) {
		_error = exception;
	}
	public Exception error() {
		return _error;
	}

	public void putUserInfo(String key, Object value) {
		_userInfo.put(key, value);
	}
	public Object userInfo(String key) {
		return _userInfo.get(key);
	}

	public void setIndicatorEnabled(boolean flags) {
		_indicatorEnabled = flags;
	}
	public boolean indicatorEnabled() {
		return _indicatorEnabled;
	}


	public interface ResultListener {
		void onTransactionCanceled(SHFGICTransaction transaction);
		void onTransactionFailed(SHFGICTransaction transaction, SHFGICTransactionException exception);
		void onTransactionFinished(SHFGICTransaction transaction);
	}

	public void setResultListener(ResultListener listener) {
		_resultListener = listener;
	}

	public ResultListener resultListener() {
		return _resultListener;
	}

	public interface ResultOnUiThreadListener {
		void onTransactionCanceledOnUiThread(SHFGICTransaction transaction);
		void onTransactionFailedOnUiThread(SHFGICTransaction transaction, SHFGICTransactionException exception);
		void onTransactionFinishedOnUiThread(SHFGICTransaction transaction);
	}

	void setResultOnUiThreadListener(ResultOnUiThreadListener listener) {
		_resultOnUiThreadListener = listener;
	}

	ResultOnUiThreadListener resultOnUiThreadListener() {
		return _resultOnUiThreadListener;
	}
	
	public abstract byte[] bytes() throws Exception;
	//public abstract byte[] getRequest() throws Exception;
	public abstract void setBytes(byte[] bytes) throws Exception;
}
