package com.shinhan.shfgicdemo.shfgic.network.cruxware;

import android.os.Handler;
import android.os.Message;

import com.shinhan.shfgicdemo.util.LogUtil;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class SHFGICSession {
	static private final int _MESSAGE_CANCELED			= 1;
	static private final int _MESSAGE_FAILED			= 2;
	static private final int _MESSAGE_FINISHED			= 3;

	enum SendType {
		SYNC,
		ASYNC,
	};

	private final ExecutorService executor = Executors.newFixedThreadPool(10);
	private HashMap<String, SHFGICTransaction> transactions = new HashMap<String, SHFGICTransaction>();
	private String target;
	private CXSessionEventListener 			eventListener;

	protected int							connectionTimeout   = 10000;
	
	public interface CXSessionEventListener {
		public void onSessionTransmitTransaction(SHFGICSession session, SHFGICTransaction transaction);
		public void onSessionIncomingTransaction(SHFGICSession session, SHFGICTransaction transaction);
		public void onSessionCompleteTransaction(SHFGICSession session, SHFGICTransaction transaction);
		public void onSessionExceptionThrown(SHFGICSession session, Exception e);
	}
	
	public void setEventListener(CXSessionEventListener listener) {
		eventListener = listener;
	}
	public CXSessionEventListener eventListener() {
		return eventListener;
	}
	
	public void transmit(SHFGICTransaction transaction) {
		synchronized (transactions) {
			LogUtil.d("Network", "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< IN : " + transaction.identifier());
			transactions.put(transaction.identifier(), transaction);
		}

		if( eventListener != null ) {
			eventListener.onSessionTransmitTransaction(SHFGICSession.this, transaction);
		}

		executor.execute(new TransmitRunnable(transaction));
	}

	private class TransmitRunnable implements Runnable {
		private SHFGICTransaction transaction;

		public TransmitRunnable(SHFGICTransaction transaction) {
			this.transaction = transaction;
		}

		@Override
		public void run() {
			try {
				boolean flags = onTransmit(transaction);
				if( flags ) {
					doSending(transaction);
				}

			} catch (Exception e) {
				transaction.setError(e);

				SHFGICTransactionException exception = new SHFGICTransactionException(e);
				exception.transaction = transaction;

				onTransmitException(transaction, exception);

				if( eventListener != null ) {
					eventListener.onSessionExceptionThrown(SHFGICSession.this, exception);
				}

				transactionFailed(transaction, exception);

				if( eventListener != null ) {
					eventListener.onSessionCompleteTransaction(SHFGICSession.this, transaction);
				}
			}
		}
	}

	public void incomingResponse(byte[] bytes, String identifier ) {
		SHFGICTransaction transaction = null;
		try {
			synchronized (transactions) {
				LogUtil.d("Network", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> OUT : " + identifier);
				transaction = transactions.get(identifier);
				transactions.remove(identifier);
			}

			if( !transaction.isSynchronous() ) {
				if( eventListener != null ) {
					eventListener.onSessionIncomingTransaction(SHFGICSession.this, transaction);
				}
			}

			transaction.setBytes(bytes);

			if( !transaction.isSynchronous() ) {
				// 이럴경우 false 리턴시 리스너의 해제를 직접 해야 한다.
				if(onResponse(transaction)) {
					transactionFinished(transaction);
				}
			}
		} catch (Exception e) {
			transaction.setError(e);
			
			if( !transaction.isSynchronous() ) {
				SHFGICTransactionException exception = new SHFGICTransactionException(e);
				exception.transaction = transaction;

				if( eventListener != null ) {
					eventListener.onSessionExceptionThrown(SHFGICSession.this, exception);
				}
				
				transactionFailed(transaction, exception);
			}

			//TODO TRACE
		} finally {
			if( eventListener != null ) {
				eventListener.onSessionCompleteTransaction(SHFGICSession.this, transaction);
			}
		}
		
		if( transaction != null && transaction.isSynchronous() ) {
			synchronized (transaction) {
				transaction.notify();
			}
		}
	}
	
	public void cancelAllTransactions() {
		synchronized (transactions) {
			for( String key : transactions.keySet() ) {
				SHFGICTransaction transaction = transactions.get(key);
				transactionCanceled(transaction);					
			}
			
			transactions.clear();
		}
	}
	
	public void transmitSynchronous(SHFGICTransaction transaction) throws Exception {
		transaction.setSynchronous(true);
		
		synchronized (transactions) {
			LogUtil.d("Network", "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< IN : " + transaction.identifier());
			transactions.put(transaction.identifier(), transaction);
		}

		if( doSending(transaction) == SendType.ASYNC ) {
			synchronized (transaction) {
				transaction.wait();
			}
		}
		
		if( transaction.error() != null )
			throw transaction.error();
	}
	
	public void setTarget( String target ) {
		this.target = target;
	}
	public String target() { return this.target; }

	protected void transactionCanceled(SHFGICTransaction transaction) {
		if( transaction != null ) {
			if (transaction.resultListener() != null) {
				transaction.resultListener().onTransactionCanceled(transaction);
			}

			if (transaction.resultOnUiThreadListener() != null) {
				resultHandler.sendMessage(Message.obtain(resultHandler,_MESSAGE_CANCELED, transaction));
			}
		}
		
		transaction.setResultListener(null);
	}
	
	protected void transactionFailed(SHFGICTransaction transaction, SHFGICTransactionException exception) {
		if( transaction != null ) {
			String message		= onTransmitExceptionMessage(transaction, exception);
			SHFGICTransactionException delivery	= new SHFGICTransactionException(message);

			delivery.exception = exception.exception;

			if( transaction.resultListener() != null )
				transaction.resultListener().onTransactionFailed(transaction, delivery);

			if (transaction.resultOnUiThreadListener() != null) {
				Hashtable<String, Object> map = new Hashtable<String, Object>();
				map.put("transaction", transaction);
				map.put("exception", delivery);

				resultHandler.sendMessage(Message.obtain(resultHandler,_MESSAGE_FAILED, map));
			}
		}
		
		transaction.setResultListener(null);
	}
	
	protected void transactionFinished(SHFGICTransaction transaction) {
		if( transaction != null ) {
			if (transaction.resultListener() != null) {
				transaction.resultListener().onTransactionFinished(transaction);
			}

			if (transaction.resultOnUiThreadListener() != null) {
				resultHandler.sendMessage(Message.obtain(resultHandler,_MESSAGE_FINISHED, transaction));
			}
		}
		
		transaction.setResultListener(null);
	}

	final Handler resultHandler = new ResultHandler(this);

	private static class ResultHandler extends Handler {
		private final WeakReference<SHFGICSession> session;

		public ResultHandler(SHFGICSession session) {
			this.session = new WeakReference<SHFGICSession>(session);
		}

		@Override
		public void handleMessage(Message msg) {
			SHFGICSession session = this.session.get();

			switch(msg.what) {
				case _MESSAGE_CANCELED : {
					SHFGICTransaction transaction = (SHFGICTransaction)(msg.obj);

					transaction.resultOnUiThreadListener().onTransactionCanceledOnUiThread(transaction);
					transaction.setResultOnUiThreadListener(null);
				}break;

				case _MESSAGE_FAILED : {
					@SuppressWarnings("unchecked")
                    Hashtable<String, Object> map = (Hashtable<String, Object>)(msg.obj);
					SHFGICTransaction transaction = (SHFGICTransaction)(map.get("transaction"));

					transaction.resultOnUiThreadListener().onTransactionFailedOnUiThread(transaction, (SHFGICTransactionException)(map.get("exception")));
					transaction.setResultOnUiThreadListener(null);
				}break;

				case _MESSAGE_FINISHED : {
					SHFGICTransaction transaction = (SHFGICTransaction)(msg.obj);

					transaction.resultOnUiThreadListener().onTransactionFinishedOnUiThread(transaction);
					transaction.setResultOnUiThreadListener(null);
				}break;
			}
		}
	}

	protected String onTransmitExceptionMessage(SHFGICTransaction transaction, SHFGICTransactionException exception) {
		return exception.getLocalizedMessage();
	}
	
	protected abstract boolean onTransmit(SHFGICTransaction transaction) throws Exception;
	protected abstract SendType doSending(SHFGICTransaction transaction) throws Exception;
	protected abstract boolean onResponse(SHFGICTransaction transaction) throws Exception;
	protected abstract void onTransmitException(SHFGICTransaction transaction, SHFGICTransactionException exception);
}
