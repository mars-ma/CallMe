package dev.mars.callme.remote.mina.client;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;

import dev.mars.callme.bean.SocketMessage;
import dev.mars.callme.remote.mina.ISendListener;

import static dev.mars.callme.remote.mina.client.ClientSessionStatus.CONNECTED;


/**
 * Created by ma.xuanwei on 2017/1/4.
 */

public class ConnectingSessionState extends ClientSessionState {


    ConnectingSessionState(MinaSocketClient client) {
        super(client);
    }

    /**
     * 关闭连接
     */
    @Override
    public IoFuture closeConnection() {
        if (minaSocketClient.connectFuture != null && !minaSocketClient.connectFuture.isDone() && !minaSocketClient.connectFuture.isCanceled()) {
            minaSocketClient.connectFuture.cancel();
//                setSessionState(new MinaSessionStateFactory().newState(SocketState.ClOSED));
        }
        return minaSocketClient.connectFuture;
    }

    /**
     * 请求连接
     */
    @Override
    public IoFuture connect() {
        return null;
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
            minaSocketClient.connectFuture.addListener(new IoFutureListener<ConnectFuture>() {
                @Override
                public void operationComplete(ConnectFuture ioFuture) {
                    if (minaSocketClient.getStatus() == CONNECTED) {
                        minaSocketClient.getSessionState().send(msg, listener, tryConnect);
                    } else {
                        if (listener != null) {
                            listener.onSendFailed("发送失败");
                        }
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.onSendFailed("发送失败");
            }
        }
    }
}
