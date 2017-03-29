package dev.mars.callme.remote.mina.client;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.nio.charset.Charset;

import dev.mars.callme.bean.SocketMessage;
import dev.mars.callme.common.Constants;
import dev.mars.callme.remote.mina.BaseCodecFactory;
import dev.mars.callme.remote.mina.ISendListener;
import dev.mars.callme.remote.mina.KeepAliveFilter;


/**
 * MinaSocket Created by ma.xuanwei on 2016/12/13.
 */

public class MinaSocketClient {
	NioSocketConnector connector;
	ConnectFuture connectFuture;
	//单一session
	IoSession session;
	ClientSessionState sessionState;
	protected String destIP;
	protected int destPort;

	MinaSessionStateFactory sessionStateFactory = new MinaSessionStateFactory();

	public MinaSocketClient() {
		super();
		init();
	}

	/**
	 * 设置IP
	 * @param str
	 */
	public void setIP(String str){
		this.destIP = str;
	}

	/**
	 * 设置端口号
	 * @param port
	 */
	public void setPort(int port){
		destPort = port;
	}

	public String getIP(){
		return destIP;
	}

	public int getPort(){
		return destPort;
	}

	/**
	 * 初始化
	 */
	private void init() {
		connector = new NioSocketConnector();
		connector.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE,
				Constants.READ_IDLE_TIMEOUT);
		connector.getSessionConfig().setIdleTime(IdleStatus.WRITER_IDLE,
				Constants.WRITE_IDLE_TIMEOUT);
		connector.getFilterChain().addLast(
				"BaseFilter",
				new ProtocolCodecFilter(new BaseCodecFactory()));
		connector.getFilterChain().addLast("KeepAlive", new KeepAliveFilter());
		// 设置连接超时检查时间
		connector.setConnectTimeoutCheckInterval(5000);
		connector.setConnectTimeoutMillis(10000); // 10秒后超时
		setSessionState(sessionStateFactory.newState(ClientSessionStatus.ClOSED));
	}

	public void setIoHandler(IoHandler ioHandler){
		connector.setHandler(ioHandler);
	}

	public void setSession(IoSession session) {
		this.session = session;
	}

	public IoSession getSession() {
		return session;
	}

	public void setSessionState(ClientSessionState s) {
		sessionState = s;
	}

	public ClientSessionState getSessionState() {
		return sessionState;
	}

	/**
	 * 获取连接状态
	 * 
	 * @return
	 */
	public ClientSessionStatus getStatus() {
		if (session == null || !session.isConnected()) {
			if (connectFuture != null && !connectFuture.isDone()
					&& !connectFuture.isCanceled()) {
				return ClientSessionStatus.CONNECTING;
			} else {
				return ClientSessionStatus.ClOSED;
			}
		} else {
			return ClientSessionStatus.CONNECTED;
		}
	}

	/**
	 * 连接
	 */
	public IoFuture connect() {
			new Thread(new Runnable() {
				@Override
				public void run() {
					getSessionState().connect();
				}
			}).start();
		return null;
	}

	/**
	 * 发送
	 * 
	 * @param msg
	 */
	public void send(final SocketMessage msg, final ISendListener listener,
					 final boolean tryConnect) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				getSessionState().send(msg, listener, tryConnect);
			}
		}).start();
	}

	/**
	 * 关闭连接
	 */
	public IoFuture closeConnection() {
		return getSessionState().closeConnection();
	}

	public void setConnectFuture(ConnectFuture connectFuture) {
		this.connectFuture = connectFuture;
	}

	public ConnectFuture getConnectFuture() {
		return connectFuture;
	}

	protected class MinaSessionStateFactory {
		public ClientSessionState newState(ClientSessionStatus status) {
			switch (status) {
			case ClOSED:
				return new ClosedSessionState(MinaSocketClient.this);
			case CONNECTING:
				return new ConnectingSessionState(MinaSocketClient.this);
			case CONNECTED:
				return new ConnectedSessionState(MinaSocketClient.this);
			}
			return null;
		}
	}

}
