package dev.mars.callme.remote.mina.client;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;

import java.net.InetSocketAddress;

import dev.mars.callme.bean.SocketMessage;
import dev.mars.callme.remote.mina.ISendListener;
import dev.mars.callme.utils.LogUtils;

import static dev.mars.callme.remote.mina.client.ClientSessionStatus.CONNECTED;
import static dev.mars.callme.remote.mina.client.ClientSessionStatus.CONNECTING;


/**
 * Created by ma.xuanwei on 2017/1/4.
 */

public class ClosedSessionState extends ClientSessionState {

    ClosedSessionState(MinaSocketClient client) {
        super(client);
    }

    /**
     * 关闭连接
     */
    @Override
    public IoFuture closeConnection() {
        //已经在关闭状态，什么都不做
        return null;
    }

    /**
     * 请求连接
     */
    @Override
    public void connect(final IoFutureListener<ConnectFuture> ioFutureListener) {
        LogUtils.DT("TCP 客户端向 "+minaSocketClient.getIP()+" : "+minaSocketClient.getPort()+" 发起请求");
        minaSocketClient.setSessionState(minaSocketClient.sessionStateFactory.newState(CONNECTING));
        minaSocketClient.setConnectFuture((minaSocketClient.connector.connect(new InetSocketAddress(minaSocketClient.getIP(), minaSocketClient.getPort()))));
        minaSocketClient.getConnectFuture().addListener(new IoFutureListener<ConnectFuture>() {
            @Override
            public void operationComplete(ConnectFuture ioFuture) {
                if (!ioFuture.isConnected() || ioFuture.isCanceled()) {
                    minaSocketClient.session = null;
                    minaSocketClient.setSessionState(new ClosedSessionState(minaSocketClient));
                } else {
                    minaSocketClient.setSessionState(minaSocketClient.sessionStateFactory.newState(CONNECTED));
                    minaSocketClient.session = ioFuture.getSession();
                }
            }
        });
        minaSocketClient.getConnectFuture().addListener(ioFutureListener);
    }

    /**
     * 发送消息
     *
     * @param msg
     * @param listener
     * @param tryConnect 是否在无连接状态下请求连接
     */
    @Override
    public void send(final SocketMessage msg, final ISendListener listener, final boolean tryConnect) {
        if (tryConnect) {
            connect(new IoFutureListener<ConnectFuture>() {
                @Override
                public void operationComplete(ConnectFuture ioFuture) {
                    if (minaSocketClient.getStatus() == CONNECTED){
                        minaSocketClient.getSessionState().send(msg, listener, tryConnect);
                    }else {
                        if (listener != null) {
                            listener.onSendFailed("发送失败");
                        }
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.onSendFailed("发送失败，网络异常。");
            }
        }
    }
}
