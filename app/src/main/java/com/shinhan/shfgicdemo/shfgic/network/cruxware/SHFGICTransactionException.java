package com.shinhan.shfgicdemo.shfgic.network.cruxware;

public class SHFGICTransactionException extends Exception {
	/**
	 * 
	 */
	public static final String CONNECT_FAILED	= "CONNECT_FAILED";
	public static final String TIMEOUT			= "TIMEOUT";
	public static final String REQUEST_FAILED	= "REQUEST_FAILED";
	public static final String RESPONSE_EROOR	= "RESPONSE_ERROR";
	
	private static final long serialVersionUID = 1L;
	
	public String exception = "";
	public SHFGICTransaction transaction;
	
	public SHFGICTransactionException(String message) {
		super(message);
	}

	public SHFGICTransactionException(Exception e) {
		super(e.getLocalizedMessage());
		if( e instanceof SHFGICTransactionException)
			exception = ((SHFGICTransactionException)e).exception;
	}

	public SHFGICTransactionException(String exception, String message) {
		super(message);
		this.exception = exception;
	}

	public SHFGICTransactionException(String exception, String message, SHFGICTransaction transaction) {
		super(message);
		this.exception = exception;
		this.transaction = transaction;
	}
}
