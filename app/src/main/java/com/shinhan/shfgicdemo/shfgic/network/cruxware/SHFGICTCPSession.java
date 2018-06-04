package com.shinhan.shfgicdemo.shfgic.network.cruxware;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public abstract class SHFGICTCPSession extends SHFGICSession {
	private Socket socket				= null;
	private boolean								receiveThreadStatus	= false;
	
	private Timer keepAliveTimer		= null;
	private Timer timeoutTimer		= null;
	
	private long								keepAliveInterval	= 30000;
	private long								timeoutInterval		= 30000;

	static public interface SHICTCPSessionEventListener extends CXSessionEventListener {
		public void onTCPSessionConnected(SHFGICTCPSession session);
		public void onTCPSessionDisconnected(SHFGICTCPSession session);
	}
	
	public void setTCPSessionEventListener(SHICTCPSessionEventListener listener) {
		super.setEventListener(listener);
	}
	
	public SHICTCPSessionEventListener getTCPSessionEventListener() {
		if( eventListener() != null && eventListener() instanceof SHICTCPSessionEventListener )
			return (SHICTCPSessionEventListener) eventListener();

		return null;
	}
	
	private void fireKeepAliveTimer() {
		keepAliveTimer = new Timer();
		keepAliveTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				try {
					keepAlive();
					
					timeoutTimer = new Timer();
					timeoutTimer.schedule(new TimerTask() {
						
						@Override
						public void run() {
							try {
								if( eventListener() != null ) {
									eventListener().onSessionExceptionThrown(SHFGICTCPSession.this, new SHFGICTransactionException(SHFGICTransactionException.TIMEOUT, "Request Timeout"));
								}
								
								disconnect();
							} catch (Exception e) {
								if( eventListener() != null ) {
									eventListener().onSessionExceptionThrown(SHFGICTCPSession.this, e);
								}
							}
						}
					}, timeoutInterval);
					
				} catch (Exception e) {
					try {
						if( eventListener() != null ) {
							eventListener().onSessionExceptionThrown(SHFGICTCPSession.this, e);
						}
						
						disconnect();
					} catch (Exception e2) {
						if( eventListener() != null ) {
							eventListener().onSessionExceptionThrown(SHFGICTCPSession.this, e2);
						}
					}

					//TODO TRACE
				}					
			}
		}, keepAliveInterval);
	}
	
	public boolean isLinked() {
		if (socket == null) return false;
		
		return true;
	}
	
	public void connect() throws Exception {
		if (socket == null) {
			try {
				socket = new Socket();
				
				String[] address = target().split(":");
				
				InetSocketAddress remoteAddr = new InetSocketAddress(address[0], Integer.parseInt(address[1]));
				socket.connect(remoteAddr, connectionTimeout);
				
				//LogUtil.LOGD("Session connected");
				
				if( getTCPSessionEventListener() != null )
					getTCPSessionEventListener().onTCPSessionConnected(this);
				
				fireKeepAliveTimer();
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						byte[]	stream = null;
						
						receiveThreadStatus = true;
						while (receiveThreadStatus)
						{
							try
							{
								int		cursor        	= 0;
								int		remainSize    	= 0;
								byte[]	bytes			= new byte[8192];
								
								int receiveSize = socket.getInputStream().read(bytes);		
								if (receiveSize <= 0) {
									throw new Exception("socket read: " + receiveSize);
								}
								
								if( stream == null ) {
									stream = new byte[receiveSize];
									System.arraycopy(bytes, 0, stream, 0, receiveSize);
								} else {
									int length = stream.length + receiveSize;
									byte[] temp = new byte[length];
									
									System.arraycopy(stream, 0, temp, 0, stream.length);
									System.arraycopy(bytes, 0, temp, stream.length, receiveSize);
									stream = temp;
								}
								
								remainSize = stream.length;
								cursor     = 0;
								
								byte[] parsingBytes;
								
								while(remainSize > 0) {
									parsingBytes = new byte[remainSize]; 
									System.arraycopy(stream, cursor, parsingBytes, 0, remainSize);

									int parsing = receivedData(parsingBytes);
									if( parsing == 0 )
										break;
									
									if( parsing < 0 ) { // error
										remainSize = 0;
										break;
									}
									
									remainSize -= parsing;
									cursor     += parsing;
								}
								
								if( remainSize > 0 ) {
									byte[] remain = new byte[remainSize];								
									System.arraycopy(stream, cursor, remain, 0, remainSize);
									stream = remain;
								}
								else {
									stream = null;
								}
							}
							catch (Exception e)	{
								try {
									if( eventListener() != null ) {
										eventListener().onSessionExceptionThrown(SHFGICTCPSession.this, e);
									}
									
									if( socket != null ) {
										// LogUtil.LOGD("Session disconnected");
										
										disconnected();
										
										if( getTCPSessionEventListener() != null )
											getTCPSessionEventListener().onTCPSessionDisconnected(SHFGICTCPSession.this);
										
										close();
									}
								} catch (Exception e2) {
									if( eventListener() != null ) {
										eventListener().onSessionExceptionThrown(SHFGICTCPSession.this, e2);
									}

									//TODO TRACE
								}
								
								break;
							}
							
							try	{
								Thread.sleep(100);
							}
							catch (Exception e) {
								//TODO TRACE
							}
						}
					}
				}).start();
			} catch (Exception e) {
				socket = null;
				throw e;
			}
		}
	}
	
	public void disconnect() throws Exception {
		if( socket == null ) 
			return;
		
		receiveThreadStatus = false;
		
		// LogUtil.LOGD("Session disconnected");
		
		disconnected();
		
		if( getTCPSessionEventListener() != null )
			getTCPSessionEventListener().onTCPSessionDisconnected(this);
		
		close();
	}
	
	public void keepAliveFinished() {
		timeoutTimer.cancel();
		timeoutTimer = null;
		
		fireKeepAliveTimer();
	}
	
	public void keepAliveFailed() {
		try {
			if( eventListener() != null ) {
				eventListener().onSessionExceptionThrown(SHFGICTCPSession.this, new Exception("Session keep alive failed"));
			}
			
			disconnect();
		} catch (Exception e) {
			if( eventListener() != null ) {
				eventListener().onSessionExceptionThrown(SHFGICTCPSession.this, e);
			}

			//TODO TRACE
		}
	}
	
	@Override
	protected SendType doSending(SHFGICTransaction transaction) throws Exception {
		byte [] stream = transaction.bytes();
		socket.getOutputStream().write(stream);

		return SendType.ASYNC;
	}
	
	protected void close() throws Exception {
		if( socket == null ) 
			return;
		
		// LogUtil.LOGD("Session close");
		
		cancelAllTransactions();
		
		if(keepAliveTimer != null) {
			keepAliveTimer.cancel();
			keepAliveTimer = null;
		}
		
		if(timeoutTimer != null) {
			timeoutTimer.cancel();
			timeoutTimer = null;
		}
		
		if (socket != null)	{
			if( socket.getInputStream() != null )
				socket.shutdownInput();
			
			if( socket.getOutputStream() != null )
				socket.shutdownOutput();
		}
		
		if (socket != null) {
			socket.close();
			socket = null;
		}
	}
	
	@Override
	protected abstract boolean onTransmit(SHFGICTransaction transaction) throws Exception;
	protected abstract void onTransmitException(SHFGICTransaction transaction, SHFGICTransactionException exception);
	protected abstract int  receivedData(byte[] bytes) throws Exception;
	protected abstract void keepAlive() throws Exception;
	protected abstract void disconnected();
	
}
